import 'package:flutter/material.dart';
import '../models/app_settings.dart';

class ScheduleCard extends StatelessWidget {
  final Map<String, DaySchedule> weeklySchedule;

  const ScheduleCard({
    super.key,
    required this.weeklySchedule,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.schedule,
                  color: Theme.of(context).colorScheme.primary,
                ),
                const SizedBox(width: 8),
                Text(
                  'Weekly Schedule',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            const SizedBox(height: 16),
            ...weeklySchedule.entries.map((entry) {
              final dayName = _getDayDisplayName(entry.key);
              final schedule = entry.value;
              
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    SizedBox(
                      width: 80,
                      child: Text(
                        dayName,
                        style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: schedule.isEnabled
                          ? Text(
                              '${schedule.startTime} - ${schedule.endTime}',
                              style: Theme.of(context).textTheme.bodyLarge,
                            )
                          : Text(
                              'Disabled',
                              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                color: Theme.of(context).colorScheme.onSurfaceVariant,
                                fontStyle: FontStyle.italic,
                              ),
                            ),
                    ),
                    Icon(
                      schedule.isEnabled ? Icons.check_circle : Icons.cancel,
                      color: schedule.isEnabled ? Colors.green : Colors.red,
                      size: 16,
                    ),
                  ],
                ),
              );
            }),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {
                  Navigator.pushNamed(context, '/settings');
                },
                icon: const Icon(Icons.edit),
                label: const Text('Edit Schedule'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _getDayDisplayName(String dayKey) {
    switch (dayKey) {
      case 'monday': return 'Mon';
      case 'tuesday': return 'Tue';
      case 'wednesday': return 'Wed';
      case 'thursday': return 'Thu';
      case 'friday': return 'Fri';
      case 'saturday': return 'Sat';
      case 'sunday': return 'Sun';
      default: return dayKey;
    }
  }
}

