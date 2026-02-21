const jwt = require('jsonwebtoken');
const { body } = require('express-validator');
const User = require('../models/User');

// Utility to generate signed JWT
const signToken = (id) =>
    jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: process.env.JWT_EXPIRE,
    });

// Utility to send token + user in response
const sendTokenResponse = (user, statusCode, res) => {
    const token = signToken(user._id);
    res.status(statusCode).json({
        success: true,
        token,
        user: {
            id: user._id,
            name: user.name,
            email: user.email,
            major: user.major,
            year: user.year,
            avatarUrl: user.avatarUrl,
        },
    });
};

// ── Validators ─────────────────────────────────────────────────────────────────
const registerValidators = [
    body('name').trim().notEmpty().withMessage('Name is required'),
    body('email').isEmail().withMessage('Please provide a valid email'),
    body('password')
        .isLength({ min: 6 })
        .withMessage('Password must be at least 6 characters'),
];

const loginValidators = [
    body('email').isEmail().withMessage('Please provide a valid email'),
    body('password').notEmpty().withMessage('Password is required'),
];

// ── Controllers ────────────────────────────────────────────────────────────────

/**
 * @desc  Register a new user
 * @route POST /api/auth/register
 * @access Public
 */
const register = async (req, res, next) => {
    try {
        const { name, email, password, major, year } = req.body;

        // Check for existing user
        const exists = await User.findOne({ email });
        if (exists) {
            return res
                .status(400)
                .json({ success: false, error: 'Email is already registered' });
        }

        const user = await User.create({ name, email, password, major, year });
        sendTokenResponse(user, 201, res);
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Login user & return token
 * @route POST /api/auth/login
 * @access Public
 */
const login = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        // Find user (include password field explicitly)
        const user = await User.findOne({ email }).select('+password');
        if (!user) {
            return res
                .status(401)
                .json({ success: false, error: 'Invalid credentials' });
        }

        const isMatch = await user.matchPassword(password);
        if (!isMatch) {
            return res
                .status(401)
                .json({ success: false, error: 'Invalid credentials' });
        }

        sendTokenResponse(user, 200, res);
    } catch (err) {
        next(err);
    }
};

/**
 * @desc  Get currently logged-in user
 * @route GET /api/auth/me
 * @access Private
 */
const getMe = async (req, res) => {
    res.status(200).json({
        success: true,
        user: req.user,
    });
};

module.exports = {
    register,
    login,
    getMe,
    registerValidators,
    loginValidators,
};
