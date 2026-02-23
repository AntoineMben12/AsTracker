const errorHandler = (err, req, res) => {
    let error = { ...err };
    error.message = err.message;

    // Log to console in development
    if (process.env.NODE_ENV === 'development') {
        console.error(err);
    }

    // Mongoose bad ObjectId
    if (err.name === 'CastError') {
        error.message = `Resource not found with id ${err.value}`;
        return res.status(404).json({ success: false, error: error.message });
    }

    // Mongoose duplicate key
    if (err.code === 11000) {
        const field = Object.keys(err.keyValue).join(', ');
        error.message = `Duplicate field value: ${field}. Please use another value.`;
        return res.status(400).json({ success: false, error: error.message });
    }

    // Mongoose validation error
    if (err.name === 'ValidationError') {
        error.message = Object.values(err.errors).map((e) => e.message).join(', ');
        return res.status(400).json({ success: false, error: error.message });
    }

    res.status(err.statusCode || 500).json({
        success: false,
        error: error.message || 'Server Error',
    });
};

module.exports = { errorHandler };
