import 'package:flutter/material.dart';

import '../api.dart';
import '../clinic.dart';
import '../roster.dart';
import '../session.dart';
import '../theme.dart';

class DraftTask {
  DraftTask({required this.name, required this.count, required this.unit, required this.keyPoints, required this.details});
  String name;
  int count;
  String unit;
  String keyPoints;
  String details;
}

class TasksPage extends StatefulWidget {
  const TasksPage({super.key, required this.session, required this.clinic});
  final Session session;
  final Clinic clinic;

  @override
  State<TasksPage> createState() => _TasksPageState();
}

class _TasksPageState extends State<TasksPage> {
  final _drafts = <DraftTask>[];
  final _name = TextEditingController();
  final _count = TextEditingController(text: '10');
  final _points = TextEditingController();
  final _details = TextEditingController();
  String _unit = '次';
  bool _busy = false;

  @override
  void dispose() {
    _name.dispose();
    _count.dispose();
    _points.dispose();
    _details.dispose();
    super.dispose();
  }

  void _add(DraftTask task) {
    final existing = _drafts.where((item) => item.name == task.name);
    setState(() {
      if (existing.isNotEmpty) {
        existing.first.count = task.count;
      } else {
        _drafts.add(task);
      }
    });
  }

  Future<void> _publish() async {
    if (_drafts.isEmpty) return;
    final patient = widget.clinic.selected;
    setState(() => _busy = true);
    try {
      for (final task in _drafts) {
        await widget.session.api.assignTask(
          patientId: patient.id,
          name: task.name,
          count: task.count,
          unit: task.unit,
          keyPoints: task.keyPoints,
          details: task.details,
        );
      }
      if (!mounted) return;
      setState(_drafts.clear);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('已向 ${patient.name} 发布任务')));
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } catch (_) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('发布失败，请确认康复服务已启动')));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.clinic,
      builder: (context, _) {
        final patient = widget.clinic.selected;
        return ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          children: [
            const Text('任务', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
            const SizedBox(height: 6),
            const Text('选择患者后，把动作加入任务单再发布', style: TextStyle(color: muted)),
            const SizedBox(height: 14),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (final item in widget.clinic.patients)
                  ChoiceChip(
                    label: Text(item.name),
                    selected: item.id == patient.id,
                    onSelected: (_) => widget.clinic.select(item.id),
                  ),
              ],
            ),
            const SizedBox(height: 16),
            const Text('预设动作', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
            const SizedBox(height: 8),
            for (final preset in presets)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: SoftCard(
                  onTap: () => _add(DraftTask(
                    name: preset.name,
                    count: preset.count,
                    unit: preset.unit,
                    keyPoints: preset.keyPoints,
                    details: preset.details,
                  )),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(preset.name, style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
                      const SizedBox(height: 2),
                      Text('${preset.count}${preset.unit} · ${preset.keyPoints}', style: const TextStyle(color: muted, fontSize: 12)),
                    ],
                  ),
                ),
              ),
            const SizedBox(height: 8),
            SoftCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('自定义动作', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
                  const SizedBox(height: 10),
                  TextField(controller: _name, decoration: const InputDecoration(labelText: '动作名称')),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(child: TextField(controller: _count, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '数量'))),
                      const SizedBox(width: 8),
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          initialValue: _unit,
                          decoration: const InputDecoration(labelText: '单位'),
                          items: const [
                            DropdownMenuItem(value: '次', child: Text('次')),
                            DropdownMenuItem(value: '秒', child: Text('秒')),
                            DropdownMenuItem(value: '分钟', child: Text('分钟')),
                            DropdownMenuItem(value: '组', child: Text('组')),
                          ],
                          onChanged: (value) => setState(() => _unit = value ?? '次'),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  TextField(controller: _points, decoration: const InputDecoration(labelText: '动作要点')),
                  const SizedBox(height: 10),
                  TextField(controller: _details, minLines: 2, maxLines: 4, decoration: const InputDecoration(labelText: '细节说明')),
                  const SizedBox(height: 12),
                  OutlinedButton(
                    onPressed: () {
                      final name = _name.text.trim();
                      if (name.isEmpty) return;
                      _add(DraftTask(
                        name: name,
                        count: int.tryParse(_count.text) ?? 10,
                        unit: _unit,
                        keyPoints: _points.text.trim(),
                        details: _details.text.trim(),
                      ));
                      _name.clear();
                      _points.clear();
                      _details.clear();
                    },
                    child: const Text('加入任务单'),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            SoftCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('给 ${patient.name} 的任务单', style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
                  const SizedBox(height: 8),
                  if (_drafts.isEmpty)
                    const Text('点上方动作即可加入', style: TextStyle(color: muted))
                  else
                    for (var i = 0; i < _drafts.length; i++)
                      ListTile(
                        contentPadding: EdgeInsets.zero,
                        title: Text('${_drafts[i].name} × ${_drafts[i].count}${_drafts[i].unit}'),
                        subtitle: _drafts[i].keyPoints.isEmpty ? null : Text(_drafts[i].keyPoints),
                        trailing: IconButton(
                          onPressed: () => setState(() => _drafts.removeAt(i)),
                          icon: const Icon(Icons.close),
                        ),
                      ),
                  const SizedBox(height: 8),
                  FilledButton(
                    onPressed: _busy || _drafts.isEmpty ? null : _publish,
                    style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                    child: Text(_busy ? '正在发布…' : '发布任务单'),
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }
}
