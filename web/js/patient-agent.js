(function () {
    'use strict';

    let initialized = false;
    let busy = false;
    const conversationKey = 'renyun_patient_agent_conversation';

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function currentPatient() {
        return typeof getCurrentPatient === 'function' ? getCurrentPatient() : null;
    }

    function consecutiveDays(patient) {
        if (!patient || !Array.isArray(patient.history)) return 0;
        const completed = patient.history.filter(item => item.done).map(item => item.date).sort().reverse();
        let cursor = typeof getTodayStr === 'function' ? getTodayStr() : new Date().toISOString().slice(0, 10);
        let count = 0;
        for (const date of completed) {
            if (date !== cursor) {
                if (date < cursor) break;
                continue;
            }
            count += 1;
            const previous = new Date(cursor + 'T00:00:00');
            previous.setDate(previous.getDate() - 1);
            cursor = previous.toISOString().slice(0, 10);
        }
        return count;
    }

    function renderContext() {
        const patient = currentPatient();
        const target = document.getElementById('agentContextStrip');
        if (!patient || !target) return;
        const records = Array.isArray(patient.records) ? patient.records.length : 0;
        const tasks = Array.isArray(patient.tasks) ? patient.tasks.length : 0;
        const streak = consecutiveDays(patient);
        target.innerHTML = `
            <div class="agent-context-item"><i>记录</i><div><strong>${records} 条</strong><small>可查看的训练记录</small></div></div>
            <div class="agent-context-item"><i>任务</i><div><strong>${tasks} 项</strong><small>今日医生任务</small></div></div>
            <div class="agent-context-item"><i>打卡</i><div><strong>${streak} 天</strong><small>连续训练天数</small></div></div>`;
    }

    function resetConversation() {
        const patient = currentPatient();
        const target = document.getElementById('agentMessages');
        if (!target) return;
        const name = patient && patient.name ? escapeHtml(patient.name) : '你好';
        target.innerHTML = `
            <div class="agent-welcome">
                <div class="welcome-mark">答</div>
                <h3>${name}，有哪些训练问题？</h3>
                <p>可以在这里查看近期训练表现、医生任务和异常记录。需要调整训练角度或强度时，请先与医生确认。</p>
            </div>`;
    }

    function appendMessage(role, text, id) {
        const target = document.getElementById('agentMessages');
        if (!target) return null;
        target.querySelector('.agent-welcome')?.remove();
        const row = document.createElement('div');
        row.className = `agent-message ${role}`;
        if (id) row.id = id;
        row.innerHTML = `
            <div class="agent-avatar">${role === 'user' ? '我' : '答'}</div>
            <div class="agent-bubble">${escapeHtml(text)}</div>`;
        target.appendChild(row);
        target.scrollTop = target.scrollHeight;
        return row;
    }

    function appendThinking() {
        const row = appendMessage('assistant thinking', '', 'agentThinking');
        if (row) row.querySelector('.agent-bubble').innerHTML = '正在查看训练记录 <span class="agent-dot"></span><span class="agent-dot"></span><span class="agent-dot"></span>';
    }

    function appendTrace(row, trace, mode) {
        if (!row || !Array.isArray(trace) || !trace.length) return;
        const bubble = row.querySelector('.agent-bubble');
        if (!bubble) return;
        const traceBox = document.createElement('div');
        traceBox.className = 'agent-trace';
        const modeLabel = mode === 'model-assisted' ? '智能整理' : '基础整理';
        traceBox.innerHTML = `
            <div class="agent-trace-title"><span>本次参考信息</span><em>${modeLabel}</em></div>
            <div class="agent-trace-steps">
                ${trace.map(item => `<span><i>✓</i>${escapeHtml(item.label || item.tool)}</span>`).join('')}
            </div>`;
        bubble.appendChild(traceBox);
    }

    function buildSnapshot(patient) {
        return {
            patient: {
                id: patient.id,
                age: patient.age,
                gender: patient.gender,
                surgeryDate: patient.surgeryDate,
                status: patient.status,
                doctor: patient.doctor
            },
            recentRecords: (patient.records || []).slice(0, 10).map(record => ({
                time: record.time,
                action: record.action,
                pitch: record.pitch,
                abnormal: record.abnormal,
                status: record.status
            })),
            tasks: (patient.tasks || []).map(task => ({
                name: task.name,
                count: task.count,
                unit: task.unit,
                keyPoints: task.keyPoints,
                done: Boolean(task.done)
            })),
            recentCheckins: (patient.history || []).slice(-14).map(item => ({
                date: item.date,
                done: Boolean(item.done)
            })),
            streakDays: consecutiveDays(patient)
        };
    }

    function conversationId(patient) {
        const existing = sessionStorage.getItem(conversationKey);
        if (existing) return existing;
        const raw = `patient_${patient.id}_${Date.now().toString(36)}`;
        const value = raw.replace(/[^a-zA-Z0-9_-]/g, '').slice(0, 64);
        sessionStorage.setItem(conversationKey, value);
        return value;
    }

    async function send() {
        if (busy) return;
        const patient = currentPatient();
        const input = document.getElementById('agentQuestion');
        const button = document.getElementById('agentSendBtn');
        if (!patient || !input || !button) return;
        const question = input.value.trim();
        if (!question) return;

        appendMessage('user', question);
        input.value = '';
        input.style.height = 'auto';
        appendThinking();
        busy = true;
        button.disabled = true;

        try {
            const response = await fetch('/api/patient-agent', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    patientId: patient.id,
                    question,
                    conversationId: conversationId(patient),
                    snapshot: buildSnapshot(patient)
                })
            });
            const payload = await response.json().catch(() => ({}));
            if (!response.ok) throw new Error(payload.error || '康复助手暂时无法响应');
            document.getElementById('agentThinking')?.remove();
            const answerRow = appendMessage('assistant', payload.answer || '暂未获得有效回复。');
            appendTrace(answerRow, payload.trace, payload.mode);
        } catch (error) {
            document.getElementById('agentThinking')?.remove();
            appendMessage('assistant', `${error.message || '服务连接失败'}。请稍后重试；如身体不适，请停止训练并联系医生。`);
        } finally {
            busy = false;
            button.disabled = false;
            input.focus();
        }
    }

    function initialize() {
        renderContext();
        if (!initialized) {
            resetConversation();
            const input = document.getElementById('agentQuestion');
            input?.addEventListener('keydown', event => {
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    send();
                }
            });
            input?.addEventListener('input', () => {
                input.style.height = 'auto';
                input.style.height = Math.min(input.scrollHeight, 110) + 'px';
            });
            initialized = true;
        }
    }

    window.openPatientAgent = function (button) {
        if (typeof switchView === 'function') switchView('agent', button);
        initialize();
    };
    window.sendPatientAgentMessage = send;
    window.askPatientAgent = function (question) {
        const input = document.getElementById('agentQuestion');
        if (!input) return;
        input.value = question;
        send();
    };

    document.addEventListener('DOMContentLoaded', initialize);
})();
