import 'package:flutter/material.dart';

import '../api.dart';
import '../session.dart';
import '../theme.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key, required this.session});
  final Session session;

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _user = TextEditingController(text: 'doctor');
  final _pass = TextEditingController(text: '123456');
  bool _busy = false;
  String? _error;

  @override
  void dispose() {
    _user.dispose();
    _pass.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await widget.session.login(_user.text.trim(), _pass.text);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = '连不上康复服务，请确认电脑上的服务已启动');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(28, 72, 28, 48),
            decoration: const BoxDecoration(
              gradient: LinearGradient(colors: [blueDeep, blue], begin: Alignment.topLeft, end: Alignment.bottomRight),
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(36)),
            ),
            child: const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('韧云智护', style: TextStyle(color: Colors.white, fontSize: 34, fontWeight: FontWeight.w700)),
                SizedBox(height: 8),
                Text('膝关节韧带康复 · 医生工作台', style: TextStyle(color: Color(0xD6FFFFFF), fontSize: 15)),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.fromLTRB(22, 22, 22, 28),
              children: [
                const Text('医生登录', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ink)),
                const SizedBox(height: 6),
                const Text('查看患者、发放任务，并回复康复消息', style: TextStyle(color: muted)),
                const SizedBox(height: 18),
                TextField(controller: _user, decoration: const InputDecoration(labelText: '账号')),
                const SizedBox(height: 12),
                TextField(
                  controller: _pass,
                  obscureText: true,
                  onSubmitted: (_) => _busy ? null : _submit(),
                  decoration: const InputDecoration(labelText: '密码'),
                ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(_error!, style: const TextStyle(color: danger)),
                ],
                const SizedBox(height: 18),
                FilledButton(
                  onPressed: _busy ? null : _submit,
                  style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(52),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  child: Text(_busy ? '正在进入…' : '进入工作台'),
                ),
                const SizedBox(height: 16),
                const Text('演示账号 doctor / 123456', textAlign: TextAlign.center, style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
