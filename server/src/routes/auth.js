const express = require('express');
const router = express.Router();

const {
    register,
    login,
    getMe,
    registerValidators,
    loginValidators,
} = require('../controllers/authController');
const { protect } = require('../middleware/auth');
const { validate } = require('../middleware/validate');

// POST /api/auth/register
router.post('/register', ...registerValidators, validate, register);

// POST /api/auth/login
router.post('/login', ...loginValidators, validate, login);

// GET /api/auth/me  (protected)
router.get('/me', protect, getMe);

module.exports = router;
