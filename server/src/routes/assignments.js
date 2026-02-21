const express = require('express');
const router = express.Router();

const {
    getAssignments,
    getAssignment,
    createAssignment,
    updateAssignment,
    deleteAssignment,
    getStats,
    createValidators,
} = require('../controllers/assignmentController');
const { protect } = require('../middleware/auth');
const { validate } = require('../middleware/validate');

// All routes are protected
router.use(protect);

// GET /api/assignments/stats  — MUST be before /:id to avoid "stats" being treated as an id
router.get('/stats', getStats);

router.route('/').get(getAssignments).post(createValidators, validate, createAssignment);

router
    .route('/:id')
    .get(getAssignment)
    .put(updateAssignment)
    .delete(deleteAssignment);

module.exports = router;
