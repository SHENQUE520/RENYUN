// ================================================================
//  Data Layer — localStorage-backed shared state
// ================================================================
const DB_KEY = 'rehab_db_v2';

const DEFAULT_DB = {
    currentUser: null,
    currentPatientId: 'p1',
    currentDoctorId: 'd1',
    patients: [
        {
            id:'p1',username:'zhangsan',password:'123456',name:'张明',gender:'男',age:28,
            phone:'138-0001-0001',emergencyContact:'张父 138-0002-0002',
            surgeryDate:'2025-12-10',notes:'左膝前交叉韧带重建术后康复',
            doctor:'李医生',status:'康复中',
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
            ],
            monitorData:[],
        },
        {
            id:'p2',username:'wangli',password:'123456',name:'王莉',gender:'女',age:34,
            phone:'139-0002-0002',emergencyContact:'王先生 139-0003-0003',
            surgeryDate:'2026-01-15',notes:'右膝后交叉韧带修复术后',
            doctor:'李医生',status:'康复中',
            history:[{date:'2026-06-22',done:true},{date:'2026-06-21',done:false}],
            tasks:[
                {name:'直腿抬高',count:15,unit:'次',keyPoints:'保持膝关节伸直',details:'',done:false},
                {name:'单腿支撑',count:15,unit:'次',keyPoints:'保持平衡',details:'',done:false},
            ],
            records:[
                {time:'14:20:10',action:'直腿抬高',pitch:48,abnormal:0,status:'标准'},
                {time:'14:22:30',action:'单腿支撑',pitch:28,abnormal:2,status:'纠正中'},
            ],
            monitorData:[],
        },
        {
            id:'p3',username:'lihua',password:'123456',name:'李华',gender:'男',age:45,
            phone:'137-0003-0003',emergencyContact:'',surgeryDate:'2025-11-20',notes:'双膝半月板修复',
            doctor:'李医生',status:'康复中',
            history:[{date:'2026-06-22',done:false}],
            tasks:[
                {name:'靠墙静蹲',count:45,unit:'秒',keyPoints:'',details:'',done:false},
                {name:'蚌式开合',count:20,unit:'次',keyPoints:'',details:'',done:false},
            ],
            records:[{time:'16:05:00',action:'靠墙静蹲',pitch:52,abnormal:0,status:'标准'}],
            monitorData:[],
        },
        {
            id:'p4',username:'zhaoxue',password:'123456',name:'赵雪',gender:'女',age:31,
            phone:'136-0004-0004',emergencyContact:'',surgeryDate:'2026-03-01',notes:'',
            doctor:'李医生',status:'康复中',
            history:[],
            tasks:[
                {name:'直腿抬高',count:12,unit:'次',keyPoints:'',details:'',done:false},
                {name:'靠墙静蹲',count:30,unit:'秒',keyPoints:'',details:'',done:false},
            ],
            records:[],monitorData:[],
        },
        {
            id:'p5',username:'chenhao',password:'123456',name:'陈浩',gender:'男',age:26,
            phone:'135-0005-0005',emergencyContact:'',surgeryDate:'2026-02-14',notes:'左膝前交叉韧带重建，运动员',
            doctor:'李医生',status:'康复中',
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
            ],
            monitorData:[],
        },
    ],
    doctors:[{
        id:'d1',username:'doctor',password:'123456',name:'李医生',gender:'男',
        title:'主任医师',department:'骨科康复科',phone:'010-8888-0001',
        hospital:'北京协和医院',speciality:'膝关节韧带重建与康复',
        bio:'从事骨科康复工作15年，专注于膝关节运动损伤的手术与术后康复。',
    }],
    customActions:[],
    // { id, fromRole:'patient'|'doctor', fromName, toPatientId, text, time, date, read:bool }
    messages:[],
    // { id, patientId, time:'HH:MM', label, enabled:bool }
    reminders:[],
};

function loadDB() {
    try {
        const stored = localStorage.getItem(DB_KEY);
        if (stored) {
            const parsed = JSON.parse(stored);
            return {
                currentUser: parsed.currentUser || null,
                currentPatientId: parsed.currentPatientId || 'p1',
                currentDoctorId: parsed.currentDoctorId || 'd1',
                patients: parsed.patients || DEFAULT_DB.patients,
                doctors: parsed.doctors || DEFAULT_DB.doctors,
                customActions: parsed.customActions || [],
                messages: parsed.messages || [],
                reminders: parsed.reminders || [],
            };
        }
    } catch(e) {}
    return JSON.parse(JSON.stringify(DEFAULT_DB));
}

function saveDB() {
    try { localStorage.setItem(DB_KEY, JSON.stringify(DB)); } catch(e) {}
}

const DB = loadDB();

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

function requireAuth(role) {
    if (!DB.currentUser) { window.location.href = 'login.html'; return false; }
    if (role && DB.currentUser !== role) {
        window.location.href = DB.currentUser === 'patient' ? 'patient.html' : 'doctor.html';
        return false;
    }
    return true;
}
