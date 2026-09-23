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
  final _user = TextEditingController(text: 'zhangsan');
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
            padding: const EdgeInsets.fromLTRB(28, 72, 28, 36),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [blueDeep, blue],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(36)),
            ),
            child: const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('韧云智护', style: TextStyle(color: Colors.white, fontSize: 34, fontWeight: FontWeight.w700)),
                SizedBox(height: 8),
                Text('膝关节韧带康复 · 患者端', style: TextStyle(color: Color(0xD6FFFFFF), fontSize: 15)),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.fromLTRB(22, 22, 22, 28),
              children: [
                const Text('登录', style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: ink)),
                const SizedBox(height: 6),
                const Text('使用医生审核通过的账号进入今日训练', style: TextStyle(color: Color(0xFF64748B))),
                const SizedBox(height: 18),
                TextField(controller: _user, decoration: const InputDecoration(labelText: '账号')),
                const SizedBox(height: 12),
                TextField(controller: _pass, obscureText: true, decoration: const InputDecoration(labelText: '密码')),
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
                  child: Text(_busy ? '正在进入…' : '进入系统'),
                ),
                const SizedBox(height: 8),
                TextButton(
                  onPressed: () => Navigator.pushNamed(context, '/register'),
                  child: const Text('还没有账号？提交注册'),
                ),
                const SizedBox(height: 8),
                const Text('演示账号 zhangsan / 123456', textAlign: TextAlign.center, style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
