'use strict';

const DEFAULT_MODEL_URL = 'https://api.deepseek.com/chat/completions';
const DEFAULT_MODEL_NAME = 'deepseek-flash';
const MAX_HISTORY_TURNS = 4;

const INTENT_LABELS = {
  safety: '安全边界检查',
  training: '读取训练记录',
  tasks: '核对医生任务',
  adherence: '评估训练依从性'
};

class RehabAgent {
  constructor(options = {}) {
    this.model = {
      apiKey: options.modelApiKey || '',
      url: options.modelUrl || DEFAULT_MODEL_URL,
      name: options.modelName || DEFAULT_MODEL_NAME
    };
    this.sessions = new Map();
  }

  async run({ patientId, question, conversationId, snapshot }) {
    const plan = this.plan(question);
    const observations = this.executeTools(plan, question, snapshot);
    const history = this.sessions.get(conversationId) || [];

    let answer;
    let mode = 'local-rules';
    if (this.model.apiKey) {
      try {
        answer = await this.askModel({ question, observations, history });
        mode = 'model-assisted';
      } catch (error) {
        console.error('自研智能体模型调用失败，已切换本地策略:', error.message);
        answer = this.composeLocally(question, observations);
      }
    } else {
      answer = this.composeLocally(question, observations);
    }

    this.remember(conversationId, question, answer);
    return {
      answer,
      mode,
      trace: plan.map(intent => ({
        tool: intent,
        label: INTENT_LABELS[intent],
        status: 'completed'
      }))
    };
  }

  plan(question) {
    const text = String(question || '').toLowerCase();
    const intents = ['safety'];
    if (hasAny(text, ['任务', '今天', '医生', '该练', '怎么练', '安排'])) intents.push('tasks');
    if (hasAny(text, ['打卡', '坚持', '连续', '完成', '依从'])) intents.push('adherence');
    if (hasAny(text, ['记录', '训练', '角度', '异常', '趋势', '进步', '表现', '结果'])) intents.push('training');
    if (intents.length === 1) intents.push('training', 'tasks', 'adherence');
    return [...new Set(intents)];
  }

  executeTools(plan, question, snapshot) {
    const output = {};
    for (const intent of plan) {
      if (intent === 'safety') output.safety = checkSafety(question);
      if (intent === 'training') output.training = summarizeTraining(snapshot.recentRecords);
      if (intent === 'tasks') output.tasks = summarizeTasks(snapshot.tasks);
      if (intent === 'adherence') output.adherence = summarizeAdherence(snapshot.recentCheckins, snapshot.streakDays);
    }
    return output;
  }

  async askModel({ question, observations, history }) {
    const upstream = await fetch(this.model.url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${this.model.apiKey}`
      },
      body: JSON.stringify({
        model: this.model.name,
        temperature: 0.2,
        max_tokens: 800,
        stream: false,
        messages: [
          {
            role: 'system',
            content: [
              '你是“韧小护”，一个由韧云智护团队自研编排的患者端康复数据解释智能体。',
              '只能使用工具返回的结构化事实，不得虚构训练数据。',
              '不诊断疾病，不修改医生处方或训练阈值，不声称可以控制支具。',
              '如安全检查命中风险表述，先建议停止训练并联系医生。',
              '使用简洁中文，先给结论，再说数据依据；数据不足时明确说明。',
              '只输出纯文本分段，不要使用 Markdown 加粗、标题或表格符号。',
              '训练记录中 pitch 数值的单位为度，abnormal 是现有算法产生的异常次数。'
            ].join('\n')
          },
          ...history,
          {
            role: 'user',
            content: `用户问题：${question}\n工具观察结果：${JSON.stringify(observations)}`
          }
        ]
      })
    });

    if (!upstream.ok) {
      const detail = await upstream.text();
      throw new Error(`HTTP ${upstream.status}: ${detail.slice(0, 160)}`);
    }
    const payload = await upstream.json();
    const answer = payload?.choices?.[0]?.message?.content?.trim();
    if (!answer) throw new Error('模型未返回有效内容');
    return answer;
  }

  composeLocally(question, observations) {
    if (observations.safety?.flagged) {
      return [
        '建议你先停止当前训练，不要自行增加角度或强度。',
        `你的描述中包含需要医生确认的信号：${observations.safety.matches.join('、')}。`,
        '请联系主治医生或康复治疗师；如果症状明显、持续加重或影响正常活动，请及时就医。',
        '我只能帮你解释已有训练数据，不能诊断或修改医生处方。'
      ].join('\n\n');
    }

    const sections = [];
    const training = observations.training;
    if (training) {
      if (!training.count) {
        sections.push('【训练记录】目前没有可用的训练记录，暂时无法判断趋势。');
      } else {
        const latest = training.latest;
        sections.push(`【训练记录】已读取 ${training.count} 条记录。最近一次为${latest.action || '未标注动作'}，记录角度 ${formatValue(latest.pitch, '°')}，异常 ${formatValue(latest.abnormal, ' 次')}，页面标记状态为“${latest.status || '未标注'}”。`);
      }
    }

    const tasks = observations.tasks;
    if (tasks) {
      if (!tasks.total) {
        sections.push('【医生任务】今日暂无可用任务，请等待医生安排。');
      } else {
        const pendingText = tasks.pending.length
          ? `待完成：${tasks.pending.map(task => task.name).join('、')}。`
          : '今日任务已全部完成。';
        sections.push(`【医生任务】共 ${tasks.total} 项，已完成 ${tasks.completed} 项。${pendingText}请按原定次数和要点训练，不要自行加量。`);
      }
    }

    const adherence = observations.adherence;
    if (adherence) {
      sections.push(`【训练打卡】近期 ${adherence.windowDays} 天中已完成 ${adherence.completedDays} 天，当前连续 ${adherence.streakDays} 天。${adherence.windowDays ? '继续保持规律记录，便于医生判断恢复趋势。' : '暂时没有足够打卡数据。'}`);
    }

    sections.push(`【回答边界】我已根据你的问题“${question}”检查了页面数据；上述内容是训练信息解释，不是疾病诊断。`);
    return sections.join('\n\n');
  }

  remember(conversationId, question, answer) {
    const history = this.sessions.get(conversationId) || [];
    history.push({ role: 'user', content: question }, { role: 'assistant', content: answer });
    this.sessions.set(conversationId, history.slice(-MAX_HISTORY_TURNS * 2));
    if (this.sessions.size > 300) this.sessions.delete(this.sessions.keys().next().value);
  }
}

function hasAny(text, words) {
  return words.some(word => text.includes(word));
}

function finite(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function formatValue(value, suffix) {
  return value == null ? '未记录' : `${value}${suffix}`;
}

function checkSafety(question) {
  const terms = ['剧痛', '突然疼痛', '严重肿胀', '无法负重', '伤口渗血', '伤口渗液', '发热', '胸痛', '呼吸困难', '晕厥', '小腿肿痛', '膝关节卡住'];
  const matches = terms.filter(term => String(question || '').includes(term));
  return { flagged: matches.length > 0, matches };
}

function summarizeTraining(records = []) {
  const safeRecords = Array.isArray(records) ? records : [];
  const abnormalTotal = safeRecords.reduce((sum, record) => sum + (finite(record.abnormal) || 0), 0);
  const pitches = safeRecords.map(record => finite(record.pitch)).filter(value => value != null);
  return {
    count: safeRecords.length,
    latest: safeRecords[0] || null,
    abnormalTotal,
    recordedPitchAverage: pitches.length
      ? Number((pitches.reduce((sum, value) => sum + value, 0) / pitches.length).toFixed(1))
      : null,
    statusCounts: safeRecords.reduce((counts, record) => {
      const key = record.status || '未标注';
      counts[key] = (counts[key] || 0) + 1;
      return counts;
    }, {})
  };
}

function summarizeTasks(tasks = []) {
  const safeTasks = Array.isArray(tasks) ? tasks : [];
  return {
    total: safeTasks.length,
    completed: safeTasks.filter(task => task.done).length,
    pending: safeTasks.filter(task => !task.done).map(task => ({
      name: task.name || '未命名任务',
      count: finite(task.count),
      unit: task.unit || '',
      keyPoints: task.keyPoints || ''
    }))
  };
}

function summarizeAdherence(checkins = [], streakDays) {
  const safeCheckins = Array.isArray(checkins) ? checkins : [];
  return {
    windowDays: safeCheckins.length,
    completedDays: safeCheckins.filter(item => item.done).length,
    streakDays: finite(streakDays) || 0
  };
}

module.exports = { RehabAgent, checkSafety, summarizeTraining, summarizeTasks, summarizeAdherence };
