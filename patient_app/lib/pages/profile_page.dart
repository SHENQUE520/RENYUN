import 'package:flutter/material.dart';

import '../session.dart';
import '../theme.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key, required this.session});
  final Session session;

  @override
  Widget build(BuildContext context) {
    final u = session.user ?? {};
    final rows = [
      ('诊断', u['diagnosis']),
      ('医院', u['hospital']),
      ('主治医生', u['doctor_name']),
      ('手术日期', u['surgery_date']),
      ('年龄', u['age']),
      ('备注', u['notes']),
    ];
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
      children: [
        SoftCard(
          child: Row(
            children: [
              CircleAvatar(radius: 28, backgroundColor: const Color(0xFFEAF0FF), child: Text(session.name.isEmpty ? '患' : session.name.characters.first, style: const TextStyle(color: blue, fontSize: 22, fontWeight: FontWeight.w700))),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(session.name, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ink)),
                    Text('${u['gender'] ?? ''}  ·  患者', style: const TextStyle(color: Color(0xFF64748B))),
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
              for (final row in rows)
                if ('${row.$2 ?? ''}'.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        SizedBox(width: 84, child: Text(row.$1, style: const TextStyle(color: Color(0xFF64748B)))),
                        Expanded(child: Text('${row.$2}', style: const TextStyle(color: ink, fontWeight: FontWeight.w600))),
                      ],
                    ),
                  ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        OutlinedButton(
          onPressed: session.logout,
          style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
          child: const Text('退出登录'),
        ),
      ],
    );
  }
}
