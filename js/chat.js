// ================================================================
//  Chat Widget — floating chat window for patient ↔ doctor
//  Features: text, emoji picker, image upload, doctor reminders
//  Call initChat(role) after db.js and utils.js are loaded
// ================================================================

function initChat(role) {
    const style = document.createElement('style');
    style.textContent = `
    .chat-fab{
        position:fixed;bottom:28px;right:28px;width:54px;height:54px;
        border-radius:50%;background:#2563eb;color:#fff;border:none;
        font-size:24px;cursor:pointer;z-index:1200;
        box-shadow:0 6px 24px rgba(37,99,235,0.40);
        display:flex;align-items:center;justify-content:center;
        transition:transform 0.25s,background 0.25s;
    }
    .chat-fab:hover{background:#1d4ed8;transform:scale(1.08);}
    .chat-unread-dot{
        position:absolute;top:-3px;right:-3px;background:#ef4444;color:#fff;
        font-size:10px;font-weight:700;min-width:18px;height:18px;
        border-radius:20px;align-items:center;justify-content:center;
        padding:0 4px;border:2px solid #fff;display:none;
    }
    .chat-panel{
        position:fixed;bottom:94px;right:28px;width:340px;height:480px;
        background:#fff;border-radius:20px;
        box-shadow:0 20px 56px rgba(0,0,0,0.18);
        z-index:1199;display:none;flex-direction:column;overflow:hidden;
        border:1px solid #e2e8f0;
    }
    .chat-panel.open{display:flex;animation:chatSlide 0.22s ease;}
    @keyframes chatSlide{from{opacity:0;transform:translateY(14px);}to{opacity:1;transform:translateY(0);}}
    .chat-head{
        background:linear-gradient(135deg,#1a4b8c,#2563eb);
        color:#fff;padding:13px 16px 11px;flex-shrink:0;
    }
    .chat-head-top{display:flex;align-items:center;justify-content:space-between;}
    .chat-head-title{font-size:14px;font-weight:600;letter-spacing:0.2px;}
    .chat-head-sub{font-size:11px;opacity:0.72;margin-top:2px;}
    .chat-close-btn{
        background:rgba(255,255,255,0.18);border:none;color:#fff;
        width:26px;height:26px;border-radius:50%;cursor:pointer;font-size:15px;
        display:flex;align-items:center;justify-content:center;transition:0.15s;flex-shrink:0;
    }
    .chat-close-btn:hover{background:rgba(255,255,255,0.32);}
    .chat-sel-bar{
        padding:8px 12px;border-bottom:1px solid #f1f5f9;flex-shrink:0;background:#fafcff;
    }
    .chat-sel-bar select{
        width:100%;padding:6px 10px;border:1.5px solid #e2e8f0;border-radius:8px;
        font-size:13px;background:#fff;outline:none;color:#1e293b;font-family:inherit;
    }
    .chat-msgs{
        flex:1;overflow-y:auto;padding:12px 13px;display:flex;
        flex-direction:column;gap:8px;background:#f5f9ff;
    }
    .chat-msgs::-webkit-scrollbar{width:3px;}
    .chat-msgs::-webkit-scrollbar-thumb{background:#c7d8f0;border-radius:3px;}
    .msg-row{display:flex;flex-direction:column;max-width:82%;}
    .msg-row.mine{align-self:flex-end;align-items:flex-end;}
    .msg-row.theirs{align-self:flex-start;align-items:flex-start;}
    .msg-sender{font-size:10px;color:#94a3b8;margin-bottom:2px;padding:0 3px;}
    .msg-bubble{
        padding:8px 12px;border-radius:14px;font-size:13px;line-height:1.55;
        word-break:break-word;max-width:100%;
    }
    .msg-row.mine .msg-bubble{
        background:#2563eb;color:#fff;border-bottom-right-radius:4px;
    }
    .msg-row.theirs .msg-bubble{
        background:#fff;color:#1e293b;border:1px solid #e9f0fa;
        border-bottom-left-radius:4px;box-shadow:0 1px 4px rgba(0,0,0,0.04);
    }
    .msg-bubble img{max-width:180px;max-height:160px;border-radius:8px;display:block;margin-top:4px;}
    .msg-meta{font-size:10px;color:#94a3b8;margin-top:3px;padding:0 3px;}
    .chat-empty-state{
        flex:1;display:flex;flex-direction:column;align-items:center;
        justify-content:center;color:#94a3b8;font-size:13px;gap:6px;
    }
    .chat-empty-icon{font-size:34px;opacity:0.35;}
    /* Emoji picker */
    .emoji-picker{
        position:absolute;bottom:56px;left:0;right:0;
        background:#fff;border-top:1px solid #f1f5f9;
        padding:8px 10px;display:none;flex-wrap:wrap;gap:4px;
        max-height:110px;overflow-y:auto;z-index:10;
    }
    .emoji-picker.open{display:flex;}
    .emoji-btn{
        font-size:20px;background:none;border:none;cursor:pointer;
        padding:3px;border-radius:6px;transition:0.12s;line-height:1;
    }
    .emoji-btn:hover{background:#f1f5f9;transform:scale(1.2);}
    /* Reminder section inside chat (doctor only) */
    .chat-reminder-bar{
        padding:8px 12px;border-top:1px solid #f1f5f9;flex-shrink:0;
        background:#f8fbff;display:none;
    }
    .chat-reminder-bar.visible{display:block;}
    .rem-row{display:flex;align-items:center;gap:8px;margin-bottom:6px;flex-wrap:wrap;}
    .rem-row label{font-size:11px;color:#64748b;flex-shrink:0;}
    .rem-row input[type=time]{
        padding:4px 8px;border:1.5px solid #e2e8f0;border-radius:8px;
        font-size:12px;outline:none;background:#fff;font-family:inherit;
    }
    .rem-row input[type=text]{
        flex:1;min-width:80px;padding:4px 8px;border:1.5px solid #e2e8f0;
        border-radius:8px;font-size:12px;outline:none;font-family:inherit;
    }
    .rem-row input:focus{border-color:#2563eb;}
    .rem-set-btn{
        padding:4px 12px;background:#2563eb;color:#fff;border:none;
        border-radius:8px;font-size:12px;cursor:pointer;white-space:nowrap;transition:0.2s;
    }
    .rem-set-btn:hover{background:#1d4ed8;}
    .rem-list{display:flex;flex-direction:column;gap:4px;max-height:80px;overflow-y:auto;}
    .rem-item{
        display:flex;align-items:center;gap:8px;font-size:11px;
        background:#fff;border:1px solid #e9f0fa;border-radius:8px;padding:4px 9px;
    }
    .rem-item .rt{font-weight:600;color:#2563eb;flex-shrink:0;}
    .rem-item .rl{flex:1;color:#475569;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
    .rem-item .rd{background:none;border:none;color:#94a3b8;cursor:pointer;font-size:13px;flex-shrink:0;}
    .rem-item .rd:hover{color:#dc2626;}
    /* Input toolbar */
    .chat-toolbar{
        display:flex;align-items:center;gap:6px;padding:6px 12px 4px;
        flex-shrink:0;border-top:1px solid #f1f5f9;background:#fff;position:relative;
    }
    .toolbar-btn{
        background:none;border:none;font-size:18px;cursor:pointer;
        padding:4px;border-radius:8px;color:#64748b;transition:0.15s;line-height:1;
    }
    .toolbar-btn:hover{background:#f1f5f9;color:#2563eb;}
    .chat-input-row{
        display:flex;align-items:flex-end;gap:8px;padding:6px 12px 10px;
        flex-shrink:0;background:#fff;
    }
    .chat-input{
        flex:1;padding:8px 12px;border:1.5px solid #e2e8f0;border-radius:12px;
        font-size:13px;font-family:inherit;outline:none;resize:none;
        max-height:80px;line-height:1.45;transition:0.2s;background:#fafcff;
    }
    .chat-input:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,0.1);background:#fff;}
    .chat-send-btn{
        width:36px;height:36px;border-radius:50%;background:#2563eb;color:#fff;
        border:none;cursor:pointer;font-size:16px;display:flex;align-items:center;
        justify-content:center;transition:0.2s;flex-shrink:0;
    }
    .chat-send-btn:hover{background:#1d4ed8;transform:scale(1.07);}
    .img-input{display:none;}
    `;
    document.head.appendChild(style);

    const EMOJIS = ['😊','😄','👍','❤️','🙏','💪','🏃','🦿','✅','⚡','😮','🥲','🤗','😅','👏','🎉','💯','🔥','⏰','📋','🩺','💊','🏥'];

    // Build panel HTML
    const selBar = role === 'doctor' ? `<div class="chat-sel-bar"><select id="chatPatSel" onchange="_chatSwitchPat(this.value)"></select></div>` : '';
    const reminderBar = role === 'doctor' ? `
        <div class="chat-reminder-bar" id="chatReminderBar">
            <div class="rem-row">
                <label>提醒时间</label>
                <input type="time" id="remTimeIn" value="08:00"/>
                <input type="text" id="remLabelIn" placeholder="提醒内容…"/>
                <button class="rem-set-btn" onclick="_addReminder()">添加</button>
            </div>
            <div class="rem-list" id="remList"></div>
        </div>` : '';

    const panelHTML = `
        <div class="chat-head">
            <div class="chat-head-top">
                <span class="chat-head-title" id="chatTitle">${role==='patient'?'联系医生':'患者对话'}</span>
                <div style="display:flex;gap:6px;align-items:center;">
                    ${role==='doctor'?'<button class="chat-close-btn" title="提醒设置" onclick="_toggleReminderBar()" style="font-size:13px;">⏰</button>':''}
                    <button class="chat-close-btn" onclick="_closeChat()">✕</button>
                </div>
            </div>
            <div class="chat-head-sub" id="chatSub">${role==='patient'?'与主治医生沟通':'选择患者开始对话'}</div>
        </div>
        ${selBar}
        <div class="chat-msgs" id="chatMsgs"></div>
        ${reminderBar}
        <div class="chat-toolbar" id="chatToolbar">
            <button class="toolbar-btn" title="表情" onclick="_toggleEmoji()">😊</button>
            <button class="toolbar-btn" title="发送图片" onclick="document.getElementById('chatImgIn').click()">🖼️</button>
            <input type="file" id="chatImgIn" class="img-input" accept="image/*" onchange="_sendImage(this)"/>
            <span style="flex:1;"></span>
        </div>
        <div class="emoji-picker" id="emojiPicker">${EMOJIS.map(e=>`<button class="emoji-btn" onclick="_insertEmoji('${e}')">${e}</button>`).join('')}</div>
        <div class="chat-input-row">
            <textarea class="chat-input" id="chatInput" placeholder="输入消息… (Enter发送)" rows="1"
                onkeydown="_chatKey(event)" oninput="_autoResize(this)"></textarea>
            <button class="chat-send-btn" onclick="_sendMsg()">➤</button>
        </div>`;

    const fab = document.createElement('button');
    fab.className = 'chat-fab';
    fab.id = 'chatFab';
    fab.title = '聊天';
    fab.innerHTML = '💬<span class="chat-unread-dot" id="chatUnreadDot"></span>';
    fab.onclick = _openChat;

    const panel = document.createElement('div');
    panel.className = 'chat-panel';
    panel.id = 'chatPanel';
    panel.innerHTML = panelHTML;

    document.body.appendChild(fab);
    document.body.appendChild(panel);

    let _activePid = DB.currentPatientId;
    let _emojiOpen = false;

    // Init doctor patient selector
    if (role === 'doctor') {
        const sel = document.getElementById('chatPatSel');
        if (sel) {
            sel.innerHTML = DB.patients.map(p=>
                `<option value="${p.id}"${p.id===_activePid?' selected':''}>${p.name}</option>`
            ).join('');
        }
        _updateDoctorHead(_activePid);
        _renderReminders();
    } else {
        const pt = getCurrentPatient();
        if (pt) {
            document.getElementById('chatTitle').textContent = pt.doctor || '医生';
        }
    }

    _updateUnread();

    // ── Public API ──────────────────────────────────────────
    function _openChat() {
        const p = document.getElementById('chatPanel');
        p.classList.add('open');
        _markRead(_activePid);
        _renderMsgs(_activePid);
        setTimeout(()=>_scrollMsgs(), 40);
        const inp = document.getElementById('chatInput');
        if (inp) inp.focus();
        _updateUnread();
    }

    window._closeChat = function() {
        document.getElementById('chatPanel').classList.remove('open');
        _emojiOpen = false;
        const ep = document.getElementById('emojiPicker');
        if (ep) ep.classList.remove('open');
    };

    window._chatSwitchPat = function(pid) {
        _activePid = pid;
        _markRead(pid);
        _renderMsgs(pid);
        _updateDoctorHead(pid);
        _renderReminders();
        _updateUnread();
    };

    window._sendMsg = function() {
        const inp = document.getElementById('chatInput');
        const txt = inp.value.trim();
        if (!txt) return;
        _pushMsg({ type:'text', text: txt });
        inp.value = '';
        inp.style.height = 'auto';
        _emojiOpen = false;
        const ep = document.getElementById('emojiPicker');
        if (ep) ep.classList.remove('open');
    };

    window._sendImage = function(input) {
        const file = input.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = e => {
            _pushMsg({ type:'image', text: e.target.result });
            input.value = '';
        };
        reader.readAsDataURL(file);
    };

    window._chatKey = function(e) {
        if (e.key==='Enter' && !e.shiftKey) { e.preventDefault(); window._sendMsg(); }
    };

    window._autoResize = function(el) {
        el.style.height = 'auto';
        el.style.height = Math.min(el.scrollHeight, 80) + 'px';
    };

    window._toggleEmoji = function() {
        _emojiOpen = !_emojiOpen;
        document.getElementById('emojiPicker').classList.toggle('open', _emojiOpen);
    };

    window._insertEmoji = function(e) {
        const inp = document.getElementById('chatInput');
        if (!inp) return;
        const s = inp.selectionStart, end = inp.selectionEnd;
        inp.value = inp.value.slice(0,s) + e + inp.value.slice(end);
        inp.selectionStart = inp.selectionEnd = s + e.length;
        inp.focus();
        window._autoResize(inp);
    };

    window._toggleReminderBar = function() {
        const bar = document.getElementById('chatReminderBar');
        if (bar) bar.classList.toggle('visible');
    };

    window._addReminder = function() {
        const t = document.getElementById('remTimeIn').value;
        const lbl = document.getElementById('remLabelIn').value.trim() || '记得完成今日康复训练';
        if (!t) { showToast('请选择提醒时间','error'); return; }
        DB.reminders = DB.reminders || [];
        DB.reminders.push({ id: Date.now()+'_r', patientId: _activePid, time: t, label: lbl, enabled: true });
        saveDB();
        _renderReminders();
        const pname = (DB.patients.find(p=>p.id===_activePid)||{}).name||'';
        showToast(`已为 ${pname} 设置 ${t} 提醒`, 'success');
        document.getElementById('remLabelIn').value = '';
    };

    window._delReminder = function(id) {
        DB.reminders = (DB.reminders||[]).filter(r=>r.id!==id);
        saveDB();
        _renderReminders();
    };

    // ── Internal helpers ────────────────────────────────────
    function _pushMsg(payload) {
        const now = new Date();
        const user = role==='patient' ? getCurrentPatient() : getCurrentDoctor();
        DB.messages = DB.messages || [];
        DB.messages.push({
            id: Date.now()+'_'+Math.random().toString(36).slice(2),
            fromRole: role,
            fromName: user ? user.name : '',
            toPatientId: _activePid,
            type: payload.type || 'text',
            text: payload.text,
            time: String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0'),
            date: getTodayStr(),
            read: false,
        });
        if (DB.messages.length > 600) DB.messages.splice(0, DB.messages.length-600);
        saveDB();
        _renderMsgs(_activePid);
        _scrollMsgs();
    }

    function _renderMsgs(pid) {
        const container = document.getElementById('chatMsgs');
        if (!container) return;
        const msgs = (DB.messages||[]).filter(m=>m.toPatientId===pid);
        if (!msgs.length) {
            container.innerHTML = `<div class="chat-empty-state"><div class="chat-empty-icon">💬</div><div>暂无消息</div></div>`;
            return;
        }
        container.innerHTML = msgs.map(m => {
            const isMine = m.fromRole === role;
            let content = '';
            if (m.type === 'image') {
                content = `<img src="${m.text}" alt="图片"/>`;
            } else {
                content = _esc(m.text);
            }
            return `<div class="msg-row ${isMine?'mine':'theirs'}">
                ${!isMine ? `<div class="msg-sender">${_esc(m.fromName)}</div>` : ''}
                <div class="msg-bubble">${content}</div>
                <div class="msg-meta">${m.time}</div>
            </div>`;
        }).join('');
    }

    function _scrollMsgs() {
        const el = document.getElementById('chatMsgs');
        if (el) el.scrollTop = el.scrollHeight;
    }

    function _markRead(pid) {
        let changed = false;
        (DB.messages||[]).forEach(m => {
            if (m.toPatientId===pid && m.fromRole!==role && !m.read) { m.read=true; changed=true; }
        });
        if (changed) saveDB();
    }

    function _updateUnread() {
        const dot = document.getElementById('chatUnreadDot');
        if (!dot) return;
        let n = 0;
        if (role==='patient') {
            n = (DB.messages||[]).filter(m=>m.toPatientId===DB.currentPatientId&&m.fromRole!=='patient'&&!m.read).length;
        } else {
            n = (DB.messages||[]).filter(m=>m.fromRole==='patient'&&!m.read).length;
        }
        dot.textContent = n > 9 ? '9+' : String(n);
        dot.style.display = n > 0 ? 'flex' : 'none';
    }

    function _updateDoctorHead(pid) {
        const p = DB.patients.find(x=>x.id===pid);
        if (!p) return;
        const t = document.getElementById('chatTitle');
        const s = document.getElementById('chatSub');
        if (t) t.textContent = p.name;
        if (s) s.textContent = p.gender+' · '+p.age+'岁 · '+p.status;
    }

    function _renderReminders() {
        const list = document.getElementById('remList');
        if (!list) return;
        const rems = (DB.reminders||[]).filter(r=>r.patientId===_activePid);
        if (!rems.length) { list.innerHTML = '<div style="font-size:11px;color:#94a3b8;padding:4px 2px;">暂无提醒</div>'; return; }
        list.innerHTML = rems.map(r =>
            `<div class="rem-item">
                <span class="rt">${r.time}</span>
                <span class="rl">${_esc(r.label)}</span>
                <button class="rd" onclick="_delReminder('${r.id}')">×</button>
            </div>`
        ).join('');
    }

    function _esc(s) {
        if (!s) return '';
        return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g,'<br>');
    }

    // Cross-tab storage sync
    window.addEventListener('storage', e => {
        if (e.key !== 'rehab_db_v2') return;
        try {
            const f = JSON.parse(e.newValue);
            DB.messages = f.messages || [];
            DB.reminders = f.reminders || [];
            if (role==='patient') DB.patients = f.patients || DB.patients;
        } catch(_) {}
        _updateUnread();
        if (document.getElementById('chatPanel').classList.contains('open')) {
            _renderMsgs(_activePid);
            _scrollMsgs();
        }
        if (role==='doctor') _renderReminders();
    });

    // Patient reminder checker (every 60s)
    if (role === 'patient') {
        const firedKey = 'rfired_' + getTodayStr();
        const fired = new Set(JSON.parse(localStorage.getItem(firedKey)||'[]'));
        function _checkReminders() {
            try { const f=JSON.parse(localStorage.getItem('rehab_db_v2')||'{}'); if(f.reminders) DB.reminders=f.reminders; } catch(_){}
            const now = new Date();
            const cur = String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0');
            (DB.reminders||[]).filter(r=>r.enabled&&r.patientId===DB.currentPatientId&&r.time===cur&&!fired.has(r.id))
            .forEach(r => {
                fired.add(r.id);
                localStorage.setItem(firedKey, JSON.stringify([...fired]));
                showToast('⏰ '+r.label, 'info');
                // Show a longer persistent reminder banner
                _showReminderBanner(r.label);
            });
        }
        function _showReminderBanner(label) {
            let banner = document.getElementById('remBanner');
            if (!banner) {
                banner = document.createElement('div');
                banner.id = 'remBanner';
                banner.style.cssText='position:fixed;top:72px;right:24px;width:290px;background:#fff;border-radius:16px;border:1px solid #bfdbfe;box-shadow:0 12px 36px rgba(37,99,235,0.18);z-index:2000;padding:16px 18px;display:none;';
                banner.innerHTML=`<button onclick="document.getElementById('remBanner').style.display='none'" style="position:absolute;top:10px;right:12px;background:none;border:none;color:#94a3b8;cursor:pointer;font-size:16px;">✕</button>
                <div style="display:flex;align-items:center;gap:10px;margin-bottom:6px;"><span style="font-size:22px;">⏰</span><span style="font-size:14px;font-weight:600;color:#0a2540;">康复提醒</span></div>
                <div id="remBannerBody" style="font-size:13px;color:#475569;line-height:1.6;"></div>`;
                document.body.appendChild(banner);
            }
            document.getElementById('remBannerBody').textContent = label;
            banner.style.display = 'block';
            setTimeout(() => { banner.style.display = 'none'; }, 15000);
        }
        _checkReminders();
        setInterval(_checkReminders, 60000);
    }
}
