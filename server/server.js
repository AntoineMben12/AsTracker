const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

if (!process.env.MONGO_URI) {
    console.error('❌ Error: MONGO_URI is not defined in environment variables.');
    process.exit(1);
}

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
