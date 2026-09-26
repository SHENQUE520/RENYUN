import 'package:flutter/material.dart';

import '../session.dart';
import '../theme.dart';

class TasksPage extends StatefulWidget {
  const TasksPage({super.key, required this.session});
  final Session session;

  @override
  State<TasksPage> createState() => _TasksPageState();
}

class _TasksPageState extends State<TasksPage> {
  List<Map<String, dynamic>> _tasks = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final list = await widget.session.api.tasks(widget.session.id);
      if (mounted) setState(() => _tasks = list);
    } catch (_) {
      if (mounted) setState(() => _error = '任务暂时没有同步到');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _toggle(Map<String, dynamic> task, bool done) async {
    await widget.session.api.setTaskDone('${task['id']}', done);
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    final open = _tasks.where((t) => !_done(t)).toList();
    final closed = _tasks.where(_done).toList();
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: [
          Text('你好，${widget.session.name}', style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
          const SizedBox(height: 4),
          Text(widget.session.user?['diagnosis']?.toString().isNotEmpty == true ? '${widget.session.user!['diagnosis']}' : '今日康复安排', style: const TextStyle(color: Color(0xFF64748B))),
          const SizedBox(height: 18),
          if (_loading) const LinearProgressIndicator(),
          if (_error != null) Text(_error!, style: const TextStyle(color: danger)),
          _section('未完成', open, false),
          const SizedBox(height: 16),
          _section('已完成', closed, true),
        ],
      ),
    );
  }

  Widget _section(String title, List<Map<String, dynamic>> items, bool done) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
        const SizedBox(height: 10),
        if (items.isEmpty)
          SoftCard(child: Text(done ? '完成后会出现在这里' : '今天的任务都完成了', style: const TextStyle(color: Color(0xFF64748B))))
        else
          ...items.map((task) => Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: SoftCard(
                  child: Row(
                    children: [
                      Container(
                        width: 44,
                        height: 44,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(color: done ? const Color(0xFFE8F7F1) : const Color(0xFFEAF0FF), borderRadius: BorderRadius.circular(14)),
                        child: Icon(done ? Icons.check_rounded : Icons.fitness_center_rounded, color: done ? good : blue),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('${task['name']}', style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
                            const SizedBox(height: 2),
                            Text('${task['count'] ?? ''} ${task['unit'] ?? ''}', style: const TextStyle(color: Color(0xFF64748B), fontSize: 13)),
                            if ('${task['key_points'] ?? ''}'.isNotEmpty)
                              Text('${task['key_points']}', style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                          ],
                        ),
                      ),
                      TextButton(
                        onPressed: () => _toggle(task, !done),
                        child: Text(done ? '撤销' : '完成'),
                      ),
                    ],
                  ),
                ),
              )),
      ],
    );
  }
}

bool _done(Map<String, dynamic> task) => task['done'] == 1 || task['done'] == true;
