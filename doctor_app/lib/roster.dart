class TrainRecord {
  const TrainRecord(this.time, this.action, this.pitch, this.abnormal, this.status);
  final String time;
  final String action;
  final int pitch;
  final int abnormal;
  final String status;
}

class Checkin {
  const Checkin(this.date, this.done);
  final String date;
  final bool done;
}

class ClinicPatient {
  const ClinicPatient({
    required this.id,
    required this.name,
    required this.gender,
    required this.age,
    this.diagnosis = '',
    this.hospital = '北京协和医院',
    this.surgeryDate = '',
    this.notes = '',
    this.phone = '',
    this.records = const [],
    this.history = const [],
  });

  final String id;
  final String name;
  final String gender;
  final int age;
  final String diagnosis;
  final String hospital;
  final String surgeryDate;
  final String notes;
  final String phone;
  final List<TrainRecord> records;
  final List<Checkin> history;

  String get headline => diagnosis.isEmpty ? '康复中' : diagnosis;
}

class TaskPreset {
  const TaskPreset(this.name, this.count, this.unit, this.keyPoints, this.details);
  final String name;
  final int count;
  final String unit;
  final String keyPoints;
  final String details;
}

class RehabScores {
  const RehabScores({required this.quality, required this.stability, required this.compliance, required this.progress});
  final int quality;
  final int stability;
  final int compliance;
  final int progress;
}

const presets = [
  TaskPreset('直腿抬高', 10, '次', '保持膝关节完全伸直', '仰卧位，下肢伸直抬高约45°，保持5秒后缓慢放下。'),
  TaskPreset('靠墙静蹲', 30, '秒', '膝关节不超过脚尖', '背靠墙面，屈膝约90°，腰背贴墙，膝关节不内扣。'),
  TaskPreset('单腿支撑', 15, '次', '保持骨盆水平', '单脚站立，支撑腿微屈，控制5～10秒后换腿。'),
  TaskPreset('蚌式开合', 20, '次', '保持骨盆稳定不旋转', '侧卧，双脚并拢，上侧膝盖向上打开后缓慢回落。'),
  TaskPreset('踝泵运动', 20, '次', '足背充分屈伸', '踝关节最大幅度背伸及跖屈，各保持3秒。'),
  TaskPreset('股四头肌等长收缩', 15, '次', '保持股四头肌紧绷', '仰卧伸腿，主动收紧大腿前方，膝关节压向床面，保持5秒。'),
];

const roster = [
  ClinicPatient(
    id: 'p1',
    name: '张明',
    gender: '男',
    age: 28,
    phone: '138-0001-0001',
    surgeryDate: '2025-12-10',
    diagnosis: '左膝前交叉韧带重建',
    notes: '左膝前交叉韧带重建术后康复',
    records: [
      TrainRecord('09:12:05', '直腿抬高', 44, 0, '标准'),
      TrainRecord('09:18:30', '靠墙静蹲', 68, 0, '标准'),
      TrainRecord('09:25:10', '踝泵运动', 32, 1, '纠正中'),
      TrainRecord('15:05:22', '直腿抬高', 42, 0, '标准'),
      TrainRecord('15:07:45', '单腿支撑', 35, 1, '纠正中'),
    ],
    history: [
      Checkin('2026-08-23', false),
      Checkin('2026-08-22', true),
      Checkin('2026-08-21', true),
      Checkin('2026-08-20', true),
      Checkin('2026-08-19', false),
      Checkin('2026-08-18', true),
      Checkin('2026-08-17', true),
      Checkin('2026-08-16', true),
      Checkin('2026-08-15', true),
      Checkin('2026-08-14', false),
      Checkin('2026-08-13', true),
      Checkin('2026-08-12', true),
      Checkin('2026-08-11', true),
    ],
  ),
  ClinicPatient(
    id: 'p2',
    name: '王莉',
    gender: '女',
    age: 34,
    phone: '139-0002-0002',
    surgeryDate: '2026-01-15',
    diagnosis: '右膝后交叉韧带修复',
    notes: '右膝后交叉韧带修复术后',
    records: [
      TrainRecord('10:15:00', '直腿抬高', 48, 0, '标准'),
      TrainRecord('10:20:30', '单腿支撑', 28, 2, '纠正中'),
      TrainRecord('14:22:30', '蚌式开合', 38, 0, '标准'),
    ],
    history: [
      Checkin('2026-08-23', false),
      Checkin('2026-08-22', true),
      Checkin('2026-08-21', false),
      Checkin('2026-08-20', true),
      Checkin('2026-08-19', true),
      Checkin('2026-08-18', true),
      Checkin('2026-08-17', false),
    ],
  ),
  ClinicPatient(
    id: 'p3',
    name: '李华',
    gender: '男',
    age: 45,
    phone: '137-0003-0003',
    surgeryDate: '2025-11-20',
    diagnosis: '双膝半月板修复',
    notes: '双膝半月板修复',
    records: [
      TrainRecord('08:30:00', '靠墙静蹲', 52, 0, '标准'),
      TrainRecord('16:05:00', '蚌式开合', 35, 1, '纠正中'),
    ],
    history: [
      Checkin('2026-08-23', false),
      Checkin('2026-08-22', false),
      Checkin('2026-08-21', true),
      Checkin('2026-08-20', true),
      Checkin('2026-08-19', true),
    ],
  ),
  ClinicPatient(
    id: 'p4',
    name: '赵雪',
    gender: '女',
    age: 31,
    phone: '136-0004-0004',
    surgeryDate: '2026-03-01',
    records: [TrainRecord('11:00:00', '直腿抬高', 40, 0, '标准')],
    history: [
      Checkin('2026-08-23', false),
      Checkin('2026-08-22', true),
      Checkin('2026-08-21', true),
    ],
  ),
  ClinicPatient(
    id: 'p5',
    name: '陈浩',
    gender: '男',
    age: 26,
    phone: '135-0005-0005',
    surgeryDate: '2026-02-14',
    diagnosis: '左膝前交叉韧带重建',
    notes: '左膝前交叉韧带重建，运动员',
    records: [
      TrainRecord('10:30:15', '单腿支撑', 40, 0, '标准'),
      TrainRecord('10:33:00', '蚌式开合', 30, 0, '标准'),
    ],
    history: [
      Checkin('2026-08-23', false),
      Checkin('2026-08-22', true),
      Checkin('2026-08-21', true),
      Checkin('2026-08-20', true),
      Checkin('2026-08-19', true),
      Checkin('2026-08-18', true),
      Checkin('2026-08-17', true),
      Checkin('2026-08-16', true),
      Checkin('2026-08-15', false),
    ],
  ),
];

int warnCount(ClinicPatient patient) => patient.records.where((r) => r.abnormal > 0).length;

RehabScores scoreOf(ClinicPatient patient, {required int doneTasks, required int totalTasks}) {
  final total = patient.records.isEmpty ? 1 : patient.records.length;
  final standard = patient.records.where((r) => r.status == '标准').length;
  final quality = (standard / total * 100).round().clamp(0, 100);
  final unstable = patient.records.where((r) => r.abnormal > 0).length;
  final stability = (100 - unstable / total * 100).round().clamp(0, 100);
  final days = patient.history.length < 7 ? 7 : patient.history.length;
  final compliance = (patient.history.where((h) => h.done).length / days * 100).round().clamp(0, 100);
  final goal = totalTasks < 1 ? 1 : totalTasks;
  final progress = (doneTasks / goal * 100).round().clamp(0, 100);
  return RehabScores(quality: quality, stability: stability, compliance: compliance, progress: progress);
}

List<double> demoWave(String id, {required double base}) {
  return List<double>.generate(24, (i) {
    final swing = (i % 6 - 2.5) * (id == 'p2' ? 2.2 : 3.4);
    return base + swing;
  });
}
