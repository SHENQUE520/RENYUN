import 'package:flutter/material.dart';

import '../api.dart';
import '../theme.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key, required this.api});
  final RenyunApi api;

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _name = TextEditingController();
  final _username = TextEditingController();
  final _password = TextEditingController();
  final _age = TextEditingController();
  final _diagnosis = TextEditingController();
  final _hospital = TextEditingController();
  String _gender = '男';
  String? _doctorId;
  List<Map<String, dynamic>> _doctors = [];
  bool _busy = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    widget.api.doctors().then((list) {
      if (mounted) setState(() => _doctors = list);
    }).catchError((_) {});
  }

  @override
  void dispose() {
    for (final c in [_name, _username, _password, _age, _diagnosis, _hospital]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _submit() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await widget.api.register({
        'role': 'patient',
        'name': _name.text.trim(),
        'gender': _gender,
        'username': _username.text.trim(),
        'password': _password.text,
        'age': int.tryParse(_age.text.trim()),
        'diagnosis': _diagnosis.text.trim(),
        'hospital': _hospital.text.trim(),
        'doctorId': _doctorId,
      });
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('已提交'),
          content: const Text('注册申请已交给医生审核。通过后即可登录。'),
          actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('好的'))],
        ),
      );
      if (mounted) Navigator.pop(context);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = '提交失败，请稍后再试');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('患者注册')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(22, 8, 22, 28),
        children: [
          TextField(controller: _name, decoration: const InputDecoration(labelText: '姓名')),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _gender,
            decoration: const InputDecoration(labelText: '性别'),
            items: const [
              DropdownMenuItem(value: '男', child: Text('男')),
              DropdownMenuItem(value: '女', child: Text('女')),
            ],
            onChanged: (v) => setState(() => _gender = v ?? '男'),
          ),
          const SizedBox(height: 12),
          TextField(controller: _age, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '年龄')),
          const SizedBox(height: 12),
          TextField(controller: _username, decoration: const InputDecoration(labelText: '账号')),
          const SizedBox(height: 12),
          TextField(controller: _password, obscureText: true, decoration: const InputDecoration(labelText: '密码')),
          const SizedBox(height: 12),
          TextField(controller: _diagnosis, decoration: const InputDecoration(labelText: '诊断')),
          const SizedBox(height: 12),
          TextField(controller: _hospital, decoration: const InputDecoration(labelText: '医院')),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _doctorId,
            decoration: const InputDecoration(labelText: '主治医生'),
            items: _doctors
                .map((d) => DropdownMenuItem(value: '${d['id']}', child: Text('${d['name']}  ${d['title'] ?? ''}')))
                .toList(),
            onChanged: (v) => setState(() => _doctorId = v),
          ),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Text(_error!, style: const TextStyle(color: danger)),
          ],
          const SizedBox(height: 18),
          FilledButton(
            onPressed: _busy ? null : _submit,
            style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(52), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
            child: Text(_busy ? '提交中…' : '提交审核'),
          ),
        ],
      ),
    );
  }
}
