// ================================================================
//  Shared Utilities
// ================================================================
let _toastTimer = null;

function showToast(msg, type = 'info') {
    let el = document.getElementById('toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast';
        el.className = 'toast';
        document.body.appendChild(el);
    }
    el.textContent = msg;
    el.className = 'toast ' + type + ' show';
    clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => el.classList.remove('show'), 2800);
}

function goTo(page) { window.location.href = page; }

function initHeader(role) {
    const user = role === 'patient' ? getCurrentPatient() : getCurrentDoctor();
    if (!user) return;
    const avatarEl = document.getElementById('headerAvatar');
    const nameEl   = document.getElementById('headerName');
    const refEl    = document.getElementById('headerRef');
    if (avatarEl) avatarEl.textContent = user.name.charAt(0);
    if (nameEl)   nameEl.textContent   = role === 'patient' && user.age ? `${user.name} · ${user.age}岁` : user.name;
    if (refEl) {
        if (role === 'patient') refEl.textContent = '主治：' + user.doctor;
        else refEl.textContent = user.department || '';
    }
}

function logout() {
    DB.currentUser = null;
    saveDB();
    window.location.href = 'login.html';
}

// Expose to window for inline onclick handlers
window.logout = logout;
window.goTo   = goTo;
