// ================================================================
//  /api/patients — patient CRUD, tasks, checkins, records
// ================================================================
const express = require('express');
const pool = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();

function randomPitch() { return Math.round(30 + Math.random() * 25); }

async function loadPatient(id) {
    const [[patient]] = await pool.query(
        `SELECT p.id, p.name, p.gender, p.age, p.phone, p.emergency_contact AS emergencyContact,
                p.surgery_date AS surgeryDate, p.notes, p.status, d.name AS doctor
         FROM patients p LEFT JOIN doctors d ON d.id = p.doctor_id
         WHERE p.id = ? LIMIT 1`,
        [id]
    );
    if (!patient) return null;

    const [tasks] = await pool.query(
        `SELECT id, name, count, unit, key_points AS keyPoints, details, done FROM tasks WHERE patient_id = ? ORDER BY id`,
        [id]
    );
    const [history] = await pool.query(
        `SELECT checkin_date AS date, done FROM checkins WHERE patient_id = ? ORDER BY checkin_date DESC`,
        [id]
    );
    const [records] = await pool.query(
        `SELECT id, TIME_FORMAT(recorded_at, '%H:%i:%s') AS time, action, pitch, abnormal, status
         FROM records WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 30`,
        [id]
    );

    patient.tasks = tasks.map(t => ({ ...t, done: !!t.done }));
    patient.history = history.map(h => ({ date: h.date, done: !!h.done }));
    patient.records = records;
    return patient;
}

function canAccessPatient(req, patientId) {
    if (req.user.role === 'doctor') return true;
    return req.user.role === 'patient' && String(req.user.id) === String(patientId);
}

// GET /api/patients — doctor only, full list with nested tasks/history/records
router.get('/', requireAuth('doctor'), async (req, res) => {
    try {
        const [ids] = await pool.query(`SELECT id FROM patients ORDER BY id`);
        const patients = await Promise.all(ids.map(r => loadPatient(r.id)));
        res.json(patients.filter(Boolean));
    } catch (err) {
        console.error('获取患者列表失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// GET /api/patients/:id — patient self or any doctor
router.get('/:id', requireAuth(), async (req, res) => {
    if (!canAccessPatient(req, req.params.id)) return res.status(403).json({ error: '无权限访问' });
    try {
        const patient = await loadPatient(req.params.id);
        if (!patient) return res.status(404).json({ error: '患者不存在' });
        res.json(patient);
    } catch (err) {
        console.error('获取患者信息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// PATCH /api/patients/:id — patient self only, profile edit
router.patch('/:id', requireAuth('patient'), async (req, res) => {
    if (String(req.user.id) !== String(req.params.id)) return res.status(403).json({ error: '无权限访问' });
    const { name, gender, age, phone, emergencyContact, surgeryDate, notes } = req.body || {};
    try {
        await pool.query(
            `UPDATE patients SET
                name = COALESCE(?, name), gender = COALESCE(?, gender), age = COALESCE(?, age),
                phone = ?, emergency_contact = ?, surgery_date = ?, notes = ?
             WHERE id = ?`,
            [name || null, gender || null, age || null, phone || null, emergencyContact || null, surgeryDate || null, notes || null, req.params.id]
        );
        const patient = await loadPatient(req.params.id);
        res.json(patient);
    } catch (err) {
        console.error('更新患者信息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// POST /api/patients/:id/tasks — doctor only, upsert one or more tasks, auto-log a record per task
router.post('/:id/tasks', requireAuth('doctor'), async (req, res) => {
    const { tasks } = req.body || {};
    if (!Array.isArray(tasks) || !tasks.length) return res.status(400).json({ error: '缺少任务数据' });

    const conn = await pool.getConnection();
    try {
        await conn.beginTransaction();
        const ts = new Date().toTimeString().slice(0, 8);
        for (const t of tasks) {
            if (!t.name) continue;
            await conn.query(
                `INSERT INTO tasks (patient_id, name, count, unit, key_points, details, done)
                 VALUES (?, ?, ?, ?, ?, ?, 0)
                 ON DUPLICATE KEY UPDATE count = VALUES(count), unit = VALUES(unit),
                    key_points = VALUES(key_points), details = VALUES(details), done = 0`,
                [req.params.id, t.name, t.count || 1, t.unit || '次', t.keyPoints || null, t.details || null]
            );
            await conn.query(
                `INSERT INTO records (patient_id, action, pitch, abnormal, status, recorded_at)
                 VALUES (?, ?, ?, 0, '标准', CONCAT(CURDATE(), ' ', ?))`,
                [req.params.id, t.name, randomPitch(), ts]
            );
        }
        await conn.commit();
        const patient = await loadPatient(req.params.id);
        res.json(patient);
    } catch (err) {
        await conn.rollback();
        console.error('发布任务失败:', err);
        res.status(500).json({ error: '服务器错误' });
    } finally {
        conn.release();
    }
});

// POST /api/patients/:id/checkin — patient self only
router.post('/:id/checkin', requireAuth('patient'), async (req, res) => {
    if (String(req.user.id) !== String(req.params.id)) return res.status(403).json({ error: '无权限访问' });

    const conn = await pool.getConnection();
    try {
        const [[existing]] = await conn.query(
            `SELECT id FROM checkins WHERE patient_id = ? AND checkin_date = CURDATE()`,
            [req.params.id]
        );
        if (existing) return res.status(400).json({ error: '今日已打卡' });

        const [tasks] = await conn.query(`SELECT done FROM tasks WHERE patient_id = ?`, [req.params.id]);
        if (!tasks.length || tasks.some(t => !t.done)) {
            return res.status(400).json({ error: '请先完成所有康复任务' });
        }

        await conn.beginTransaction();
        await conn.query(`INSERT INTO checkins (patient_id, checkin_date, done) VALUES (?, CURDATE(), 1)`, [req.params.id]);
        await conn.query(`UPDATE tasks SET done = 0 WHERE patient_id = ?`, [req.params.id]);
        await conn.commit();

        const patient = await loadPatient(req.params.id);
        res.json(patient);
    } catch (err) {
        await conn.rollback();
        console.error('打卡失败:', err);
        res.status(500).json({ error: '服务器错误' });
    } finally {
        conn.release();
    }
});

// PATCH /api/patients/:id/tasks/:taskId/done — patient self only, mark one task complete
router.patch('/:id/tasks/:taskId/done', requireAuth('patient'), async (req, res) => {
    if (String(req.user.id) !== String(req.params.id)) return res.status(403).json({ error: '无权限访问' });
    try {
        await pool.query(`UPDATE tasks SET done = 1 WHERE id = ? AND patient_id = ?`, [req.params.taskId, req.params.id]);
        const patient = await loadPatient(req.params.id);
        res.json(patient);
    } catch (err) {
        console.error('更新任务状态失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// POST /api/patients/:id/telemetry — either role tied to the patient, records raw sensor sample
router.post('/:id/telemetry', requireAuth(), async (req, res) => {
    if (!canAccessPatient(req, req.params.id)) return res.status(403).json({ error: '无权限访问' });
    const { mode, pitch, roll, ked } = req.body || {};
    try {
        await pool.query(
            `INSERT INTO monitor_readings (patient_id, mode, pitch, roll, ked) VALUES (?, ?, ?, ?, ?)`,
            [req.params.id, mode || null, pitch ?? null, roll ?? null, ked ?? null]
        );
        res.json({ ok: true });
    } catch (err) {
        console.error('写入传感器数据失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// GET /api/patients/:id/reminders
router.get('/:id/reminders', requireAuth(), async (req, res) => {
    if (!canAccessPatient(req, req.params.id)) return res.status(403).json({ error: '无权限访问' });
    try {
        const [rows] = await pool.query(
            `SELECT id, remind_time AS time, label, enabled FROM reminders WHERE patient_id = ? ORDER BY remind_time`,
            [req.params.id]
        );
        res.json(rows.map(r => ({ ...r, enabled: !!r.enabled })));
    } catch (err) {
        console.error('获取提醒失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// POST /api/patients/:id/reminders
router.post('/:id/reminders', requireAuth(), async (req, res) => {
    if (!canAccessPatient(req, req.params.id)) return res.status(403).json({ error: '无权限访问' });
    const { time, label } = req.body || {};
    if (!time) return res.status(400).json({ error: '缺少提醒时间' });
    try {
        const [result] = await pool.query(
            `INSERT INTO reminders (patient_id, remind_time, label, enabled) VALUES (?, ?, ?, 1)`,
            [req.params.id, time, label || '记得完成今日康复训练']
        );
        res.json({ id: result.insertId, time, label: label || '记得完成今日康复训练', enabled: true });
    } catch (err) {
        console.error('新增提醒失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// DELETE /api/patients/:id/reminders/:reminderId
router.delete('/:id/reminders/:reminderId', requireAuth(), async (req, res) => {
    if (!canAccessPatient(req, req.params.id)) return res.status(403).json({ error: '无权限访问' });
    try {
        await pool.query(`DELETE FROM reminders WHERE id = ? AND patient_id = ?`, [req.params.reminderId, req.params.id]);
        res.json({ ok: true });
    } catch (err) {
        console.error('删除提醒失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

module.exports = router;
