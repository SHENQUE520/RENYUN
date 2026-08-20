// ================================================================
//  /api/doctors — doctor profile
// ================================================================
const express = require('express');
const pool = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();

router.get('/:id', requireAuth(), async (req, res) => {
    try {
        const [[doctor]] = await pool.query(
            `SELECT id, name, gender, title, department, phone, hospital, speciality, bio
             FROM doctors WHERE id = ? LIMIT 1`,
            [req.params.id]
        );
        if (!doctor) return res.status(404).json({ error: '医生不存在' });
        res.json(doctor);
    } catch (err) {
        console.error('获取医生信息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

router.patch('/:id', requireAuth('doctor'), async (req, res) => {
    if (String(req.user.id) !== String(req.params.id)) return res.status(403).json({ error: '无权限访问' });
    const { name, gender, phone, title, department, hospital, speciality, bio } = req.body || {};
    try {
        await pool.query(
            `UPDATE doctors SET
                name = COALESCE(?, name), gender = ?, phone = ?, title = ?,
                department = ?, hospital = ?, speciality = ?, bio = ?
             WHERE id = ?`,
            [name || null, gender || null, phone || null, title || null, department || null, hospital || null, speciality || null, bio || null, req.params.id]
        );
        const [[doctor]] = await pool.query(
            `SELECT id, name, gender, title, department, phone, hospital, speciality, bio FROM doctors WHERE id = ?`,
            [req.params.id]
        );
        res.json(doctor);
    } catch (err) {
        console.error('更新医生信息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

module.exports = router;
