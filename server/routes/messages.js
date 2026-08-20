// ================================================================
//  /api/messages — patient <-> doctor chat, persisted in MySQL,
//  broadcast to connected clients over SSE.
// ================================================================
const express = require('express');
const pool = require('../db');
const { requireAuth } = require('../auth');

const router = express.Router();
const sseClients = [];

function broadcast(payload) {
    const line = `data: ${JSON.stringify(payload)}\n\n`;
    sseClients.forEach(c => { try { c.res.write(line); } catch (_) {} });
}

function rowToMsg(row) {
    return {
        id: row.id,
        fromRole: row.from_role,
        fromName: row.from_name,
        toPatientId: row.to_patient_id,
        type: row.type,
        text: row.recalled ? '' : row.text,
        time: row.msg_time,
        date: row.msg_date,
        read: !!row.is_read,
        recalled: !!row.recalled,
    };
}

// SSE stream
router.get('/stream', requireAuth(), (req, res) => {
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();
    res.write('data: {"type":"connected"}\n\n');
    const client = { id: Date.now() + '_' + Math.random(), res };
    sseClients.push(client);
    req.on('close', () => {
        const i = sseClients.findIndex(c => c.id === client.id);
        if (i !== -1) sseClients.splice(i, 1);
    });
});

// list messages (optionally filtered by patientId)
router.get('/', requireAuth(), async (req, res) => {
    const { patientId } = req.query;
    try {
        const [rows] = patientId
            ? await pool.query(`SELECT * FROM messages WHERE to_patient_id = ? ORDER BY created_at`, [patientId])
            : await pool.query(`SELECT * FROM messages ORDER BY created_at`);
        res.json(rows.map(rowToMsg));
    } catch (err) {
        console.error('获取消息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// send message
router.post('/', requireAuth(), async (req, res) => {
    const msg = req.body;
    if (!msg || !msg.id || !msg.toPatientId) return res.status(400).json({ error: 'invalid' });
    try {
        await pool.query(
            `INSERT INTO messages (id, from_role, from_name, to_patient_id, type, text, msg_time, msg_date, is_read, recalled)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
             ON DUPLICATE KEY UPDATE text = VALUES(text), type = VALUES(type)`,
            [msg.id, msg.fromRole, msg.fromName, msg.toPatientId, msg.type || 'text', msg.text, msg.time, msg.date]
        );
        broadcast({ type: 'message', msg });
        res.json({ ok: true });
    } catch (err) {
        console.error('发送消息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// mark read
router.patch('/:id/read', requireAuth(), async (req, res) => {
    try {
        await pool.query(`UPDATE messages SET is_read = 1 WHERE id = ?`, [req.params.id]);
        res.json({ ok: true });
    } catch (err) {
        console.error('标记已读失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

// recall
router.patch('/:id/recall', requireAuth(), async (req, res) => {
    try {
        const [[msg]] = await pool.query(`SELECT id FROM messages WHERE id = ?`, [req.params.id]);
        if (!msg) return res.status(404).json({ error: 'not found' });
        await pool.query(`UPDATE messages SET recalled = 1, text = '' WHERE id = ?`, [req.params.id]);
        broadcast({ type: 'recall', id: req.params.id });
        res.json({ ok: true });
    } catch (err) {
        console.error('撤回消息失败:', err);
        res.status(500).json({ error: '服务器错误' });
    }
});

module.exports = router;
