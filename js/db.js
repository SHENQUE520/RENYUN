// ================================================================
//  Data Layer — REST API client backed by MySQL on the server.
//  Keeps the same function names the pages already call
//  (getCurrentPatient, getCurrentDoctor, requireAuth, etc.) so page
//  scripts only need to await the bootstrap functions once.
// ================================================================
const AUTH_KEY = 'rehab_auth_v1';

const DB = {
    patients: [],
    doctors: [],
    currentPatientId: null,
    currentDoctorId: null,
};

function getAuth() {
    try { return JSON.parse(localStorage.getItem(AUTH_KEY) || 'null'); } catch (e) { return null; }
}

function setAuth(auth) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
}

function clearAuth() {
    localStorage.removeItem(AUTH_KEY);
}

async function apiFetch(path, opts = {}) {
    const auth = getAuth();
    const headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
    if (auth && auth.token) headers.Authorization = 'Bearer ' + auth.token;
    const res = await fetch(path, Object.assign({}, opts, { headers }));
    if (res.status === 401) {
        clearAuth();
        window.location.href = 'login.html';
        throw new Error('未登录');
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || '请求失败');
    return data;
}

// ── Auth actions ─────────────────────────────────────────
async function login(role, username, password) {
    const data = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ role, username, password }),
    });
    setAuth(data);
    if (role === 'patient') DB.currentPatientId = data.id;
    else DB.currentDoctorId = data.id;
    return data;
}

function logout() {
    clearAuth();
    window.location.href = 'login.html';
}

function requireAuth(role) {
    const auth = getAuth();
    if (!auth) { window.location.href = 'login.html'; return false; }
    if (role && auth.role !== role) {
        window.location.href = auth.role === 'patient' ? 'patient.html' : 'doctor.html';
        return false;
    }
    if (auth.role === 'patient') DB.currentPatientId = auth.id;
    else DB.currentDoctorId = auth.id;
    return true;
}

// ── Bootstrap: fetch current user's data into the DB cache ──
async function bootstrapPatient() {
    const auth = getAuth();
    const patient = await apiFetch(`/api/patients/${auth.id}`);
    patient.id = auth.id;
    DB.patients = [patient];
    DB.currentPatientId = auth.id;
    return patient;
}

async function bootstrapDoctor() {
    const auth = getAuth();
    const [patients, doctor] = await Promise.all([
        apiFetch('/api/patients'),
        apiFetch(`/api/doctors/${auth.id}`),
    ]);
    DB.patients = patients;
    DB.doctors = [doctor];
    doctor.id = auth.id;
    DB.currentDoctorId = auth.id;
    if (!DB.currentPatientId && patients.length) DB.currentPatientId = patients[0].id;
    return { patients, doctor };
}

async function refreshPatient(id) {
    const patient = await apiFetch(`/api/patients/${id}`);
    patient.id = id;
    const i = DB.patients.findIndex(p => p.id === id);
    if (i !== -1) DB.patients[i] = patient; else DB.patients.push(patient);
    return patient;
}

// ── Patient actions ──────────────────────────────────────
async function checkinPatient(id) {
    const patient = await apiFetch(`/api/patients/${id}/checkin`, { method: 'POST' });
    patient.id = id;
    const i = DB.patients.findIndex(p => p.id === id);
    if (i !== -1) DB.patients[i] = patient;
    return patient;
}

async function markTaskDone(patientId, taskId) {
    const patient = await apiFetch(`/api/patients/${patientId}/tasks/${taskId}/done`, { method: 'PATCH' });
    patient.id = patientId;
    const i = DB.patients.findIndex(p => p.id === patientId);
    if (i !== -1) DB.patients[i] = patient;
    return patient;
}

async function assignTasks(patientId, tasks) {
    const patient = await apiFetch(`/api/patients/${patientId}/tasks`, {
        method: 'POST',
        body: JSON.stringify({ tasks }),
    });
    patient.id = patientId;
    const i = DB.patients.findIndex(p => p.id === patientId);
    if (i !== -1) DB.patients[i] = patient;
    return patient;
}

async function saveProfile(role, id, fields) {
    const path = role === 'patient' ? `/api/patients/${id}` : `/api/doctors/${id}`;
    const updated = await apiFetch(path, { method: 'PATCH', body: JSON.stringify(fields) });
    updated.id = id;
    if (role === 'patient') {
        const i = DB.patients.findIndex(p => p.id === id);
        if (i !== -1) DB.patients[i] = Object.assign(DB.patients[i], updated);
    } else {
        const i = DB.doctors.findIndex(d => d.id === id);
        if (i !== -1) DB.doctors[i] = Object.assign(DB.doctors[i], updated);
    }
    return updated;
}

async function sendTelemetry(patientId, sample) {
    try { await apiFetch(`/api/patients/${patientId}/telemetry`, { method: 'POST', body: JSON.stringify(sample) }); }
    catch (e) { /* best-effort, don't interrupt live monitoring on network hiccups */ }
}

// ── Reminders ────────────────────────────────────────────
async function fetchReminders(patientId) {
    return apiFetch(`/api/patients/${patientId}/reminders`);
}
async function addReminderApi(patientId, time, label) {
    return apiFetch(`/api/patients/${patientId}/reminders`, { method: 'POST', body: JSON.stringify({ time, label }) });
}
async function delReminderApi(patientId, reminderId) {
    return apiFetch(`/api/patients/${patientId}/reminders/${reminderId}`, { method: 'DELETE' });
}

// ── Sync accessors (read from in-memory cache) ───────────
function getPatient(id) { return DB.patients.find(p => p.id === id); }
function getCurrentPatient() { return getPatient(DB.currentPatientId); }
function getCurrentDoctor() { return DB.doctors.find(d => d.id === DB.currentDoctorId) || DB.doctors[0]; }

function getTodayStr() {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0');
}

function formatDate(s) {
    const [y,m,d] = s.split('-');
    return `${y}/${m}/${d}`;
}

function getStatusClass(status) {
    if (status === '标准') return 'badge-green';
    if (status === '纠正中') return 'badge-yellow';
    return 'badge-red';
}
