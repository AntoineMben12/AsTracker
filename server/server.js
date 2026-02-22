require('dotenv').config();
const connectDB = require('./src/config/db');
const app = require('./src/app');

const PORT = process.env.PORT || 5000;

// Connect to MongoDB, then start server
connectDB().then(() => {
    app.listen(PORT, () => {
        console.log(
            `🚀 AsTracker server running in ${process.env.NODE_ENV} mode on port ${PORT}`
        );
    });
});
