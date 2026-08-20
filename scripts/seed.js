// ================================================================
//  Seed script — populates MySQL with the demo data that used to
//  live in js/db.js's DEFAULT_DB. Safe to re-run: it wipes the
//  tables it seeds before inserting.
//
//  Usage: npm run db:init   (reads DB_* vars from .env)
// ================================================================
require('dotenv').config();
const bcrypt = require('bcryptjs');
const pool = require('../server/db');

const DOCTORS = [
    { username: 'doctor', password: '123456', name: '李医生', gender: '男',
      title: '主任医师', department: '骨科康复科', phone: '010-8888-0001',
      hospital: '北京协和医院', speciality: '膝关节韧带重建与康复',
      bio: '从事骨科康复工作15年，专注于膝关节运动损伤的手术与术后康复。' },
];

const PATIENTS = [
    { username:'zhangsan', password:'123456', name:'张明', gender:'男', age:28,
      phone:'138-0001-0001', emergencyContact:'张父 138-0002-0002',
      surgeryDate:'2025-12-10', notes:'左膝前交叉韧带重建术后康复',
      doctorUsername:'doctor', status:'康复中',
      history:[
          {date:'2026-06-22',done:true},{date:'2026-06-21',done:true},
          {date:'2026-06-20',done:false},{date:'2026-06-19',done:true},
          {date:'2026-06-18',done:true},{date:'2026-06-17',done:true},
      ],
      tasks:[
          {name:'直腿抬高',count:10,unit:'次',keyPoints:'保持膝关节伸直',details:'仰卧位，腿抬高45°保持5秒',done:false},
          {name:'靠墙静蹲',count:30,unit:'秒',keyPoints:'膝关节不超过脚尖',details:'背靠墙，屈膝90°',done:false},
      ],
      records:[
          {time:'15:05:22',action:'直腿抬高',pitch:42,abnormal:0,status:'标准'},
          {time:'15:07:45',action:'单腿支撑',pitch:35,abnormal:1,status:'纠正中'},
      ] },
    { username:'wangli', password:'123456', name:'王莉', gender:'女', age:34,
      phone:'139-0002-0002', emergencyContact:'王先生 139-0003-0003',
      surgeryDate:'2026-01-15', notes:'右膝后交叉韧带修复术后',
      doctorUsername:'doctor', status:'康复中',
      history:[{date:'2026-06-22',done:true},{date:'2026-06-21',done:false}],
      tasks:[
          {name:'直腿抬高',count:15,unit:'次',keyPoints:'保持膝关节伸直',details:'',done:false},
          {name:'单腿支撑',count:15,unit:'次',keyPoints:'保持平衡',details:'',done:false},
      ],
      records:[
          {time:'14:20:10',action:'直腿抬高',pitch:48,abnormal:0,status:'标准'},
          {time:'14:22:30',action:'单腿支撑',pitch:28,abnormal:2,status:'纠正中'},
      ] },
    { username:'lihua', password:'123456', name:'李华', gender:'男', age:45,
      phone:'137-0003-0003', emergencyContact:'', surgeryDate:'2025-11-20', notes:'双膝半月板修复',
      doctorUsername:'doctor', status:'康复中',
      history:[{date:'2026-06-22',done:false}],
      tasks:[
          {name:'靠墙静蹲',count:45,unit:'秒',keyPoints:'',details:'',done:false},
          {name:'蚌式开合',count:20,unit:'次',keyPoints:'',details:'',done:false},
      ],
      records:[{time:'16:05:00',action:'靠墙静蹲',pitch:52,abnormal:0,status:'标准'}] },
    { username:'zhaoxue', password:'123456', name:'赵雪', gender:'女', age:31,
      phone:'136-0004-0004', emergencyContact:'', surgeryDate:'2026-03-01', notes:'',
      doctorUsername:'doctor', status:'康复中',
      history:[],
      tasks:[
          {name:'直腿抬高',count:12,unit:'次',keyPoints:'',details:'',done:false},
          {name:'靠墙静蹲',count:30,unit:'秒',keyPoints:'',details:'',done:false},
      ],
      records:[] },
    { username:'chenhao', password:'123456', name:'陈浩', gender:'男', age:26,
      phone:'135-0005-0005', emergencyContact:'', surgeryDate:'2026-02-14', notes:'左膝前交叉韧带重建，运动员',
      doctorUsername:'doctor', status:'康复中',
      history:[
          {date:'2026-06-22',done:true},{date:'2026-06-21',done:true},
          {date:'2026-06-20',done:true},{date:'2026-06-19',done:true},
      ],
      tasks:[
          {name:'单腿支撑',count:20,unit:'次',keyPoints:'',details:'',done:false},
          {name:'蚌式开合',count:25,unit:'次',keyPoints:'',details:'',done:false},
      ],
      records:[
          {time:'10:30:15',action:'单腿支撑',pitch:40,abnormal:0,status:'标准'},
          {time:'10:33:00',action:'蚌式开合',pitch:30,abnormal:0,status:'标准'},
      ] },
];

async function main() {
    const conn = await pool.getConnection();
    try {
        console.log('清空现有数据...');
        await conn.query('SET FOREIGN_KEY_CHECKS = 0');
        for (const t of ['monitor_readings', 'reminders', 'messages', 'records', 'checkins', 'tasks', 'patients', 'doctors']) {
            await conn.query(`TRUNCATE TABLE ${t}`);
        }
        await conn.query('SET FOREIGN_KEY_CHECKS = 1');

        console.log('写入医生...');
        const doctorIdByUsername = {};
        for (const d of DOCTORS) {
            const hash = await bcrypt.hash(d.password, 10);
            const [result] = await conn.query(
                `INSERT INTO doctors (username, password_hash, name, gender, title, department, phone, hospital, speciality, bio)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                [d.username, hash, d.name, d.gender, d.title, d.department, d.phone, d.hospital, d.speciality, d.bio]
            );
            doctorIdByUsername[d.username] = result.insertId;
        }

        console.log('写入患者及关联数据...');
        for (const p of PATIENTS) {
            const hash = await bcrypt.hash(p.password, 10);
            const doctorId = doctorIdByUsername[p.doctorUsername] || null;
            const [result] = await conn.query(
                `INSERT INTO patients (username, password_hash, name, gender, age, phone, emergency_contact, surgery_date, notes, doctor_id, status)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                [p.username, hash, p.name, p.gender, p.age, p.phone, p.emergencyContact, p.surgeryDate || null, p.notes, doctorId, p.status]
            );
            const patientId = result.insertId;

            for (const t of p.tasks) {
                await conn.query(
                    `INSERT INTO tasks (patient_id, name, count, unit, key_points, details, done) VALUES (?, ?, ?, ?, ?, ?, ?)`,
                    [patientId, t.name, t.count, t.unit, t.keyPoints || null, t.details || null, t.done ? 1 : 0]
                );
            }
            for (const h of p.history) {
                await conn.query(
                    `INSERT INTO checkins (patient_id, checkin_date, done) VALUES (?, ?, ?)`,
                    [patientId, h.date, h.done ? 1 : 0]
                );
            }
            for (const r of p.records) {
                await conn.query(
                    `INSERT INTO records (patient_id, action, pitch, abnormal, status, recorded_at) VALUES (?, ?, ?, ?, ?, CURDATE())`,
                    [patientId, r.action, r.pitch, r.abnormal, r.status]
                );
            }
        }

        console.log('种子数据写入完成。');
    } finally {
        conn.release();
        await pool.end();
    }
}

main().catch(err => {
    console.error('种子脚本执行失败:', err);
    process.exit(1);
});
