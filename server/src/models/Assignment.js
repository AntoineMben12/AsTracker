const mongoose = require('mongoose');

const SubtaskSchema = new mongoose.Schema(
    {
        title: {
            type: String,
            required: true,
            trim: true,
        },
        checked: {
            type: Boolean,
            default: false,
        },
    },
    { _id: true }
);

const AssignmentSchema = new mongoose.Schema(
    {
        userId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User',
            required: true,
        },
        title: {
            type: String,
            required: [true, 'Please add a title'],
            trim: true,
            maxlength: [200, 'Title cannot be more than 200 characters'],
        },
        description: {
            type: String,
            default: '',
            maxlength: [2000, 'Description cannot be more than 2000 characters'],
        },
        subject: {
            type: String,
            required: [true, 'Please add a subject'],
            trim: true,
        },
        dueDate: {
            type: Date,
            required: [true, 'Please add a due date'],
        },
        priority: {
            type: String,
            enum: ['Low', 'Medium', 'High'],
            default: 'Medium',
        },
        status: {
            type: String,
            enum: ['pending', 'completed', 'overdue'],
            default: 'pending',
        },
        subtasks: {
            type: [SubtaskSchema],
            default: [],
        },
        progress: {
            type: Number,
            min: 0,
            max: 100,
            default: 0,
        },
        tags: {
            type: [String],
            default: [],
        },
        type: {
            type: String,
            default: 'Assignment', // e.g. "Essay", "Lab Report", "Quiz"
        },
    },
    {
        timestamps: true,
        toJSON: { virtuals: true },
        toObject: { virtuals: true },
    }
);

// Auto-compute progress from subtasks whenever the document is saved
AssignmentSchema.pre('save', function (next) {
    if (this.subtasks && this.subtasks.length > 0) {
        const done = this.subtasks.filter((s) => s.checked).length;
        this.progress = Math.round((done / this.subtasks.length) * 100);
    }
    // Auto-mark overdue
    if (this.status === 'pending' && this.dueDate < new Date()) {
        this.status = 'overdue';
    }
    next();
});

// Index for efficient user-scoped queries
AssignmentSchema.index({ userId: 1, status: 1 });
AssignmentSchema.index({ userId: 1, dueDate: 1 });

module.exports = mongoose.model('Assignment', AssignmentSchema);
