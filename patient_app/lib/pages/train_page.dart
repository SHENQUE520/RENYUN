import 'dart:async';

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

import '../demo.dart';
import '../session.dart';
import '../theme.dart';

class TrainPage extends StatefulWidget {
  const TrainPage({super.key, required this.session});
  final Session session;

  @override
  State<TrainPage> createState() => _TrainPageState();
}

class _TrainPageState extends State<TrainPage> {
  String _mode = 'squat';
  bool _running = false;
  int _index = 0;
  double _correctSeconds = 0;
  Timer? _timer;
  final List<FlSpot> _pitch = [];

  SampleSeries get _series => seriesFor(widget.session.id);

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _toggle() {
    if (_running) {
      _timer?.cancel();
      setState(() => _running = false);
      return;
    }
    setState(() => _running = true);
    _timer = Timer.periodic(const Duration(milliseconds: 500), (_) {
      final i = _index % _series.pitch.length;
      final pose = evaluate(_mode, _series.pitch[i], _series.roll[i]);
      setState(() {
        _index = i + 1;
        _correctSeconds = pose.correct ? _correctSeconds + 0.5 : 0;
        _pitch.add(FlSpot(_pitch.length.toDouble(), _series.pitch[i]));
        if (_pitch.length > 24) {
          _pitch.removeAt(0);
          for (var n = 0; n < _pitch.length; n++) {
            _pitch[n] = FlSpot(n.toDouble(), _pitch[n].y);
          }
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final i = _series.pitch.isEmpty ? 0 : (_index == 0 ? 0 : (_index - 1) % _series.pitch.length);
    final pitch = _running ? _series.pitch[i] : 0.0;
    final roll = _running ? _series.roll[i] : 0.0;
    final ked = _running ? _series.ked[i] : 0.0;
    final pose = _running ? evaluate(_mode, pitch, roll) : const Posture('准备好后开始，系统会根据演示曲线给出姿态提示', 'neutral', false);
    final tone = switch (pose.state) {
      'standard' => good,
      'adjust' => warn,
      'danger' => danger,
      _ => const Color(0xFF64748B),
    };

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
      children: [
        const Text('训练', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: [
            for (final item in exercises)
              ChoiceChip(
                label: Text(item.$2),
                selected: _mode == item.$1,
                onSelected: (_) => setState(() {
                  _mode = item.$1;
                  _correctSeconds = 0;
                }),
              ),
          ],
        ),
        const SizedBox(height: 16),
        SoftCard(
          color: tone,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('${_correctSeconds.toStringAsFixed(1)}s', style: const TextStyle(color: Colors.white, fontSize: 42, fontWeight: FontWeight.w700)),
              const SizedBox(height: 6),
              Text(pose.feedback, style: const TextStyle(color: Colors.white, fontSize: 16, height: 1.4)),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            _metric('Pitch', pitch),
            const SizedBox(width: 8),
            _metric('膝关节', ked),
            const SizedBox(width: 8),
            _metric('Roll', roll),
          ],
        ),
        const SizedBox(height: 12),
        SoftCard(
          child: SizedBox(
            height: 180,
            child: _pitch.length < 2
                ? const Center(child: Text('开始训练后显示角度曲线', style: TextStyle(color: Color(0xFF94A3B8))))
                : LineChart(
                    LineChartData(
                      gridData: const FlGridData(show: false),
                      titlesData: const FlTitlesData(show: false),
                      borderData: FlBorderData(show: false),
                      lineBarsData: [
                        LineChartBarData(
                          spots: _pitch,
                          isCurved: true,
                          color: blue,
                          barWidth: 3,
                          dotData: const FlDotData(show: false),
                          belowBarData: BarAreaData(show: true, color: blue.withValues(alpha: 0.12)),
                        ),
                      ],
                    ),
                  ),
          ),
        ),
        const SizedBox(height: 16),
        FilledButton(
          onPressed: _toggle,
          style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(52), backgroundColor: _running ? danger : blue, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
          child: Text(_running ? '结束' : '开始训练'),
        ),
      ],
    );
  }

  Widget _metric(String label, double value) {
    return Expanded(
      child: SoftCard(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
        child: Column(
          children: [
            Text(label, style: const TextStyle(color: Color(0xFF64748B), fontSize: 12)),
            const SizedBox(height: 4),
            Text('${value.toStringAsFixed(1)}°', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: ink)),
          ],
        ),
      ),
    );
  }
}
