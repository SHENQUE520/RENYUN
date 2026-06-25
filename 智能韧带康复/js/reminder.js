// ================================================================
//  Reminder System
//  Call initReminders(role) after db.js and utils.js are loaded
// ================================================================

function initReminders(role) {
    const style = document.createElement('style');
    style.textContent = `
    /* ── Reminder dropdown panel (doctor) ── */
    .reminder-wrap{position:relative;display:inline-block;}
    .reminder-btn{
        padding:7px 14px;border:1px solid #e2e8f0;border-radius:30px;
        background:#fff;font-size:13px;color:#64748b;cursor:pointer;
        transition:0.2s;display:inline-flex;align-items:center;gap:5px;
        white-space:nowrap;
    }
    .reminder-btn:hover{background:#f1f5f9;color:#1e293b;}
    .reminder-btn .rbadge{
        background:#2563eb;color:#fff;font-size:10px;font-weight:700;
        padding:1px 5px;border-radius:10px;min-width:16px;text-align:center;display:none;
    }
    .reminder-panel{
        position:fixed;top:72px;right:28px;width:360px;
        background:#fff;border-radius:18px;border:1px solid #e9f0fa;
        box-shadow:0 16px 48px rgba(0,0,0,0.14);z-index:1100;
        display:none;flex-direction:column;overflow:hidden;
        animation:chatSlide 0.2s ease;
    }
    .reminder-panel.open{display:flex;}
    .reminder-panel-head{
        background:linear-gradient(135deg,#1a4b8c,#2563eb);
        color:#fff;padding:14px 18px;display:flex;align-items:center;
        justify-content:space-between;flex-shrink:0;
    }
    .reminder-panel-head .rp-title{font-size:14px;font-weight:600;}
    .reminder-panel-close{
        background:rgba(255,255,255,0.2);border:none;color:#fff;
        width:26px;height:26px;border-radius:50%;cursor:pointer;
        display:flex;align-items:center;justify-content:center;font-size:14px;transition:0.15s;
    }
    .reminder-panel-close:hover{background:rgba(255,255,255,0.35);}
    .reminder-form{padding:16px 18px;border-bottom:1px solid #f1f5f9;flex-shrink:0;}
    .reminder-form-row{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:10px;}
    .reminder-form-row .rf-field label{font-size:12px;font-weight:500;color:#334155;display:block;margin-bottom:4px;}
    .reminder-form-row .rf-field select,
    .reminder-form-row .rf-field input{
        width:100%;padding:8px 11px;border:1.5px solid #e2e8f0;border-radius:9px;
        font-size:13px;font-family:inherit;outline:none;background:#fafcff;transition:0.2s;
    }
    .reminder-form-row .rf-field select:focus,
    .reminder-form-row .rf-field input:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,0.1);}
    .reminder-label-row{margin-bottom:10px;}
    .reminder-label-row label{font-size:12px;font-weight:500;color:#334155;display:block;margin-bottom:4px;}
    .reminder-label-row input{
        width:100%;padding:8px 11px;border:1.5px solid #e2e8f0;border-radius:9px;
        font-size:13px;font-family:inherit;outline:none;background:#fafcff;transition:0.2s;
    }
    .reminder-label-row input:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,0.1);}
    .reminder-add-btn{
        width:100%;padding:9px;background:#2563eb;color:#fff;border:none;
        border-radius:10px;font-size:13px;font-weight:500;cursor:pointer;transition:0.2s;
    }
    .reminder-add-btn:hover{background:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,0.25);}
    .reminder-list{overflow-y:auto;max-height:220px;padding:10px 14px 14px;}
    .reminder-list::-webkit-scrollbar{width:3px;}
    .reminder-empty{color:#94a3b8;font-size:13px;text-align:center;padding:18px 0;}
    .reminder-item{
        background:#f8fbff;border:1px solid #eef2f6;border-radius:12px;
        padding:11px 14px;margin-bottom:8px;display:flex;align-items:center;gap:10px;
    }
    .reminder-item .ri-time{
        font-size:18px;font-weight:700;color:#2563eb;
        min-width:44px;text-align:center;flex-shrink:0;
    }
    .reminder-item .ri-info{flex:1;min-width:0;}
    .reminder-item .ri-name{font-size:13px;font-weight:500;color:#0a2540;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}
    .reminder-item .ri-label{font-size:11px;color:#64748b;margin-top:2px;}
    .ri-toggle{
        position:relative;width:36px;height:20px;flex-shrink:0;cursor:pointer;
    }
    .ri-toggle input{opacity:0;width:0;height:0;position:absolute;}
    .ri-toggle-track{
        position:absolute;inset:0;background:#e2e8f0;border-radius:20px;transition:0.2s;
    }
    .ri-toggle input:checked + .ri-toggle-track{background:#2563eb;}
    .ri-toggle-thumb{
        position:absolute;top:2px;left:2px;width:16px;height:16px;
        background:#fff;border-radius:50%;transition:0.2s;
        box-shadow:0 1px 4px rgba(0,0,0,0.15);
    }
    .ri-toggle input:checked ~ .ri-toggle-thumb{transform:translateX(16px);}
    .ri-del{
        background:none;border:none;color:#94a3b8;cursor:pointer;
        font-size:16px;padding:2px 4px;border-radius:6px;transition:0.15s;flex-shrink:0;
    }
    .ri-del:hover{color:#dc2626;background:#fee2e2;}

    /* ── Patient reminder toast (bigger) ── */
    .reminder-toast{
        position:fixed;top:80px;right:24px;width:300px;
        background:#fff;border-radius:16px;border:1px solid #bfdbfe;
        box-shadow:0 12px 36px rgba(37,99,235,0.18);z-index:2000;
        padding:18px 20px;display:none;animation:chatSlide 0.25s ease;
    }
    .reminder-toast.show{display:block;}
    .reminder-toast-head{display:flex;align-items:center;gap:10px;margin-bottom:8px;}
    .reminder-toast-icon{font-size:24px;}
    .reminder-toast-title{font-size:14px;font-weight:600;color:#0a2540;}
    .reminder-toast-body{font-size:13px;color:#475569;line-height:1.6;}
    .reminder-toast-close{
        position:absolute;top:12px;right:14px;background:none;border:none;
        color:#94a3b8;cursor:pointer;font-size:16px;padding:2px;
    }
    .reminder-toast-close:hover{color:#64748b;}
    `;
    document.head.appendChild(style);

    if (role === 'doctor') {
        initDoctorReminders();
    } else {
        initPatientReminders();
    }
}

// ── Doctor: reminder management panel ────────────────────────────
function initDoctorReminders() {
    // Inject button into header right area
    const headerRight = document.querySelector('.app-header .right');
    if (!headerRight) return;

    const wrap = document.createElement('div');
    wrap.className = 'reminder-wrap';
    wrap.innerHTML = `
        <button class="reminder-btn" id="reminderToggleBtn" onclick="toggleReminderPanel()">
            ⏰ 提醒<span class="rbadge" id="reminderBadge">0</span>
        </button>
        <div class="reminder-panel" id="reminderPanel">
            <div class="reminder-panel-head">
                <span class="rp-title">⏰ 康复提醒设置</span>
                <button class="reminder-panel-close" onclick="toggleReminderPanel()">✕</button>
            </div>
            <div class="reminder-form">
                <div class="reminder-form-row">
                    <div class="rf-field">
                        <label>患者</label>
                        <select id="remPatientSel"></select>
                    </div>
                    <div class="rf-field">
                        <label>提醒时间</label>
                        <input type="time" id="remTime" value="08:00"/>
                    </div>
                </div>
                <div class="reminder-label-row">
                    <label>提醒内容</label>
                    <input type="text" id="remLabel" placeholder="如：记得完成今日康复训练" maxlength="40"/>
                </div>
                <button class="reminder-add-btn" onclick="addReminder()">＋ 添加提醒</button>
            </div>
            <div class="reminder-list" id="reminderList"></div>
        </div>`;

    // Insert before the logout button
    const logoutBtn = headerRight.querySelector('button');
    headerRight.insertBefore(wrap, logoutBtn);

    // Populate patient selector
    const sel = document.getElementById('remPatientSel');
    if (sel) {
        sel.innerHTML = DB.patients.map(p =>
            `<option value="${p.id}">${p.name}</option>`
        ).join('');
    }

    renderReminderList();
    updateReminderBadge();

    window.toggleReminderPanel = function() {
        const panel = document.getElementById('reminderPanel');
        if (!panel) return;
        panel.classList.toggle('open');
    };

    window.addReminder = function() {
        const patientId = document.getElementById('remPatientSel').value;
        const time = document.getElementById('remTime').value;
        const label = document.getElementById('remLabel').value.trim() || '记得完成今日康复训练';
        if (!time) { showToast('请选择提醒时间', 'error'); return; }
        const reminder = {
            id: Date.now() + '_r',
            patientId,
            time,
            label,
            enabled: true,
        };
        DB.reminders.push(reminder);
        saveDB();
        renderReminderList();
        updateReminderBadge();
        document.getElementById('remLabel').value = '';
        const pname = (DB.patients.find(p=>p.id===patientId)||{}).name || '';
        showToast(`已为 ${pname} 设置 ${time} 提醒`, 'success');
    };

    window.deleteReminder = function(id) {
        DB.reminders = DB.reminders.filter(r => r.id !== id);
        saveDB();
        renderReminderList();
        updateReminderBadge();
    };

    window.toggleReminder = function(id, val) {
        const r = DB.reminders.find(x => x.id === id);
        if (r) { r.enabled = val; saveDB(); updateReminderBadge(); }
    };
}

function renderReminderList() {
    const list = document.getElementById('reminderList');
    if (!list) return;
    if (!DB.reminders.length) {
        list.innerHTML = '<div class="reminder-empty">暂无提醒，添加后患者将在指定时间收到通知</div>';
        return;
    }
    list.innerHTML = DB.reminders.map(r => {
        const pname = (DB.patients.find(p=>p.id===r.patientId)||{}).name || r.patientId;
        const uid = 'tog_' + r.id.replace(/[^a-z0-9]/gi,'_');
        return `<div class="reminder-item">
            <div class="ri-time">${r.time}</div>
            <div class="ri-info">
                <div class="ri-name">${pname}</div>
                <div class="ri-label">${r.label}</div>
            </div>
            <label class="ri-toggle" title="${r.enabled?'点击禁用':'点击启用'}">
                <input type="checkbox" id="${uid}" ${r.enabled?'checked':''}
                    onchange="toggleReminder('${r.id}', this.checked)"/>
                <div class="ri-toggle-track"></div>
                <div class="ri-toggle-thumb"></div>
            </label>
            <button class="ri-del" onclick="deleteReminder('${r.id}')" title="删除">×</button>
        </div>`;
    }).join('');
}

function updateReminderBadge() {
    const badge = document.getElementById('reminderBadge');
    if (!badge) return;
    const count = DB.reminders.filter(r=>r.enabled).length;
    badge.textContent = count;
    badge.style.display = count > 0 ? 'inline-block' : 'none';
}

// ── Patient: receive reminders ────────────────────────────────────
function initPatientReminders() {
    // Build reminder toast element
    const toast = document.createElement('div');
    toast.className = 'reminder-toast';
    toast.id = 'reminderToast';
    toast.innerHTML = `
        <button class="reminder-toast-close" onclick="closeReminderToast()">✕</button>
        <div class="reminder-toast-head">
            <div class="reminder-toast-icon">⏰</div>
            <div class="reminder-toast-title">康复提醒</div>
        </div>
        <div class="reminder-toast-body" id="reminderToastBody">记得完成今日康复训练！</div>`;
    document.body.appendChild(toast);

    window.closeReminderToast = function() {
        document.getElementById('reminderToast').classList.remove('show');
    };

    const firedKey = 'reminder_fired_' + getTodayStr();
    const firedToday = new Set(JSON.parse(localStorage.getItem(firedKey) || '[]'));

    function checkReminders() {
        const now = new Date();
        const curTime = String(now.getHours()).padStart(2,'0') + ':' + String(now.getMinutes()).padStart(2,'0');
        const pid = DB.currentPatientId;

        // Re-read DB from storage in case doctor updated reminders in another tab
        try {
            const fresh = JSON.parse(localStorage.getItem('rehab_db_v2') || '{}');
            if (fresh.reminders) DB.reminders = fresh.reminders;
        } catch(_) {}

        const matching = DB.reminders.filter(r =>
            r.enabled && r.patientId === pid && r.time === curTime && !firedToday.has(r.id)
        );

        matching.forEach(r => {
            firedToday.add(r.id);
            localStorage.setItem(firedKey, JSON.stringify([...firedToday]));
            fireReminderToast(r.label);
        });
    }

    function fireReminderToast(label) {
        const body = document.getElementById('reminderToastBody');
        if (body) body.textContent = label;
        const toastEl = document.getElementById('reminderToast');
        if (toastEl) {
            toastEl.classList.add('show');
            setTimeout(() => toastEl.classList.remove('show'), 12000);
        }
        // Also showToast for visibility
        showToast('⏰ ' + label, 'info');
        // Browser notification (if permitted)
        if (window.Notification && Notification.permission === 'granted') {
            new Notification('韧云智护 · 康复提醒', { body: label, icon: '' });
        } else if (window.Notification && Notification.permission !== 'denied') {
            Notification.requestPermission();
        }
    }

    // Check every minute
    checkReminders();
    setInterval(checkReminders, 60 * 1000);

    // Listen for cross-tab reminder updates
    window.addEventListener('storage', e => {
        if (e.key !== 'rehab_db_v2') return;
        try {
            const fresh = JSON.parse(e.newValue);
            if (fresh.reminders) DB.reminders = fresh.reminders;
        } catch(_) {}
    });
}
