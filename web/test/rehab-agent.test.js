'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const {
  RehabAgent,
  checkSafety,
  summarizeTraining,
  summarizeTasks,
  summarizeAdherence
} = require('../agent/rehab-agent');

test('安全检查能识别需要立即停止训练的表述', () => {
  const result = checkSafety('训练后小腿肿痛，而且无法负重');
  assert.equal(result.flagged, true);
  assert.deepEqual(result.matches, ['无法负重', '小腿肿痛']);
});

test('工具只汇总现有训练结果', () => {
  const result = summarizeTraining([
    { action: '直腿抬高', pitch: 42, abnormal: 1, status: '良好' },
    { action: '直腿抬高', pitch: 38, abnormal: 2, status: '需调整' }
  ]);
  assert.equal(result.count, 2);
  assert.equal(result.abnormalTotal, 3);
  assert.equal(result.recordedPitchAverage, 40);
  assert.equal(result.latest.pitch, 42);
});

test('任务和打卡工具正确保留页面状态', () => {
  const tasks = summarizeTasks([
    { name: '静态站立', done: true },
    { name: '侧抬腿', count: 10, unit: '次', done: false }
  ]);
  const adherence = summarizeAdherence([
    { date: '2026-09-18', done: true },
    { date: '2026-09-19', done: false }
  ], 3);
  assert.equal(tasks.completed, 1);
  assert.equal(tasks.pending[0].name, '侧抬腿');
  assert.deepEqual(adherence, { windowDays: 2, completedDays: 1, streakDays: 3 });
});

test('未配置模型时仍可完整执行自研智能体流程', async () => {
  const agent = new RehabAgent();
  const result = await agent.run({
    patientId: 'P001',
    conversationId: 'test-conversation',
    question: '我今天的任务完成得怎么样？',
    snapshot: {
      recentRecords: [],
      tasks: [{ name: '屈膝训练', count: 10, unit: '次', done: false }],
      recentCheckins: [{ date: '2026-09-19', done: true }],
      streakDays: 1
    }
  });
  assert.equal(result.mode, 'local-rules');
  assert.match(result.answer, /医生任务/);
  assert.ok(result.trace.some(step => step.tool === 'tasks'));
  assert.ok(result.trace.some(step => step.tool === 'safety'));
});
