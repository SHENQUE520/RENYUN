import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'api.dart';

class Session extends ChangeNotifier {
  Session(this.api);
  final RenyunApi api;
  Map<String, dynamic>? user;
  bool ready = false;

  String get id => '${user?['id'] ?? ''}';
  String get name => '${user?['name'] ?? '医生'}';
  String get title => '${user?['title'] ?? '主治医师'}';

  Future<void> restore() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString('renyun_doctor');
    if (raw != null) {
      user = Map<String, dynamic>.from(jsonDecode(raw) as Map);
    }
    ready = true;
    notifyListeners();
  }

  Future<void> login(String username, String password) async {
    final result = await api.login(username, password);
    final next = Map<String, dynamic>.from(result['user'] as Map);
    next.remove('password');
    user = next;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('renyun_doctor', jsonEncode(user));
    notifyListeners();
  }

  Future<void> logout() async {
    user = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('renyun_doctor');
    notifyListeners();
  }
}
