import 'package:flutter_test/flutter_test.dart';
import 'package:patient_app/demo.dart';

void main() {
  test('靠墙静蹲在合理角度时判定为标准', () {
    final pose = evaluate('squat', 45, 2);
    expect(pose.state, 'standard');
    expect(pose.correct, isTrue);
  });

  test('靠墙静蹲过深时判定为危险', () {
    final pose = evaluate('squat', 85, 2);
    expect(pose.state, 'danger');
  });
}
