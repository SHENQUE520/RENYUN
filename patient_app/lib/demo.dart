class Posture {
  const Posture(this.feedback, this.state, this.correct);
  final String feedback;
  final String state;
  final bool correct;
}

class SampleSeries {
  const SampleSeries(this.pitch, this.roll, this.ked);
  final List<double> pitch;
  final List<double> roll;
  final List<double> ked;
}

const exercises = [
  ('squat', '靠墙静蹲', '背靠墙壁，缓慢下蹲到大腿接近水平，保持均匀呼吸。'),
  ('stance', '单脚支撑', '单脚站稳，另一脚轻轻离地，核心收紧。'),
  ('sideleg', '侧抬腿训练', '侧卧伸直上方腿，平稳向外抬起，避免腰部借力。'),
  ('legraise', '直腿抬高', '仰卧绷直膝关节，缓慢抬高后停住，再轻轻放下。'),
];

const _series = {
  'p1': SampleSeries(
    [45, 27, 59, 48, 58, 43, 50, 40, 47, 30, 48, 55, 30, 56, 42, 29, 52, 47, 56, 30],
    [2, 3, 1, 4, 2, 3, 2, 1, 3, 2, 4, 2, 3, 1, 2, 3, 2, 4, 1, 3],
    [83, 84, 86, 85, 87, 84, 86, 83, 85, 82, 85, 87, 82, 87, 84, 82, 86, 85, 87, 82],
  ),
  'p2': SampleSeries(
    [32, 34, 36, 38, 39, 37, 35, 33, 31, 30, 32, 35, 38, 39, 37, 34, 31, 30, 33, 36],
    [4, 5, 6, 5, 5, 6, 5, 4, 4, 5, 5, 6, 5, 4, 5, 6, 5, 4, 5, 6],
    [58, 59, 60, 61, 62, 61, 60, 59, 57, 57, 58, 60, 61, 62, 61, 59, 57, 57, 59, 60],
  ),
};

SampleSeries seriesFor(String? patientId) => _series[patientId] ?? _series['p1']!;

String videoUrl(String mode) => '$mode.mp4';

Posture evaluate(String mode, double pitch, double roll) {
  final absPitch = pitch.abs();
  final absRoll = roll.abs();
  if (mode == 'squat') {
    if (absPitch >= 30 && absPitch <= 65) return const Posture('静蹲姿态优秀，请保持均匀呼吸', 'standard', true);
    if (absPitch > 65 && absPitch <= 80) return const Posture('下蹲角度偏深，若膝盖疼痛请稍稍起身', 'adjust', true);
    if (absPitch > 80) return const Posture('危险！蹲姿过深易致韧带受损，请立即起身', 'danger', false);
    return const Posture('请背靠墙壁，下蹲至合理角度', 'neutral', false);
  }
  if (mode == 'legraise') {
    if (absPitch >= 45 && absPitch <= 65) return const Posture('直抬腿动作标准，保持股四头肌紧绷', 'standard', true);
    if (absPitch < 45) return const Posture('抬腿高度过高，易引发腰部代偿，请稍放低', 'danger', false);
    if (absPitch > 70) return const Posture('请绷紧大腿，平稳抬高至合适高度', 'neutral', false);
    return const Posture('继续调整抬腿高度', 'adjust', false);
  }
  if (mode == 'stance') {
    if (absPitch <= 6) return const Posture('核心控制极佳，本体感觉平衡优秀', 'standard', true);
    if (absPitch <= 12) return const Posture('重心轻微摇晃，请微收腹部核心', 'adjust', true);
    return const Posture('重心失控！请手扶椅背保护，安全第一', 'danger', false);
  }
  if (absRoll >= 23 && absRoll <= 45) {
    if (absPitch <= 12) return const Posture('外展角度合格，臀中肌持续发力', 'standard', true);
    return const Posture('身体前后倾斜！请保持躯干侧卧成一直线', 'adjust', false);
  }
  if (absRoll > 45) return const Posture('侧抬过高，请收低角度，防止腰椎借力', 'danger', false);
  return const Posture('伸直膝关节，平稳向外侧方抬起大腿', 'neutral', false);
}
