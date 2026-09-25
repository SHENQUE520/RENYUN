import 'package:flutter/material.dart';

import 'api.dart';
import 'clinic.dart';
import 'pages/chat_page.dart';
import 'pages/login_page.dart';
import 'pages/patients_page.dart';
import 'pages/profile_page.dart';
import 'pages/tasks_page.dart';
import 'session.dart';
import 'theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final session = Session(RenyunApi());
  await session.restore();
  runApp(RenyunDoctorApp(session: session));
}

class RenyunDoctorApp extends StatefulWidget {
  const RenyunDoctorApp({super.key, required this.session});
  final Session session;

  @override
  State<RenyunDoctorApp> createState() => _RenyunDoctorAppState();
}

class _RenyunDoctorAppState extends State<RenyunDoctorApp> {
  final _clinic = Clinic();

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.session,
      builder: (context, _) {
        return MaterialApp(
          title: '韧云智护',
          debugShowCheckedModeBanner: false,
          theme: renyunTheme(),
          home: !widget.session.ready
              ? const Scaffold(body: Center(child: CircularProgressIndicator()))
              : widget.session.user == null
                  ? LoginPage(session: widget.session)
                  : HomeShell(session: widget.session, clinic: _clinic),
        );
      },
    );
  }
}

class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.session, required this.clinic});
  final Session session;
  final Clinic clinic;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      PatientsPage(session: widget.session, clinic: widget.clinic),
      TasksPage(session: widget.session, clinic: widget.clinic),
      ChatPage(session: widget.session, clinic: widget.clinic),
      ProfilePage(session: widget.session),
    ];
    return Scaffold(
      body: SafeArea(child: pages[_index]),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.people_outline), selectedIcon: Icon(Icons.people), label: '患者'),
          NavigationDestination(icon: Icon(Icons.assignment_outlined), selectedIcon: Icon(Icons.assignment), label: '任务'),
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), selectedIcon: Icon(Icons.chat_bubble), label: '消息'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: '我的'),
        ],
      ),
    );
  }
}
