'use strict';
const Database = require('better-sqlite3');
const path = require('path');
const { randomUUID } = require('crypto');

const DB_PATH = path.join(__dirname, '..', 'rehab.db');
const db = new Database(DB_PATH);

db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

// ---- Prepared statements (created after initSchema) ----
let queries;

function buildQueries() {
    queries = {
        getUserByUsername: db.prepare('SELECT * FROM users WHERE username=?'),
        getUserById:       db.prepare('SELECT * FROM users WHERE id=?'),
        updateUserStatus:  db.prepare('UPDATE users SET status=? WHERE id=?'),

        getPatientFull: db.prepare(`
            SELECT u.id, u.username, u.password, u.role, u.name, u.gender, u.status,
                   pp.age, pp.phone, pp.emergency_contact, pp.surgery_date, pp.notes,
                   pp.diagnosis, pp.hospital, pp.doctor_id, pp.doctor_name
            FROM users u LEFT JOIN patient_profiles pp ON u.id=pp.user_id WHERE u.id=?`),
        getDoctorFull: db.prepare(`
            SELECT u.id, u.username, u.password, u.role, u.name, u.gender, u.status,
                   dp.hospital, dp.department, dp.title, dp.speciality, dp.bio, dp.phone
            FROM users u LEFT JOIN doctor_profiles dp ON u.id=dp.user_id WHERE u.id=?`),

        getPendingPatients: db.prepare(`
            SELECT u.id, u.name, u.gender, u.status,
                   pp.age, pp.diagnosis, pp.hospital, pp.doctor_name, pp.doctor_id,
                   u.created_at
            FROM users u JOIN patient_profiles pp ON u.id=pp.user_id
            WHERE u.role='patient' AND u.status='pending' ORDER BY u.created_at ASC`),

        getApprovedPatients: db.prepare(`
            SELECT u.id, u.name, u.gender, u.status,
                   pp.age, pp.phone, pp.surgery_date, pp.notes, pp.diagnosis,
                   pp.hospital, pp.doctor_id, pp.doctor_name
            FROM users u JOIN patient_profiles pp ON u.id=pp.user_id
            WHERE u.role='patient' AND u.status='approved'`),

        listDoctors: db.prepare(`
            SELECT u.id, u.name, dp.hospital, dp.department, dp.title
            FROM users u JOIN doctor_profiles dp ON u.id=dp.user_id
            WHERE u.role='doctor' AND u.status='active' ORDER BY u.name ASC`),

        assignDoctorToPatient: db.prepare(
            'UPDATE patient_profiles SET doctor_id=?, doctor_name=? WHERE user_id=?'),

        getMessages:    db.prepare('SELECT * FROM messages WHERE to_patient_id=? ORDER BY created_at ASC'),
        getAllMessages:  db.prepare('SELECT * FROM messages ORDER BY created_at ASC'),
        upsertMessage:  db.prepare(`
            INSERT INTO messages(id,from_role,from_name,to_patient_id,type,text,time,date,read,recalled)
            VALUES(@id,@from_role,@from_name,@to_patient_id,@type,@text,@time,@date,@read,@recalled)
            ON CONFLICT(id) DO UPDATE SET
                text=excluded.text, recalled=excluded.recalled, read=excluded.read`),
        recallMessage:  db.prepare("UPDATE messages SET recalled=1, text='' WHERE id=?"),
        getMessageById: db.prepare('SELECT * FROM messages WHERE id=?'),

        getTasksByPatient: db.prepare('SELECT * FROM tasks WHERE patient_id=? ORDER BY assigned_at ASC'),

        getRecordsByPatient: db.prepare(
            'SELECT * FROM training_records WHERE patient_id=? ORDER BY recorded_at DESC LIMIT 50'),
        purgeOldRecords: db.prepare(
            'DELETE FROM training_records WHERE recorded_at < ?'),

        getHistoryByPatient: db.prepare(
            'SELECT * FROM checkin_history WHERE patient_id=? ORDER BY date DESC LIMIT 90'),
    };
}

function initSchema() {
    db.exec(`
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('patient','doctor')),
    name TEXT NOT NULL,
    gender TEXT,
    status TEXT NOT NULL DEFAULT 'pending'
        CHECK(status IN ('pending','approved','rejected','active')),
    created_at INTEGER NOT NULL DEFAULT (unixepoch())
);
CREATE TABLE IF NOT EXISTS patient_profiles (
    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    age INTEGER, phone TEXT, emergency_contact TEXT,
    surgery_date TEXT, notes TEXT, diagnosis TEXT, hospital TEXT,
    doctor_id TEXT REFERENCES users(id) ON DELETE SET NULL,
    doctor_name TEXT
);
CREATE TABLE IF NOT EXISTS doctor_profiles (
    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    hospital TEXT, department TEXT, title TEXT, speciality TEXT, bio TEXT, phone TEXT
);
CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    from_role TEXT NOT NULL, from_name TEXT NOT NULL,
    to_patient_id TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'text',
    text TEXT NOT NULL DEFAULT '',
    time TEXT NOT NULL, date TEXT NOT NULL,
    read INTEGER NOT NULL DEFAULT 0,
    recalled INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL DEFAULT (unixepoch())
);
CREATE INDEX IF NOT EXISTS idx_messages_patient ON messages(to_patient_id);
CREATE INDEX IF NOT EXISTS idx_messages_date ON messages(created_at);
CREATE TABLE IF NOT EXISTS tasks (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL, count INTEGER NOT NULL DEFAULT 10,
    unit TEXT NOT NULL DEFAULT '次', key_points TEXT DEFAULT '',
    details TEXT DEFAULT '', done INTEGER NOT NULL DEFAULT 0,
    assigned_at INTEGER NOT NULL DEFAULT (unixepoch())
);
CREATE INDEX IF NOT EXISTS idx_tasks_patient ON tasks(patient_id);
CREATE TABLE IF NOT EXISTS training_records (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    time TEXT NOT NULL, action TEXT NOT NULL,
    pitch REAL NOT NULL DEFAULT 0, abnormal INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT '标准',
    recorded_at INTEGER NOT NULL DEFAULT (unixepoch())
);
CREATE INDEX IF NOT EXISTS idx_records_patient ON training_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_records_date ON training_records(recorded_at);
CREATE TABLE IF NOT EXISTS checkin_history (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 1,
    UNIQUE(patient_id, date)
);
CREATE INDEX IF NOT EXISTS idx_checkin_patient ON checkin_history(patient_id);
`);
}

function seedDemoData() {
    const exists = db.prepare('SELECT id FROM users WHERE username=?').get('zhangsan');
    if (exists) return;

    const insertUser = db.prepare(
        'INSERT INTO users(id,username,password,role,name,gender,status) VALUES(?,?,?,?,?,?,?)');
    const insertPP = db.prepare(`
        INSERT INTO patient_profiles(user_id,age,phone,emergency_contact,surgery_date,notes,diagnosis,hospital,doctor_id,doctor_name)
        VALUES(?,?,?,?,?,?,?,?,?,?)`);
    const insertDP = db.prepare(
        'INSERT INTO doctor_profiles(user_id,hospital,department,title,speciality,bio,phone) VALUES(?,?,?,?,?,?,?)');
    const insertTask = db.prepare(
        'INSERT INTO tasks(id,patient_id,name,count,unit,key_points,details,done) VALUES(?,?,?,?,?,?,?,0)');
    const insertRecord = db.prepare(
        'INSERT INTO training_records(id,patient_id,time,action,pitch,abnormal,status) VALUES(?,?,?,?,?,?,?)');
    const insertCheckin = db.prepare(
        'INSERT OR IGNORE INTO checkin_history(id,patient_id,date,done) VALUES(?,?,?,?)');

    const seed = db.transaction(() => {
        // Demo doctor
        insertUser.run('d1','doctor','123456','doctor','李医生','男','active');
        insertDP.run('d1','北京协和医院','骨科康复科','主任医师','膝关节韧带重建与康复',
            '从事骨科康复工作15年，专注于膝关节运动损伤的手术与术后康复。','010-8888-0001');

        // Demo patients
        const patients = [
            {id:'p1',username:'zhangsan',name:'张明',gender:'男',age:28,phone:'138-0001-0001',
             ec:'张父 138-0002-0002',sd:'2025-12-10',notes:'左膝前交叉韧带重建术后康复',diag:'左膝前交叉韧带重建',hosp:'北京协和医院',
             tasks:[['直腿抬高',10,'次','保持膝关节伸直','仰卧位，腿抬高45°保持5秒'],
                    ['靠墙静蹲',30,'秒','膝关节不超过脚尖','背靠墙，屈膝90°']],
             records:[['15:05:22','直腿抬高',42,0,'标准'],['15:07:45','单腿支撑',35,1,'纠正中']],
             history:['2026-06-22','2026-06-21','2026-06-19','2026-06-18','2026-06-17']},
            {id:'p2',username:'wangli',name:'王莉',gender:'女',age:34,phone:'139-0002-0002',
             ec:'王先生 139-0003-0003',sd:'2026-01-15',notes:'右膝后交叉韧带修复术后',diag:'右膝后交叉韧带修复',hosp:'北京协和医院',
             tasks:[['直腿抬高',15,'次','保持膝关节伸直',''],['单腿支撑',15,'次','保持平衡','']],
             records:[['14:20:10','直腿抬高',48,0,'标准'],['14:22:30','单腿支撑',28,2,'纠正中']],
             history:['2026-06-22']},
            {id:'p3',username:'lihua',name:'李华',gender:'男',age:45,phone:'137-0003-0003',
             ec:'',sd:'2025-11-20',notes:'双膝半月板修复',diag:'双膝半月板修复',hosp:'北京协和医院',
             tasks:[['靠墙静蹲',45,'秒','',''],['蚌式开合',20,'次','','']],
             records:[['16:05:00','靠墙静蹲',52,0,'标准']],
             history:[]},
            {id:'p4',username:'zhaoxue',name:'赵雪',gender:'女',age:31,phone:'136-0004-0004',
             ec:'',sd:'2026-03-01',notes:'',diag:'',hosp:'北京协和医院',
             tasks:[['直腿抬高',12,'次','',''],['靠墙静蹲',30,'秒','','']],
             records:[],history:[]},
            {id:'p5',username:'chenhao',name:'陈浩',gender:'男',age:26,phone:'135-0005-0005',
             ec:'',sd:'2026-02-14',notes:'左膝前交叉韧带重建，运动员',diag:'左膝前交叉韧带重建',hosp:'北京协和医院',
             tasks:[['单腿支撑',20,'次','',''],['蚌式开合',25,'次','','']],
             records:[['10:30:15','单腿支撑',40,0,'标准'],['10:33:00','蚌式开合',30,0,'标准']],
             history:['2026-06-22','2026-06-21','2026-06-20','2026-06-19']},
        ];

        for (const p of patients) {
            insertUser.run(p.id, p.username, '123456', 'patient', p.name, p.gender, 'approved');
            insertPP.run(p.id, p.age, p.phone, p.ec, p.sd, p.notes, p.diag, p.hosp, 'd1', '李医生');
            for (const t of p.tasks) {
                insertTask.run(randomUUID(), p.id, t[0], t[1], t[2], t[3], t[4]);
            }
            for (const r of p.records) {
                insertRecord.run(randomUUID(), p.id, r[0], r[1], r[2], r[3], r[4]);
            }
            for (const date of p.history) {
                insertCheckin.run(randomUUID(), p.id, date, 1);
            }
        }
    });

    seed();
}

// Transaction: approve patient → status=approved + assign doctor + default tasks
const approvePatientTx = db.transaction((patientId, doctorId, doctorName) => {
    db.prepare('UPDATE users SET status=? WHERE id=?').run('approved', patientId);
    db.prepare('UPDATE patient_profiles SET doctor_id=?, doctor_name=? WHERE user_id=?')
        .run(doctorId, doctorName, patientId);
    // Add default tasks if patient has none
    const existing = db.prepare('SELECT id FROM tasks WHERE patient_id=?').get(patientId);
    if (!existing) {
        const ins = db.prepare(
            'INSERT INTO tasks(id,patient_id,name,count,unit,key_points,details,done) VALUES(?,?,?,?,?,?,?,0)');
        ins.run(randomUUID(), patientId, '直腿抬高', 10, '次', '保持膝关节伸直', '仰卧位，腿抬高45°保持5秒');
        ins.run(randomUUID(), patientId, '靠墙静蹲', 30, '秒', '膝关节不超过脚尖', '背靠墙，屈膝90°');
    }
});

function purgeOldRecords() {
    const cutoff = Math.floor(Date.now() / 1000) - (30 * 24 * 3600);
    db.prepare('DELETE FROM training_records WHERE recorded_at < ?').run(cutoff);
}

initSchema();
buildQueries();
seedDemoData();

module.exports = { db, queries, approvePatientTx, purgeOldRecords, randomUUID };
