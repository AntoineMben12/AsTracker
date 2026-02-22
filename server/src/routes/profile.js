const express = require('express');
const router = express.Router();

const {
    getProfile,
    updateProfile,
    changePassword,
} = require('../controllers/profileController');
const { protect } = require('../middleware/auth');

// All routes are protected
router.use(protect);

// GET /api/profile   – get profile + stats
// PUT /api/profile   – update name, major, year, avatarUrl
router.route('/').get(getProfile).put(updateProfile);

// PUT /api/profile/password  – change password
router.put('/password', changePassword);

module.exports = router;
