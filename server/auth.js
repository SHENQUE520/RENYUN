// ================================================================
//  Auth helpers — password hashing + JWT issue/verify middleware
// ================================================================
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET;
const JWT_EXPIRES_IN = '7d';

function signToken(payload) {
    return jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });
}

// Express middleware: verifies Authorization: Bearer <token>, attaches req.user = {role, id}
function requireAuth(role) {
    return (req, res, next) => {
        const header = req.headers.authorization || '';
        const token = header.startsWith('Bearer ') ? header.slice(7) : (req.query.token || null);
        if (!token) return res.status(401).json({ error: '未登录' });
        try {
            const decoded = jwt.verify(token, JWT_SECRET);
            if (role && decoded.role !== role) return res.status(403).json({ error: '无权限访问' });
            req.user = decoded;
            next();
        } catch (err) {
            return res.status(401).json({ error: '登录已过期，请重新登录' });
        }
    };
}

module.exports = { signToken, requireAuth };
