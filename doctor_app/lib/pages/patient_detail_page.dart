import 'dart:async';

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

import '../clinic.dart';
import '../roster.dart';
import '../session.dart';
import '../theme.dart';
import 'report_page.dart';

class PatientDetailPage extends StatefulWidget {
  const PatientDetailPage({super.key, required this.session, required this.clinic, required this.patient});
  final Session session;
  final Clinic clinic;
  final ClinicPatient patient;

  @override
  State<PatientDetailPage> createState() => _PatientDetailPageState();
}

class _PatientDetailPageState extends State<PatientDetailPage> {
  late final List<double> _pitch = demoWave(widget.patient.id, base: 42);
  late final List<double> _roll = demoWave(widget.patient.id, base: 6);
  int _index = 0;
  Timer? _timer;
  List<Map<String, dynamic>> _tasks = [];
  String _preset = presets.first.name;
  final _count = TextEditingController(text: '15');
  bool _assigning = false;

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(milliseconds: 700), (_) {
      if (mounted) setState(() => _index = (_index + 1) % _pitch.length);
    });
    _loadTasks();
  }

  @override
  void dispose() {
    _timer?.cancel();
    _count.dispose();
    super.dispose();
  }

  Future<void> _loadTasks() async {
    try {
      final list = await widget.session.api.tasks(widget.patient.id);
      if (mounted) setState(() => _tasks = list);
    } catch (_) {}
  }

  Future<void> _assign() async {
    setState(() => _assigning = true);
    try {
      await widget.session.api.assignTask(
        patientId: widget.patient.id,
        name: _preset,
        count: int.tryParse(_count.text) ?? 15,
        unit: '次',
      );
      await _loadTasks();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('已向 ${widget.patient.name} 发放 $_preset')));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('发放失败，请确认康复服务已启动')));
      }
    } finally {
      if (mounted) setState(() => _assigning = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final patient = widget.patient;
    final pitch = _pitch[_index];
    final roll = _roll[_index];
    final ked = 80 + (pitch - 40) * 0.2;
    final tone = pitch.abs() > 65 ? danger : (pitch.abs() >= 25 ? good : muted);
    final pill = pitch.abs() > 65 ? '角度偏大' : (pitch.abs() >= 25 ? '动作正常' : '等待动作');
    final spots = [
      for (var i = 0; i < _pitch.length; i++) FlSpot(i.toDouble(), _pitch[(i + _index) % _pitch.length]),
    ];

    return Scaffold(
      appBar: AppBar(title: Text(patient.name)),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 28),
        children: [
          SoftCard(
            color: blueDeep,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(patient.headline, style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w700)),
                const SizedBox(height: 6),
                Text('${patient.gender} · ${patient.age}岁 · ${patient.hospital}', style: const TextStyle(color: Color(0xD6FFFFFF))),
                if (patient.surgeryDate.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: Text('手术 ${patient.surgeryDate}', style: const TextStyle(color: Color(0xD6FFFFFF), fontSize: 13)),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _metric('Pitch', '${pitch.toStringAsFixed(1)}°', tone),
              const SizedBox(width: 8),
              _metric('膝关节', '${ked.toStringAsFixed(1)}°', ink),
              const SizedBox(width: 8),
              _metric('Roll', '${roll.toStringAsFixed(1)}°', ink),
            ],
          ),
          const SizedBox(height: 12),
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Expanded(child: Text('姿态曲线', style: TextStyle(fontWeight: FontWeight.w700, color: ink))),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(color: tone.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(20)),
                      child: Text(pill, style: TextStyle(color: tone, fontSize: 12, fontWeight: FontWeight.w600)),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                const Text('演示曲线，与网页监测同一类角度', style: TextStyle(color: muted, fontSize: 12)),
                const SizedBox(height: 8),
                SizedBox(
                  height: 160,
                  child: LineChart(
                    LineChartData(
                      minY: 0,
                      maxY: 90,
                      gridData: const FlGridData(show: false),
                      titlesData: const FlTitlesData(show: false),
                      borderData: FlBorderData(show: false),
                      lineBarsData: [
                        LineChartBarData(
                          spots: spots,
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
              ],
            ),
          ),
          const SizedBox(height: 12),
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('动作记录', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
                const SizedBox(height: 8),
                if (patient.records.isEmpty)
                  const Text('暂无动作记录', style: TextStyle(color: muted))
                else
                  for (final record in patient.records) _record(record),
              ],
            ),
          ),
          const SizedBox(height: 12),
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('当前任务', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
                const SizedBox(height: 8),
                if (_tasks.isEmpty)
                  const Text('还没有从服务端读到任务', style: TextStyle(color: muted))
                else
                  for (final task in _tasks)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Text(
                        '${task['name']} × ${task['count']}${task['unit']}  ${task['done'] == 1 ? '已完成' : '未完成'}',
                        style: const TextStyle(color: ink),
                      ),
                    ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('快速发放', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: _preset,
                  items: [for (final item in presets) DropdownMenuItem(value: item.name, child: Text(item.name))],
                  onChanged: (value) => setState(() => _preset = value ?? _preset),
                ),
                const SizedBox(height: 10),
                TextField(controller: _count, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '次数')),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: _assigning ? null : _assign,
                  style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                  child: Text(_assigning ? '正在发放…' : '发放给 ${patient.name}'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => ReportPage(session: widget.session, patient: patient)),
              );
            },
            style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
            child: const Text('查看康复评估'),
          ),
        ],
      ),
    );
  }

  Widget _metric(String label, String value, Color color) {
    return Expanded(
      child: SoftCard(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
        child: Column(
          children: [
            Text(label, style: const TextStyle(fontSize: 12, color: muted)),
            const SizedBox(height: 4),
            Text(value, style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: color)),
          ],
        ),
      ),
    );
  }

  Widget _record(TrainRecord record) {
    final tone = statusTone(record.status);
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        children: [
          Container(width: 8, height: 8, decoration: BoxDecoration(color: tone, shape: BoxShape.circle)),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(record.action, style: const TextStyle(fontWeight: FontWeight.w600, color: ink)),
                Text('${record.time} · Pitch ${record.pitch}° · 异常 ${record.abnormal}', style: const TextStyle(color: muted, fontSize: 12)),
              ],
            ),
          ),
          Text(record.status, style: TextStyle(color: tone, fontWeight: FontWeight.w600)),
        ],
      ),
    );
  }
}
