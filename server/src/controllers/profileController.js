const User = require('../models/User');
const Assignment = require('../models/Assignment');

/**
 * @desc  Get profile with computed assignment stats
 * @route GET /api/profile
 * @access Private
 */
const getProfile = async (req, res, next) => {
    try {
        const userId = req.user._id;

        // Fetch stats in parallel
        const [total, completed, overdue, pending] = await Promise.all([
            Assignment.countDocuments({ userId }),
            Assignment.countDocuments({ userId, status: 'completed' }),
            Assignment.countDocuments({ userId, status: 'overdue' }),
            Assignment.countDocuments({ userId, status: 'pending' }),
        ]);

        // Average progress
        const progressAgg = await Assignment.aggregate([
            { $match: { userId } },
            { $group: { _id: null, avg: { $avg: '$progress' } } },
        ]);
        const avgProgress =
            progressAgg.length > 0 ? Math.round(progressAgg[0].avg) : 0;

        res.status(200).json({
            success: true,
            data: {
                user: req.user,
                stats: {
                    totalAssignments: total,
                    completed,
                    active: pending,
                    overdue,
                    avgProgress,
                },
            },
        });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Update profile (name, major, year, avatarUrl)
 * @route PUT /api/profile
 * @access Private
 */
const updateProfile = async (req, res, next) => {
    try {
        const allowedFields = ['name', 'major', 'year', 'avatarUrl'];
        const updates = {};
        allowedFields.forEach((field) => {
            if (req.body[field] !== undefined) {
                updates[field] = req.body[field];
            }
        });

        const user = await User.findByIdAndUpdate(req.user._id, updates, {
            new: true,
            runValidators: true,
        });

        res.status(200).json({ success: true, data: user });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Change password
 * @route PUT /api/profile/password
 * @access Private
 */
const changePassword = async (req, res, next) => {
    try {
        const { currentPassword, newPassword } = req.body;
        if (!currentPassword || !newPassword) {
            return res
                .status(400)
                .json({ success: false, error: 'Please provide currentPassword and newPassword' });
        }
        if (newPassword.length < 6) {
            return res
                .status(400)
                .json({ success: false, error: 'New password must be at least 6 characters' });
        }

        // Load user with password
        const user = await User.findById(req.user._id).select('+password');
        const isMatch = await user.matchPassword(currentPassword);
        if (!isMatch) {
            return res
                .status(401)
                .json({ success: false, error: 'Current password is incorrect' });
        }

        user.password = newPassword;
        await user.save(); // triggers bcrypt pre-save hook

        res.status(200).json({ success: true, message: 'Password updated successfully' });
    } catch (err) {
        next(err);
    }
};

module.exports = { getProfile, updateProfile, changePassword };
