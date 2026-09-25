import 'package:doctor_app/roster.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('demo roster matches the doctor workstation patients', () {
    expect(roster, hasLength(5));
    expect(roster.first.name, '张明');
    expect(warnCount(roster.first), 2);
    expect(warnCount(roster[3]), 0);

    final scores = scoreOf(roster.first, doneTasks: 2, totalTasks: 4);
    expect(scores.quality, 60);
    expect(scores.stability, 60);
    expect(scores.progress, 50);
  });
}
