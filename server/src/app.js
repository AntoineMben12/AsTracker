const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const { errorHandler } = require('./middleware/errorHandler');

// Route imports
const authRoutes = require('./routes/auth');
const assignmentRoutes = require('./routes/assignments');
const notificationRoutes = require('./routes/notifications');
const profileRoutes = require('./routes/profile');

const app = express();

// ── Global Middleware ──────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

if (process.env.NODE_ENV === 'development') {
    app.use(morgan('dev'));
}

// ── Health Check ───────────────────────────────────────────────────────────────
app.get('/api/health', (req, res) => {
    res.status(200).json({ success: true, message: 'AsTracker API is running' });
});

// ── Routes ─────────────────────────────────────────────────────────────────────
app.use('/api/auth', authRoutes);
app.use('/api/assignments', assignmentRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api/profile', profileRoutes);

// ── 404 Handler ────────────────────────────────────────────────────────────────
app.use((req, res) => {
    res.status(404).json({ success: false, error: `Route ${req.originalUrl} not found` });
});

// ── Global Error Handler ───────────────────────────────────────────────────────
app.use(errorHandler);

module.exports = app;
