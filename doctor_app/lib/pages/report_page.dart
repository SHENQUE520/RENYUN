import 'package:flutter/material.dart';

import '../roster.dart';
import '../session.dart';
import '../theme.dart';

class ReportPage extends StatefulWidget {
  const ReportPage({super.key, required this.session, required this.patient});
  final Session session;
  final ClinicPatient patient;

  @override
  State<ReportPage> createState() => _ReportPageState();
}

class _ReportPageState extends State<ReportPage> {
  RehabScores? _scores;
  String? _ai;
  String? _error;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    var done = 0;
    var total = 0;
    try {
      final tasks = await widget.session.api.tasks(widget.patient.id);
      total = tasks.length;
      done = tasks.where((t) => t['done'] == 1 || t['done'] == true).length;
    } catch (_) {}
    if (mounted) setState(() => _scores = scoreOf(widget.patient, doneTasks: done, totalTasks: total));
  }

  Future<void> _generate() async {
    final scores = _scores;
    if (scores == null) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    final patient = widget.patient;
    final records = patient.records.map((r) => '${r.time} ${r.action} ${r.pitch}° 异常${r.abnormal}次 ${r.status}').join('；');
    final prompt = '''
患者：${patient.name}，${patient.age}岁${patient.gender}，诊断：${patient.headline}，手术日期：${patient.surgeryDate.isEmpty ? '未知' : patient.surgeryDate}。
训练记录：${records.isEmpty ? '暂无' : records}
动作质量${scores.quality}分，稳定性${scores.stability}分，依从性${scores.compliance}分，任务完成${scores.progress}分。
请生成康复评估报告，包含患者概况、训练数据分析、动作质量评估、依从性评价、风险提示、临床建议。
''';
    try {
      final text = await widget.session.api.chat(prompt);
      if (mounted) setState(() => _ai = text);
    } catch (_) {
      if (mounted) setState(() => _error = 'AI 报告暂时不可用。上方评分仍可根据动作记录查看。');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final patient = widget.patient;
    final scores = _scores;
    return Scaffold(
      appBar: AppBar(title: const Text('康复评估')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 28),
        children: [
          Text(patient.name, style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
          Text(patient.headline, style: const TextStyle(color: muted)),
          const SizedBox(height: 16),
          if (scores == null)
            const Center(child: CircularProgressIndicator())
          else
            SoftCard(
              child: Column(
                children: [
                  _bar('动作质量', scores.quality, blue),
                  _bar('稳定性', scores.stability, good),
                  _bar('依从性', scores.compliance, warn),
                  _bar('任务完成', scores.progress, blueDeep),
                ],
              ),
            ),
          const SizedBox(height: 12),
          FilledButton(
            onPressed: _busy || scores == null ? null : _generate,
            style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(48)),
            child: Text(_busy ? '正在生成…' : '生成 AI 报告'),
          ),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Text(_error!, style: const TextStyle(color: muted, height: 1.4)),
          ],
          if (_ai != null) ...[
            const SizedBox(height: 12),
            SoftCard(child: Text(_ai!, style: const TextStyle(color: ink, height: 1.5))),
          ],
        ],
      ),
    );
  }

  Widget _bar(String label, int value, Color color) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(child: Text(label, style: const TextStyle(color: ink, fontWeight: FontWeight.w600))),
              Text('$value', style: TextStyle(color: color, fontWeight: FontWeight.w700)),
            ],
          ),
          const SizedBox(height: 6),
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: LinearProgressIndicator(
              value: value / 100,
              minHeight: 8,
              backgroundColor: line,
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}
