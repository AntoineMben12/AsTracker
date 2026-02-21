const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema(
    {
        userId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true,
        },
        title: {
            type: String,
            required: [true, 'Notification must have a title'],
            trim: true,
        },
        body: {
            type: String,
            required: [true, 'Notification must have a body'],
        },
        boldWord: {
            type: String,
            default: '',
        },
        type: {
            type: String,
            enum: [
                'deadline',
                'new_assignment',
                'grade',
                'reminder',
                'cancellation',
                'submission',
                'general',
            ],
            default: 'general',
        },
        isRead: {
            type: Boolean,
            default: false,
        },
        actions: {
            type: [
                {
                    label: { type: String, required: true },
                    isPrimary: { type: Boolean, default: false },
                },
            ],
            default: [],
        },
    },
    {
        timestamps: true,
    }
);

// Index for user-scoped, chronological queries
NotificationSchema.index({ userId: 1, createdAt: -1 });

module.exports = mongoose.model('Notification', NotificationSchema);
