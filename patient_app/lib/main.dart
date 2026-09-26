import 'package:flutter/material.dart';

import 'api.dart';
import 'pages/chat_page.dart';
import 'pages/login_page.dart';
import 'pages/profile_page.dart';
import 'pages/register_page.dart';
import 'pages/tasks_page.dart';
import 'pages/train_page.dart';
import 'pages/videos_page.dart';
import 'session.dart';
import 'theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final session = Session(RenyunApi());
  await session.restore();
  runApp(RenyunPatientApp(session: session));
}

class RenyunPatientApp extends StatelessWidget {
  const RenyunPatientApp({super.key, required this.session});
  final Session session;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: session,
      builder: (context, _) {
        return MaterialApp(
          title: '韧云智护',
          debugShowCheckedModeBanner: false,
          theme: renyunTheme(),
          routes: {
            '/register': (_) => RegisterPage(api: session.api),
          },
          home: !session.ready
              ? const Scaffold(body: Center(child: CircularProgressIndicator()))
              : session.user == null
                  ? LoginPage(session: session)
                  : HomeShell(session: session),
        );
      },
    );
  }
}

class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.session});
  final Session session;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      TasksPage(session: widget.session),
      TrainPage(session: widget.session),
      const VideosPage(),
      ChatPage(session: widget.session),
      ProfilePage(session: widget.session),
    ];
    return Scaffold(
      body: SafeArea(child: pages[_index]),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.today_outlined), selectedIcon: Icon(Icons.today), label: '今日'),
          NavigationDestination(icon: Icon(Icons.monitor_heart_outlined), selectedIcon: Icon(Icons.monitor_heart), label: '训练'),
          NavigationDestination(icon: Icon(Icons.play_circle_outline), selectedIcon: Icon(Icons.play_circle), label: '动作'),
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), selectedIcon: Icon(Icons.chat_bubble), label: '消息'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: '我的'),
        ],
      ),
    );
  }
}
