// ================================================================
//  POST /api/auth/login
// ================================================================
const express = require('express');
const bcrypt = require('bcryptjs');
const pool = require('../db');
const { signToken } = require('../auth');

const router = express.Router();

router.post('/login', async (req, res) => {
    const { role, username, password } = req.body || {};
    if (!role || !username || !password) {
        return res.status(400).json({ error: '请输入账号和密码' });
    }
    if (role !== 'patient' && role !== 'doctor') {
        return res.status(400).json({ error: '角色参数不合法' });
    }

    const table = role === 'patient' ? 'patients' : 'doctors';
    try {
        const [rows] = await pool.query(
            `SELECT id, username, password_hash, name FROM ${table} WHERE username = ? LIMIT 1`,
            [username]
        );
        const user = rows[0];
        if (!user) return res.status(401).json({ error: '账号或密码错误' });

        const ok = await bcrypt.compare(password, user.password_hash);
        if (!ok) return res.status(401).json({ error: '账号或密码错误' });

        const token = signToken({ role, id: user.id, name: user.name });
        res.json({ token, role, id: user.id, name: user.name });
    } catch (err) {
        console.error('登录失败:', err);
        res.status(500).json({ error: '服务器错误，请稍后重试' });
    }
});

module.exports = router;
