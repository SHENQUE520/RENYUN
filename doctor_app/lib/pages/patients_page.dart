import 'package:flutter/material.dart';

import '../api.dart';
import '../clinic.dart';
import '../roster.dart';
import '../session.dart';
import '../theme.dart';
import 'patient_detail_page.dart';

class PatientsPage extends StatefulWidget {
  const PatientsPage({super.key, required this.session, required this.clinic});
  final Session session;
  final Clinic clinic;

  @override
  State<PatientsPage> createState() => _PatientsPageState();
}

class _PatientsPageState extends State<PatientsPage> {
  List<Map<String, dynamic>> _pending = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final list = await widget.session.api.pendingPatients();
      if (!mounted) return;
      setState(() {
        _pending = list;
        _loading = false;
      });
    } catch (_) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _approve(Map<String, dynamic> patient) async {
    try {
      await widget.session.api.approve('${patient['id']}', widget.session.id, widget.session.name);
      widget.clinic.remember(_fromPending(patient));
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('已批准 ${patient['name']}')));
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  Future<void> _reject(Map<String, dynamic> patient) async {
    try {
      await widget.session.api.reject('${patient['id']}');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('已拒绝 ${patient['name']}')));
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  ClinicPatient _fromPending(Map<String, dynamic> patient) {
    return ClinicPatient(
      id: '${patient['id']}',
      name: '${patient['name'] ?? '新患者'}',
      gender: '${patient['gender'] ?? ''}',
      age: int.tryParse('${patient['age']}') ?? 0,
      diagnosis: '${patient['diagnosis'] ?? ''}',
      hospital: '${patient['hospital'] ?? ''}',
    );
  }

  @override
  Widget build(BuildContext context) {
    final clinic = widget.clinic;
    return AnimatedBuilder(
      animation: clinic,
      builder: (context, _) {
        return ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          children: [
            Text(widget.session.name, style: const TextStyle(color: muted, fontSize: 13)),
            const SizedBox(height: 2),
            const Text('患者', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
            const SizedBox(height: 4),
            Text(widget.session.title, style: const TextStyle(color: muted)),
            const SizedBox(height: 16),
            Row(
              children: [
                _stat('${clinic.patients.length}', '管理患者', blue),
                const SizedBox(width: 8),
                _stat('${clinic.warningPatients}', '动作预警', danger),
                const SizedBox(width: 8),
                _stat(_loading ? '…' : '${_pending.length}', '待审核', warn),
              ],
            ),
            if (_pending.isNotEmpty) ...[
              const SizedBox(height: 18),
              const Text('待审核', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
              const SizedBox(height: 8),
              for (final patient in _pending) _pendingCard(patient),
            ],
            const SizedBox(height: 18),
            const Text('在管患者', style: TextStyle(fontWeight: FontWeight.w700, color: ink)),
            const SizedBox(height: 8),
            for (final patient in clinic.patients) _patientTile(patient, patient.id == clinic.selectedId),
          ],
        );
      },
    );
  }

  Widget _stat(String value, String label, Color color) {
    return Expanded(
      child: SoftCard(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
        child: Column(
          children: [
            Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: color)),
            const SizedBox(height: 2),
            Text(label, style: const TextStyle(fontSize: 12, color: muted)),
          ],
        ),
      ),
    );
  }

  Widget _pendingCard(Map<String, dynamic> patient) {
    final name = '${patient['name'] ?? ''}';
    final meta = '${patient['gender'] ?? ''} ${patient['age'] ?? ''}岁'.trim();
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: SoftCard(
        color: const Color(0xFFFFF8EB),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(name, style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
            if (meta.isNotEmpty) Text(meta, style: const TextStyle(color: muted, fontSize: 12)),
            if ('${patient['diagnosis'] ?? ''}'.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Text('${patient['diagnosis']}', style: const TextStyle(color: ink, fontSize: 13)),
              ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(child: FilledButton(onPressed: () => _approve(patient), child: const Text('批准'))),
                const SizedBox(width: 8),
                Expanded(child: OutlinedButton(onPressed: () => _reject(patient), child: const Text('拒绝'))),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _patientTile(ClinicPatient patient, bool active) {
    final warns = warnCount(patient);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: SoftCard(
        onTap: () {
          widget.clinic.select(patient.id);
          Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => PatientDetailPage(session: widget.session, clinic: widget.clinic, patient: patient)),
          );
        },
        child: Row(
          children: [
            CircleAvatar(
              backgroundColor: active ? blue : const Color(0xFFE8EEF9),
              foregroundColor: active ? Colors.white : blueDeep,
              child: Text(patient.name.characters.first),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(patient.name, style: const TextStyle(fontWeight: FontWeight.w700, color: ink)),
                  const SizedBox(height: 2),
                  Text('${patient.gender} · ${patient.age}岁 · ${patient.headline}', style: const TextStyle(color: muted, fontSize: 12)),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: warns > 0 ? const Color(0xFFFDECEC) : const Color(0xFFE7F6EF),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                warns > 0 ? '$warns 次异常' : '动作平稳',
                style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: warns > 0 ? danger : good),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
