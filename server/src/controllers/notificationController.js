const Notification = require('../models/Notification');

/**
 * @desc  Get all notifications for logged-in user
 * @route GET /api/notifications
 * @access Private
 */
const getNotifications = async (req, res, next) => {
    try {
        const notifications = await Notification.find({ userId: req.user._id })
            .sort({ createdAt: -1 });

        // Group into TODAY / YESTERDAY / EARLIER for the Android UI
        const now = new Date();
        const startOfToday = new Date(now);
        startOfToday.setHours(0, 0, 0, 0);
        const startOfYesterday = new Date(startOfToday);
        startOfYesterday.setDate(startOfYesterday.getDate() - 1);

        const grouped = { today: [], yesterday: [], earlier: [] };
        notifications.forEach((n) => {
            const created = new Date(n.createdAt);
            if (created >= startOfToday) {
                grouped.today.push(n);
            } else if (created >= startOfYesterday) {
                grouped.yesterday.push(n);
            } else {
                grouped.earlier.push(n);
            }
        });

        const unreadCount = notifications.filter((n) => !n.isRead).length;

        res.status(200).json({
            success: true,
            count: notifications.length,
            unreadCount,
            data: grouped,
        });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Mark a single notification as read
 * @route PUT /api/notifications/:id/read
 * @access Private
 */
const markAsRead = async (req, res, next) => {
    try {
        const notification = await Notification.findOneAndUpdate(
            { _id: req.params.id, userId: req.user._id },
            { isRead: true },
            { new: true }
        );
        if (!notification) {
            return res.status(404).json({ success: false, error: 'Notification not found' });
        }
        res.status(200).json({ success: true, data: notification });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Mark all notifications as read
 * @route PUT /api/notifications/read-all
 * @access Private
 */
const markAllRead = async (req, res, next) => {
    try {
        await Notification.updateMany(
            { userId: req.user._id, isRead: false },
            { isRead: true }
        );
        res.status(200).json({ success: true, message: 'All notifications marked as read' });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Delete a notification
 * @route DELETE /api/notifications/:id
 * @access Private
 */
const deleteNotification = async (req, res, next) => {
    try {
        const notification = await Notification.findOneAndDelete({
            _id: req.params.id,
            userId: req.user._id,
        });
        if (!notification) {
            return res.status(404).json({ success: false, error: 'Notification not found' });
        }
        res.status(200).json({ success: true, data: {} });
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Create a notification (internal utility — useful for server-side triggers)
 * @route POST /api/notifications
 * @access Private
 */
const createNotification = async (req, res, next) => {
    try {
        const notification = await Notification.create({
            ...req.body,
            userId: req.user._id,
        });
        res.status(201).json({ success: true, data: notification });
    } catch (err) {
        next(err);
    }
};

module.exports = {
    getNotifications,
    markAsRead,
    markAllRead,
    deleteNotification,
    createNotification,
};
