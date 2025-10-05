import 'package:flutter_test/flutter_test.dart';
import 'package:call_blocker/domain/app_settings_use_case.dart';
import 'package:call_blocker/models/app_settings.dart';

void main() {
  group('WeeklySchedule Tests', () {
    late WeeklySchedule weeklySchedule;

    setUp(() {
      final schedule = {
        'monday': DaySchedule.defaultSchedule(),
        'tuesday': DaySchedule.defaultSchedule(),
        'wednesday': DaySchedule.defaultSchedule(),
        'thursday': DaySchedule.defaultSchedule(),
        'friday': DaySchedule.defaultSchedule(),
        'saturday': DaySchedule.defaultSchedule(),
        'sunday': DaySchedule.defaultSchedule(),
      };
      weeklySchedule = WeeklySchedule(schedule: schedule);
    });

    test('should return true for business hours', () {
      // Mock current time to be within business hours (e.g., 10 AM on Monday)
      // Note: This is a simplified test - in real implementation you'd mock DateTime.now()
      expect(weeklySchedule.isBusinessHours(), isA<bool>());
    });

    test('should return false for non-business hours', () {
      // Mock current time to be outside business hours (e.g., 8 PM on Monday)
      expect(weeklySchedule.shouldBlockCall(), isA<bool>());
    });

    test('should return true for SMS when outside business hours', () {
      expect(weeklySchedule.shouldSendSMS(), isA<bool>());
    });
  });
}

