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
      'role': 'doctor',
    });
  }

  Future<List<Map<String, dynamic>>> pendingPatients() async {
    final data = await _get('/api/patients/pending');
    return _list(data);
  }

  Future<void> approve(String patientId, String doctorId, String doctorName) async {
    await _post('/api/patients/$patientId/approve', {
      'doctorId': doctorId,
      'doctorName': doctorName,
    });
  }

  Future<void> reject(String patientId) async {
    await _send('POST', '/api/patients/$patientId/reject', {});
  }

  Future<List<Map<String, dynamic>>> tasks(String patientId) async {
    return _list(await _get('/api/tasks/$patientId'));
  }

  Future<void> assignTask({
    required String patientId,
    required String name,
    required int count,
    required String unit,
    String keyPoints = '',
    String details = '',
  }) async {
    await _post('/api/tasks/$patientId', {
      'name': name,
      'count': count,
      'unit': unit,
      'key_points': keyPoints,
      'details': details,
    });
  }

  Future<List<Map<String, dynamic>>> messages(String patientId) async {
    return _list(await _get('/api/messages?patientId=$patientId'));
  }

  Future<void> sendMessage(Map<String, dynamic> message) async {
    await _post('/api/messages', message);
  }

  Future<void> recall(String id) async {
    await _send('PATCH', '/api/messages/$id/recall', {});
  }

  Future<String> chat(String prompt) async {
    final data = await _post('/api/chat', {
      'messages': [
        {
          'role': 'system',
          'content': '你是专业的康复医师，为主治医生撰写详细、专业的患者康复评估报告。450字左右，6个小标题，纯文本，语气专业、有数据支撑。',
        },
        {'role': 'user', 'content': prompt},
      ],
      'temperature': 0.5,
    });
    final choices = data['choices'];
    if (choices is List && choices.isNotEmpty) {
      final message = choices.first;
      if (message is Map && message['message'] is Map) {
        return '${(message['message'] as Map)['content'] ?? ''}';
      }
    }
    throw ApiException('AI 没有返回报告');
  }

  List<Map<String, dynamic>> _list(dynamic data) {
    return (data as List).map((e) => Map<String, dynamic>.from(e as Map)).toList();
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
