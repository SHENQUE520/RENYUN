import 'dart:async';

import 'package:flutter/material.dart';

import '../clinic.dart';
import '../session.dart';
import '../theme.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key, required this.session, required this.clinic});
  final Session session;
  final Clinic clinic;

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  List<Map<String, dynamic>> _messages = [];
  Timer? _poll;
  String? _error;

  @override
  void initState() {
    super.initState();
    widget.clinic.addListener(_load);
    _load();
    _poll = Timer.periodic(const Duration(seconds: 3), (_) => _load());
  }

  @override
  void dispose() {
    _poll?.cancel();
    widget.clinic.removeListener(_load);
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final list = await widget.session.api.messages(widget.clinic.selected.id);
      if (!mounted) return;
      setState(() {
        _error = null;
        _messages = list.where((m) => m['recalled'] != 1).toList();
      });
    } catch (_) {
      if (mounted) setState(() => _error = '消息暂时读不到');
    }
  }

  Future<void> _send() async {
    final text = _input.text.trim();
    if (text.isEmpty) return;
    _input.clear();
    final now = DateTime.now();
    final hh = now.hour.toString().padLeft(2, '0');
    final mm = now.minute.toString().padLeft(2, '0');
    final month = now.month.toString().padLeft(2, '0');
    final day = now.day.toString().padLeft(2, '0');
    try {
      await widget.session.api.sendMessage({
        'id': 'm${now.microsecondsSinceEpoch}',
        'fromRole': 'doctor',
        'fromName': widget.session.name,
        'toPatientId': widget.clinic.selected.id,
        'type': 'text',
        'text': text,
        'time': '$hh:$mm',
        'date': '${now.year}-$month-$day',
        'read': false,
        'recalled': false,
      });
      await _load();
    } catch (_) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('发送失败')));
    }
  }

  Future<void> _recall(String id) async {
    try {
      await widget.session.api.recall(id);
      await _load();
    } catch (_) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('撤回失败')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final patient = widget.clinic.selected;
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('消息', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
              const SizedBox(height: 8),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    for (final item in widget.clinic.patients)
                      Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: ChoiceChip(
                          label: Text(item.name),
                          selected: item.id == patient.id,
                          onSelected: (_) => widget.clinic.select(item.id),
                        ),
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: _messages.isEmpty
              ? Center(child: Text(_error ?? '和${patient.name}还没有消息', style: const TextStyle(color: muted)))
              : ListView.builder(
                  controller: _scroll,
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
                  itemCount: _messages.length,
                  itemBuilder: (context, index) {
                    final message = _messages[index];
                    final mine = message['from_role'] == 'doctor';
                    return Align(
                      alignment: mine ? Alignment.centerRight : Alignment.centerLeft,
                      child: GestureDetector(
                        onLongPress: mine ? () => _recall('${message['id']}') : null,
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 8),
                          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                          constraints: const BoxConstraints(maxWidth: 280),
                          decoration: BoxDecoration(
                            color: mine ? blue : Colors.white,
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('${message['text'] ?? ''}', style: TextStyle(color: mine ? Colors.white : ink, height: 1.35)),
                              const SizedBox(height: 4),
                              Text(
                                mine ? '${message['time'] ?? ''} · 长按撤回' : '${message['from_name'] ?? ''} ${message['time'] ?? ''}',
                                style: TextStyle(color: mine ? const Color(0xCCFFFFFF) : muted, fontSize: 11),
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
        ),
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(12, 0, 12, 8),
            child: Row(
              children: [
                Expanded(child: TextField(controller: _input, decoration: const InputDecoration(hintText: '回复患者'), onSubmitted: (_) => _send())),
                const SizedBox(width: 8),
                IconButton.filled(onPressed: _send, icon: const Icon(Icons.send)),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
