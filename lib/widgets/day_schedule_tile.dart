import 'package:flutter/material.dart';
import '../models/app_settings.dart';

class DayScheduleTile extends StatelessWidget {
  final String day;
  final DaySchedule schedule;
  final Function(DaySchedule) onScheduleChanged;

  const DayScheduleTile({
    super.key,
    required this.day,
    required this.schedule,
    required this.onScheduleChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    _getDayDisplayName(day),
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                Switch(
                  value: schedule.isEnabled,
                  onChanged: (value) {
                    onScheduleChanged(schedule.copyWith(isEnabled: value));
                  },
                ),
              ],
            ),
            if (schedule.isEnabled) ...[
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: _TimePickerButton(
                      label: 'Start Time',
                      time: schedule.startTime,
                      onTimeChanged: (time) {
                        onScheduleChanged(schedule.copyWith(startTime: time));
                      },
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: _TimePickerButton(
                      label: 'End Time',
                      time: schedule.endTime,
                      onTimeChanged: (time) {
                        onScheduleChanged(schedule.copyWith(endTime: time));
                      },
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _getDayDisplayName(String dayKey) {
    switch (dayKey) {
      case 'monday': return 'Monday';
      case 'tuesday': return 'Tuesday';
      case 'wednesday': return 'Wednesday';
      case 'thursday': return 'Thursday';
      case 'friday': return 'Friday';
      case 'saturday': return 'Saturday';
      case 'sunday': return 'Sunday';
      default: return dayKey;
    }
  }
}

class _TimePickerButton extends StatelessWidget {
  final String label;
  final CustomTimeOfDay time;
  final Function(CustomTimeOfDay) onTimeChanged;

  const _TimePickerButton({
    required this.label,
    required this.time,
    required this.onTimeChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 4),
        InkWell(
        onTap: () async {
          final TimeOfDay? pickedTime = await showTimePicker(
            context: context,
            initialTime: TimeOfDay(hour: time.hour, minute: time.minute),
          );
          if (pickedTime != null) {
            onTimeChanged(CustomTimeOfDay(hour: pickedTime.hour, minute: pickedTime.minute));
          }
        },
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              border: Border.all(
                color: Theme.of(context).colorScheme.outline,
              ),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  time.toString(),
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
                Icon(
                  Icons.access_time,
                  size: 16,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

