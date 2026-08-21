require('dotenv').config();
const express = require('express');
const path = require('path');
const { randomUUID } = require('crypto');
const { queries, approvePatientTx, purgeOldRecords } = require('./db/sqlite');

const app = express();
const PORT = process.env.PORT || 3000;
const DEEPSEEK_API_KEY = process.env.DEEPSEEK_API_KEY;
const DEEPSEEK_URL = 'https://api.deepseek.com/chat/completions';

app.use(express.json({ limit: '200kb' }));
// 允许跨域请求（CORS）
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    // 处理预检请求（OPTIONS）
    if (req.method === 'OPTIONS') {
        return res.sendStatus(200);
    }
    next();
});
app.use(express.static(__dirname));

app.post('/api/report', async (req, res) => {
  if (!DEEPSEEK_API_KEY) {
    return res.status(500).json({ error: '服务器未配置 DEEPSEEK_API_KEY' });
  }

  const { mode, modeName, durationSec, samples, stats } = req.body || {};

  if (!mode || !stats) {
    return res.status(400).json({ error: '缺少必要的训练数据' });
  }

  const prompt = buildPrompt({ modeName, durationSec, samples, stats });

  try {
    const upstream = await fetch(DEEPSEEK_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${DEEPSEEK_API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-v4-flash',
        messages: [
          {
            role: 'system',
            content: '你是一名专业的运动康复治疗师，擅长根据ACL（前交叉韧带）术后康复训练的传感器数据，给出简明、专业、可执行的康复评估报告。使用中文回答，语气专业且温和。'
          },
          { role: 'user', content: prompt }
        ],
        temperature: 0.5,
        stream: false
      })
    });

    if (!upstream.ok) {
      const errText = await upstream.text();
      console.error('DeepSeek API 错误:', upstream.status, errText);
      return res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
    }

    const data = await upstream.json();
    const content = data?.choices?.[0]?.message?.content || '';
    res.json({ report: content });
  } catch (err) {
    console.error('调用 DeepSeek API 出错:', err);
    res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
  }
});

function buildPrompt({ modeName, durationSec, samples, stats }) {
  return `请基于以下康复训练数据生成一份简明的AI康复评估报告（使用中文，控制在400字以内，分为"训练概况"、"动作评估"、"风险提示"、"康复建议"四个部分）：

【训练动作】${modeName || '未知'}
【本次训练时长】${durationSec != null ? durationSec.toFixed(1) + ' 秒' : '未知'}
【采样点数】${samples || 0}
【统计数据】
- 大腿 Pitch 平均值: ${stats.avgPitch}°，最大值: ${stats.maxPitch}°，最小值: ${stats.minPitch}°
- 大腿 Roll 平均值: ${stats.avgRoll}°，最大值: ${stats.maxRoll}°，最小值: ${stats.minRoll}°
- 膝关节夹角(KED) 平均值: ${stats.avgKed}°
- 标准姿态占比: ${stats.standardRatio}%
- 需调整姿态占比: ${stats.adjustRatio}%
- 危险姿态占比: ${stats.dangerRatio}%

请给出专业、具体、可执行的评估和建议。`;
}

// 加在 app.listen 前面就行
// 通用聊天代理接口，兼容前端现有请求格式
app.post('/api/chat', async (req, res) => {
  if (!DEEPSEEK_API_KEY) {
    return res.status(500).json({ error: '服务器未配置 DEEPSEEK_API_KEY' });
  }
  const { messages, temperature } = req.body || {};
  if (!messages || !Array.isArray(messages)) {
    return res.status(400).json({ error: '缺少messages参数' });
  }
  try {
    const upstream = await fetch(DEEPSEEK_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${DEEPSEEK_API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-v4-flash',
        messages: messages,
        temperature: temperature || 0.5,
        stream: false
      })
    });
    if (!upstream.ok) {
      const errText = await upstream.text();
      console.error('DeepSeek API 错误:', upstream.status, errText);
      return res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
    }
    const data = await upstream.json();
    const content = data?.choices?.[0]?.message?.content || '';
    res.json({ choices: [{ message: { content } }] }); // 返回格式和原来的Worker一致，前端不用改解析逻辑
  } catch (err) {
    console.error('调用 DeepSeek API 出错:', err);
    res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
  }
});

// ============ 实时消息 API ============
const sseClients = [];

// SSE 实时推送端点
app.get('/api/messages/stream', (req, res) => {
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

// 获取消息列表
app.get('/api/messages', (req, res) => {
  const { patientId } = req.query;
  const result = patientId ? queries.getMessages.all(patientId) : queries.getAllMessages.all();
  res.json(result);
});

// 发送消息
app.post('/api/messages', (req, res) => {
  const msg = req.body;
  if (!msg || !msg.id) return res.status(400).json({ error: 'invalid' });
  queries.upsertMessage.run({
    id: msg.id,
    from_role: msg.fromRole || '',
    from_name: msg.fromName || '',
    to_patient_id: msg.toPatientId || '',
    type: msg.type || 'text',
    text: msg.text || '',
    time: msg.time || '',
    date: msg.date || '',
    read: msg.read ? 1 : 0,
    recalled: msg.recalled ? 1 : 0,
  });
  sseClients.forEach(c => { try { c.res.write(`data: ${JSON.stringify({ type: 'message', msg })}\n\n`); } catch(_) {} });
  res.json({ ok: true });
});

// 撤回消息
app.patch('/api/messages/:id/recall', (req, res) => {
  const msg = queries.getMessageById.get(req.params.id);
  if (!msg) return res.status(404).json({ error: 'not found' });
  queries.recallMessage.run(req.params.id);
  sseClients.forEach(c => { try { c.res.write(`data: ${JSON.stringify({ type: 'recall', id: req.params.id })}\n\n`); } catch(_) {} });
  res.json({ ok: true });
});

// ============ 认证 & 注册 API ============

// 登录
app.post('/api/auth/login', (req, res) => {
  const { username, password, role } = req.body || {};
  if (!username || !password || !role) return res.status(400).json({ error: '参数不完整' });

  const user = queries.getUserByUsername.get(username);
  if (!user || user.password !== password || user.role !== role) {
    return res.status(401).json({ error: '账号或密码错误' });
  }
  if (user.role === 'patient' && user.status === 'pending') {
    return res.status(403).json({ code: 'pending', message: '等待医生审核，请耐心等待' });
  }
  if (user.role === 'patient' && user.status === 'rejected') {
    return res.status(403).json({ code: 'rejected', message: '注册申请已被拒绝，请联系医生' });
  }

  const fullUser = user.role === 'patient'
    ? queries.getPatientFull.get(user.id)
    : queries.getDoctorFull.get(user.id);

  res.json({ ok: true, user: fullUser });
});

// 注册
app.post('/api/auth/register', (req, res) => {
  const { role, username, password, name, gender } = req.body || {};
  if (!role || !username || !password || !name) {
    return res.status(400).json({ error: '请填写必填项' });
  }
  const existing = queries.getUserByUsername.get(username);
  if (existing) return res.status(409).json({ error: '用户名已存在，请更换' });

  const newId = (role === 'patient' ? 'p' : 'd') + Date.now();
  const insertUser = require('./db/sqlite').db.prepare(
    'INSERT INTO users(id,username,password,role,name,gender,status) VALUES(?,?,?,?,?,?,?)');

  if (role === 'patient') {
    const { age, diagnosis, hospital, doctorId, doctorFreeText } = req.body;
    const doctorName = doctorId
      ? (queries.getUserById.get(doctorId) || {}).name || doctorFreeText || ''
      : (doctorFreeText || '');
    const tx = require('./db/sqlite').db.transaction(() => {
      insertUser.run(newId, username, password, 'patient', name, gender || '男', 'pending');
      require('./db/sqlite').db.prepare(
        'INSERT INTO patient_profiles(user_id,age,diagnosis,hospital,doctor_id,doctor_name) VALUES(?,?,?,?,?,?)'
      ).run(newId, parseInt(age) || null, diagnosis || '', hospital || '',
            doctorId || null, doctorName);
    });
    tx();
    res.json({ ok: true, status: 'pending' });
  } else {
    const { hospital, department, title, speciality } = req.body;
    const tx = require('./db/sqlite').db.transaction(() => {
      insertUser.run(newId, username, password, 'doctor', name, gender || '男', 'active');
      require('./db/sqlite').db.prepare(
        'INSERT INTO doctor_profiles(user_id,hospital,department,title,speciality) VALUES(?,?,?,?,?)'
      ).run(newId, hospital || '', department || '', title || '', speciality || '');
    });
    tx();
    res.json({ ok: true, status: 'active' });
  }
});

// 获取医生列表（注册页下拉用）
app.get('/api/doctors', (req, res) => {
  res.json(queries.listDoctors.all());
});

// 获取待审核患者（医生端）
app.get('/api/patients/pending', (req, res) => {
  res.json(queries.getPendingPatients.all());
});

// 批准患者
app.post('/api/patients/:id/approve', (req, res) => {
  const { doctorId, doctorName } = req.body || {};
  if (!doctorId) return res.status(400).json({ error: '缺少 doctorId' });
  try {
    approvePatientTx(req.params.id, doctorId, doctorName || '');
    res.json({ ok: true });
  } catch(e) {
    res.status(500).json({ error: e.message });
  }
});

// 拒绝患者
app.post('/api/patients/:id/reject', (req, res) => {
  queries.updateUserStatus.run('rejected', req.params.id);
  res.json({ ok: true });
});

app.listen(PORT, () => {
  console.log(`服务已启动: http://localhost:${PORT}`);
  if (!DEEPSEEK_API_KEY) {
    console.warn('警告: 未检测到 DEEPSEEK_API_KEY，AI报告功能将不可用。请在 .env 文件中配置。');
  }
  purgeOldRecords();
  setInterval(purgeOldRecords, 24 * 60 * 60 * 1000);
});

// ============ 患者/医生端 AI 康复报告 ============
app.post('/api/patient-report', async (req, res) => {
  if (!DEEPSEEK_API_KEY) {
    return res.status(500).json({ error: '服务器未配置 DEEPSEEK_API_KEY' });
  }

  const { patientName, gender, age, surgeryDate, doctor, status,
          records, tasks, history, streakDays, totalCheckins } = req.body || {};

  if (!patientName) {
    return res.status(400).json({ error: '缺少患者信息' });
  }

  const prompt = buildPatientPrompt(req.body);

  try {
    const upstream = await fetch(DEEPSEEK_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${DEEPSEEK_API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-v4-flash',
        messages: [
          {
            role: 'system',
            content: '你是一名专业的运动康复治疗师，擅长根据ACL（前交叉韧带）术后康复训练数据和患者打卡记录，给出简明、专业、个性化的康复评估报告。使用中文回答，语气专业且温和鼓励。'
          },
          { role: 'user', content: prompt }
        ],
        temperature: 0.5,
        stream: false
      })
    });

    if (!upstream.ok) {
      const errText = await upstream.text();
      console.error('DeepSeek API 错误:', upstream.status, errText);
      return res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
    }

    const data = await upstream.json();
    const content = data?.choices?.[0]?.message?.content || '';
    res.json({ report: content });
  } catch (err) {
    console.error('调用 DeepSeek API 出错:', err);
    res.status(502).json({ error: 'AI 服务调用失败，请稍后重试' });
  }
});

function buildPatientPrompt(data) {
  const { patientName, gender, age, surgeryDate, doctor, status,
          records, tasks, history, streakDays, totalCheckins } = data;

  const recordSummary = (records || []).slice(0, 10).map(r =>
    `  ${r.time} | ${r.action} | 最大角度${r.pitch}° | 异常${r.abnormal}次 | ${r.status}`
  ).join('\n') || '  暂无记录';

  const taskSummary = (tasks || []).map(t =>
    `  ${t.name} × ${t.count}${t.unit} ${t.done ? '(已完成)' : '(未完成)'}`
  ).join('\n') || '  暂无任务';

  const recentHistory = (history || []).slice(-7).map(h =>
    `  ${h.date}: ${h.done ? '已打卡' : '未完成'}`
  ).join('\n') || '  暂无打卡记录';

  return `请基于以下患者康复数据生成一份AI康复评估报告（中文，500字以内，分为"患者概况"、"训练表现"、"康复依从性"、"风险提示"、"下一步建议"五个部分）：

【患者信息】
- 姓名: ${patientName}，${gender || ''}，${age || ''}岁
- 手术日期: ${surgeryDate || '未知'}
- 主治医生: ${doctor || '未知'}
- 当前状态: ${status || '康复中'}

【训练数据（近期动作记录）】
${recordSummary}

【今日任务完成情况】
${taskSummary}

【打卡统计】
- 累计打卡天数: ${totalCheckins || 0} 天
- 当前连续打卡: ${streakDays || 0} 天

【近7天打卡记录】
${recentHistory}

请综合训练角度数据和康复依从性，给出专业、具体、鼓励性的评估和建议。`;
}
