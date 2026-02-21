const { body } = require('express-validator');
const Assignment = require('../models/Assignment');

// ── Validators ─────────────────────────────────────────────────────────────────
const createValidators = [
    body('title').trim().notEmpty().withMessage('Title is required'),
    body('subject').trim().notEmpty().withMessage('Subject is required'),
    body('dueDate')
        .notEmpty()
        .withMessage('Due date is required')
        .isISO8601()
        .withMessage('Due date must be a valid date (ISO8601)'),
    body('priority')
        .optional()
        .isIn(['Low', 'Medium', 'High'])
        .withMessage('Priority must be Low, Medium, or High'),
];

// ── Helpers ────────────────────────────────────────────────────────────────────
const buildFilter = (userId, query) => {
    const filter = { userId };
    if (query.status) filter.status = query.status;
    if (query.priority) filter.priority = query.priority;
    if (query.subject) filter.subject = new RegExp(query.subject, 'i');
    return filter;
};

// ── Controllers ────────────────────────────────────────────────────────────────

/**
 * @desc  Get all assignments for logged-in user
 * @route GET /api/assignments
 * @access Private
 */
const getAssignments = async (req, res, next) => {
    try {
        const filter = buildFilter(req.user._id, req.query);
        const assignments = await Assignment.find(filter).sort({ dueDate: 1 });
        res.status(200).json({ success: true, count: assignments.length, data: assignments });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Get a single assignment
 * @route GET /api/assignments/:id
 * @access Private
 */
const getAssignment = async (req, res, next) => {
    try {
        const assignment = await Assignment.findOne({
            _id: req.params.id,
            userId: req.user._id,
        });
        if (!assignment) {
            return res.status(404).json({ success: false, error: 'Assignment not found' });
        }
        res.status(200).json({ success: true, data: assignment });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Create a new assignment
 * @route POST /api/assignments
 * @access Private
 */
const createAssignment = async (req, res, next) => {
    try {
        const assignment = await Assignment.create({
            ...req.body,
            userId: req.user._id,
        });
        res.status(201).json({ success: true, data: assignment });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Update an assignment
 * @route PUT /api/assignments/:id
 * @access Private
 */
const updateAssignment = async (req, res, next) => {
    try {
        let assignment = await Assignment.findOne({
            _id: req.params.id,
            userId: req.user._id,
        });
        if (!assignment) {
            return res.status(404).json({ success: false, error: 'Assignment not found' });
        }

        // Apply updates and re-save (triggers pre-save hooks for progress & overdue)
        Object.assign(assignment, req.body);
        await assignment.save();

        res.status(200).json({ success: true, data: assignment });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Delete an assignment
 * @route DELETE /api/assignments/:id
 * @access Private
 */
const deleteAssignment = async (req, res, next) => {
    try {
        const assignment = await Assignment.findOneAndDelete({
            _id: req.params.id,
            userId: req.user._id,
        });
        if (!assignment) {
            return res.status(404).json({ success: false, error: 'Assignment not found' });
        }
        res.status(200).json({ success: true, data: {} });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Get assignment stats for logged-in user
 * @route GET /api/assignments/stats
 * @access Private
 */
const getStats = async (req, res, next) => {
    try {
        const userId = req.user._id;
        const [total, completed, overdue, pending] = await Promise.all([
            Assignment.countDocuments({ userId }),
            Assignment.countDocuments({ userId, status: 'completed' }),
            Assignment.countDocuments({ userId, status: 'overdue' }),
            Assignment.countDocuments({ userId, status: 'pending' }),
        ]);

        // Average progress across all assignments
        const progressAgg = await Assignment.aggregate([
            { $match: { userId } },
            { $group: { _id: null, avgProgress: { $avg: '$progress' } } },
        ]);
        const avgProgress = progressAgg.length > 0 ? Math.round(progressAgg[0].avgProgress) : 0;

        // Due today
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);
        const endOfToday = new Date();
        endOfToday.setHours(23, 59, 59, 999);
        const dueToday = await Assignment.countDocuments({
            userId,
            dueDate: { $gte: startOfToday, $lte: endOfToday },
            status: { $ne: 'completed' },
        });

        res.status(200).json({
            success: true,
            data: { total, completed, overdue, active: pending, dueToday, avgProgress },
        });
    } catch (err) {
        next(err);
    }
};

module.exports = {
    getAssignments,
    getAssignment,
    createAssignment,
    updateAssignment,
    deleteAssignment,
    getStats,
    createValidators,
};
