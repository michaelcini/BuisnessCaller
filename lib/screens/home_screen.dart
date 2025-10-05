import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_settings_provider.dart';
import '../services/call_blocker_service.dart';
import '../widgets/schedule_card.dart';
import '../widgets/status_card.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late CallBlockerService _callBlockerService;

  @override
  void initState() {
    super.initState();
    _callBlockerService = context.read<CallBlockerService>();
    _initializeService();
  }

  Future<void> _initializeService() async {
    await _callBlockerService.initialize();
    await _callBlockerService.startService();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Call Blocker'),
        actions: [
          Consumer<AppSettingsProvider>(
            builder: (context, provider, child) {
              return Switch(
                value: provider.settings.isEnabled,
                onChanged: (value) async {
                  await provider.toggleEnabled(value);
                  if (value) {
                    await _callBlockerService.startService();
                  } else {
                    await _callBlockerService.stopService();
                  }
                },
              );
            },
          ),
        ],
      ),
      body: Consumer<AppSettingsProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                StatusCard(
                  isEnabled: provider.settings.isEnabled,
                  isServiceRunning: _callBlockerService.isServiceRunning,
                ),
                const SizedBox(height: 16),
                ScheduleCard(
                  weeklySchedule: provider.settings.weeklySchedule,
                ),
                const SizedBox(height: 16),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Quick Actions',
                          style: Theme.of(context).textTheme.titleLarge,
                        ),
                        const SizedBox(height: 16),
                        Row(
                          children: [
                            Expanded(
                              child: ElevatedButton.icon(
                                onPressed: () {
                                  Navigator.pushNamed(context, '/settings');
                                },
                                icon: const Icon(Icons.settings),
                                label: const Text('Settings'),
                              ),
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: ElevatedButton.icon(
                                onPressed: () {
                                  Navigator.pushNamed(context, '/about');
                                },
                                icon: const Icon(Icons.info),
                                label: const Text('About'),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton.icon(
                            onPressed: () {
                              Navigator.pushNamed(context, '/logs');
                            },
                            icon: const Icon(Icons.list_alt),
                            label: const Text('View Logs'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.blue[100],
                              foregroundColor: Colors.blue[800],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

