import 'dart:convert';

import 'package:http/http.dart' as http;

const apiBase = String.fromEnvironment('API_BASE', defaultValue: 'http://10.0.2.2:3000');

class ApiException implements Exception {
  ApiException(this.message, {this.code});
  final String message;
  final String? code;
  @override
  String toString() => message;
}

class RenyunApi {
  RenyunApi({http.Client? client}) : _client = client ?? http.Client();
  final http.Client _client;

  Future<Map<String, dynamic>> login(String username, String password) {
    return _post('/api/auth/login', {
      'username': username,
      'password': password,
      'role': 'patient',
    });
  }

  Future<void> register(Map<String, dynamic> body) async {
    await _post('/api/auth/register', body);
  }

  Future<List<Map<String, dynamic>>> doctors() async {
    final data = await _get('/api/doctors');
    return (data as List).map((e) => Map<String, dynamic>.from(e as Map)).toList();
  }

  Future<List<Map<String, dynamic>>> tasks(String patientId) async {
    final data = await _get('/api/tasks/$patientId');
    return (data as List).map((e) => Map<String, dynamic>.from(e as Map)).toList();
  }

  Future<void> setTaskDone(String taskId, bool done) async {
    await _send('PATCH', '/api/tasks/$taskId', {'done': done});
  }

  Future<List<Map<String, dynamic>>> messages(String patientId) async {
    final data = await _get('/api/messages?patientId=$patientId');
    return (data as List).map((e) => Map<String, dynamic>.from(e as Map)).toList();
  }

  Future<void> sendMessage(Map<String, dynamic> message) async {
    await _post('/api/messages', message);
  }

  Future<dynamic> _get(String path) async {
    final res = await _client.get(Uri.parse('$apiBase$path'));
    return _body(res);
  }

  Future<Map<String, dynamic>> _post(String path, Map<String, dynamic> body) async {
    final data = await _send('POST', path, body);
    return data is Map<String, dynamic> ? data : <String, dynamic>{'ok': true};
  }

  Future<dynamic> _send(String method, String path, Map<String, dynamic> body) async {
    final req = http.Request(method, Uri.parse('$apiBase$path'))
      ..headers['Content-Type'] = 'application/json; charset=utf-8'
      ..body = jsonEncode(body);
    final streamed = await _client.send(req);
    final res = await http.Response.fromStream(streamed);
    return _body(res);
  }

  dynamic _body(http.Response res) {
    dynamic data = {};
    if (res.body.isNotEmpty) {
      data = jsonDecode(utf8.decode(res.bodyBytes));
    }
    if (res.statusCode >= 400) {
      final map = data is Map ? data : const {};
      throw ApiException(
        (map['message'] ?? map['error'] ?? '请求失败').toString(),
        code: map['code']?.toString(),
      );
    }
    if (data is Map && data['ok'] == true && data['user'] is Map) {
      return {'ok': true, 'user': Map<String, dynamic>.from(data['user'] as Map)};
    }
    return data;
  }
}
