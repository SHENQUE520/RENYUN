import 'dart:async';

import 'package:flutter/material.dart';

import '../session.dart';
import '../theme.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key, required this.session});
  final Session session;

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  List<Map<String, dynamic>> _messages = [];
  Timer? _poll;

  @override
  void initState() {
    super.initState();
    _load();
    _poll = Timer.periodic(const Duration(seconds: 3), (_) => _load(silent: true));
  }

  @override
  void dispose() {
    _poll?.cancel();
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _load({bool silent = false}) async {
    try {
      final list = await widget.session.api.messages(widget.session.id);
      if (!mounted) return;
      setState(() => _messages = list.where((m) => m['recalled'] != 1).toList());
      if (!silent && _scroll.hasClients) {
        _scroll.jumpTo(_scroll.position.maxScrollExtent);
      }
    } catch (_) {}
  }

  Future<void> _send() async {
    final text = _input.text.trim();
    if (text.isEmpty) return;
    _input.clear();
    final now = DateTime.now();
    final hh = now.hour.toString().padLeft(2, '0');
    final mm = now.minute.toString().padLeft(2, '0');
    await widget.session.api.sendMessage({
      'id': 'm${now.microsecondsSinceEpoch}',
      'fromRole': 'patient',
      'fromName': widget.session.name,
      'toPatientId': widget.session.id,
      'type': 'text',
      'text': text,
      'time': '$hh:$mm',
      'date': '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}',
      'read': false,
      'recalled': false,
    });
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    final doctor = widget.session.user?['doctor_name']?.toString();
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 8),
          child: Row(
            children: [
              const Expanded(child: Text('消息', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink))),
              Text(doctor == null || doctor.isEmpty ? '主治医生' : doctor, style: const TextStyle(color: Color(0xFF64748B))),
            ],
          ),
        ),
        Expanded(
          child: _messages.isEmpty
              ? const Center(child: Text('还没有消息，可以直接发给医生', style: TextStyle(color: Color(0xFF94A3B8))))
              : ListView.builder(
                  controller: _scroll,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: _messages.length,
                  itemBuilder: (context, i) {
                    final m = _messages[i];
                    final mine = m['from_role'] == 'patient';
                    return Align(
                      alignment: mine ? Alignment.centerRight : Alignment.centerLeft,
                      child: Container(
                        margin: const EdgeInsets.only(bottom: 8),
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                        constraints: const BoxConstraints(maxWidth: 280),
                        decoration: BoxDecoration(
                          color: mine ? blue : Colors.white,
                          borderRadius: BorderRadius.circular(18),
                        ),
                        child: Text('${m['text'] ?? ''}', style: TextStyle(color: mine ? Colors.white : ink, height: 1.35)),
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
                Expanded(child: TextField(controller: _input, decoration: const InputDecoration(hintText: '写给医生'))),
                const SizedBox(width: 8),
                IconButton.filled(onPressed: _send, icon: const Icon(Icons.send_rounded)),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
