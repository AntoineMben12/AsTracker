const express = require('express');
const router = express.Router();

const {
    getNotifications,
    markAsRead,
    markAllRead,
    deleteNotification,
    createNotification,
} = require('../controllers/notificationController');
const { protect } = require('../middleware/auth');

// All routes are protected
router.use(protect);

// GET  /api/notifications           – list all (grouped by day)
// POST /api/notifications           – create a notification
router.route('/').get(getNotifications).post(createNotification);

// PUT  /api/notifications/read-all  – mark all read  (MUST be before /:id)
router.put('/read-all', markAllRead);

// PUT  /api/notifications/:id/read  – mark single read
router.put('/:id/read', markAsRead);

// DELETE /api/notifications/:id
router.delete('/:id', deleteNotification);

module.exports = router;
