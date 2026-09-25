import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

import '../api.dart';
import '../demo.dart';
import '../theme.dart';

class VideosPage extends StatelessWidget {
  const VideosPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
      children: [
        const Text('动作', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700, color: ink)),
        const SizedBox(height: 6),
        const Text('先看标准动作，再开始训练', style: TextStyle(color: Color(0xFF64748B))),
        const SizedBox(height: 16),
        for (final item in exercises)
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: SoftCard(
              child: InkWell(
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => VideoPlayPage(mode: item.$1, title: item.$2))),
                child: Row(
                  children: [
                    Container(
                      width: 56,
                      height: 56,
                      decoration: BoxDecoration(color: const Color(0xFFEAF0FF), borderRadius: BorderRadius.circular(16)),
                      child: const Icon(Icons.play_arrow_rounded, color: blue, size: 32),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(item.$2, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16, color: ink)),
                          const SizedBox(height: 4),
                          Text(item.$3, style: const TextStyle(color: Color(0xFF64748B), height: 1.35)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class VideoPlayPage extends StatefulWidget {
  const VideoPlayPage({super.key, required this.mode, required this.title});
  final String mode;
  final String title;

  @override
  State<VideoPlayPage> createState() => _VideoPlayPageState();
}

class _VideoPlayPageState extends State<VideoPlayPage> {
  late final VideoPlayerController _controller;
  String? _error;

  @override
  void initState() {
    super.initState();
    _controller = VideoPlayerController.networkUrl(Uri.parse('$apiBase/videos/${widget.mode}.mp4'))
      ..initialize().then((_) {
        if (mounted) setState(() {});
        _controller.play();
      }).catchError((Object e) {
        if (mounted) setState(() => _error = '视频暂时无法播放');
      });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: ink,
      appBar: AppBar(backgroundColor: ink, foregroundColor: Colors.white, title: Text(widget.title)),
      body: Center(
        child: _error != null
            ? Text(_error!, style: const TextStyle(color: Colors.white))
            : _controller.value.isInitialized
                ? AspectRatio(aspectRatio: _controller.value.aspectRatio, child: VideoPlayer(_controller))
                : const CircularProgressIndicator(color: Colors.white),
      ),
    );
  }
}
