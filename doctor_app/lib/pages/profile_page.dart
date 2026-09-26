import 'package:flutter/material.dart';

import '../session.dart';
import '../theme.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key, required this.session});
  final Session session;

  @override
  Widget build(BuildContext context) {
    final user = session.user ?? {};
    final initial = session.name.isEmpty ? '医' : session.name.characters.first;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
      children: [
        const Text('我的', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
        const SizedBox(height: 16),
        SoftCard(
          color: blueDeep,
          child: Row(
            children: [
              CircleAvatar(radius: 28, backgroundColor: Colors.white, foregroundColor: blueDeep, child: Text(initial, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700))),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(session.name, style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w700)),
                    const SizedBox(height: 4),
                    Text('${_text(user, 'title')} · ${_text(user, 'department')}', style: const TextStyle(color: Color(0xD6FFFFFF))),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        SoftCard(
          child: Column(
            children: [
              _row('医院', _text(user, 'hospital')),
              _row('专长', _text(user, 'speciality')),
              _row('电话', _text(user, 'phone')),
              _row('简介', _text(user, 'bio')),
            ],
          ),
        ),
        const SizedBox(height: 16),
        OutlinedButton(
          onPressed: session.logout,
          style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48), foregroundColor: danger),
          child: const Text('退出登录'),
        ),
      ],
    );
  }

  String _text(Map<String, dynamic> user, String key) {
    final value = '${user[key] ?? ''}'.trim();
    return value.isEmpty ? '—' : value;
  }

  Widget _row(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 52, child: Text(label, style: const TextStyle(color: muted))),
          Expanded(child: Text(value, style: const TextStyle(color: ink, height: 1.4))),
        ],
      ),
    );
  }
}
