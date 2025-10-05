import 'package:flutter_test/flutter_test.dart';
import 'package:call_blocker/models/app_settings.dart';

void main() {
  group('AppSettings Tests', () {
    test('should create default settings', () {
      final settings = AppSettings.defaultSettings();
      
      expect(settings.isEnabled, false);
      expect(settings.blockCalls, true);
      expect(settings.sendSMS, true);
      expect(settings.isFirstRun, true);
      expect(settings.customMessage, isNotEmpty);
      expect(settings.weeklySchedule.length, 7);
    });

    test('should serialize and deserialize correctly', () {
      final originalSettings = AppSettings.defaultSettings();
      final json = originalSettings.toJson();
      final restoredSettings = AppSettings.fromJson(json);
      
      expect(restoredSettings.isEnabled, originalSettings.isEnabled);
      expect(restoredSettings.blockCalls, originalSettings.blockCalls);
      expect(restoredSettings.sendSMS, originalSettings.sendSMS);
      expect(restoredSettings.customMessage, originalSettings.customMessage);
    });

    test('should copy with new values', () {
      final originalSettings = AppSettings.defaultSettings();
      final updatedSettings = originalSettings.copyWith(
        isEnabled: true,
        customMessage: 'Test message',
      );
      
      expect(updatedSettings.isEnabled, true);
      expect(updatedSettings.customMessage, 'Test message');
      expect(updatedSettings.blockCalls, originalSettings.blockCalls);
    });
  });

  group('DaySchedule Tests', () {
    test('should create default schedule', () {
      final schedule = DaySchedule.defaultSchedule();
      
      expect(schedule.isEnabled, true);
      expect(schedule.startTime.hour, 9);
      expect(schedule.startTime.minute, 0);
      expect(schedule.endTime.hour, 17);
      expect(schedule.endTime.minute, 0);
    });

    test('should serialize and deserialize correctly', () {
      final originalSchedule = DaySchedule.defaultSchedule();
      final json = originalSchedule.toJson();
      final restoredSchedule = DaySchedule.fromJson(json);
      
      expect(restoredSchedule.isEnabled, originalSchedule.isEnabled);
      expect(restoredSchedule.startTime.hour, originalSchedule.startTime.hour);
      expect(restoredSchedule.endTime.hour, originalSchedule.endTime.hour);
    });
  });

  group('CustomTimeOfDay Tests', () {
    test('should format time correctly', () {
      final time = CustomTimeOfDay(hour: 9, minute: 30);
      expect(time.toString(), '09:30');
      
      final time2 = CustomTimeOfDay(hour: 14, minute: 5);
      expect(time2.toString(), '14:05');
    });

    test('should compare times correctly', () {
      final time1 = CustomTimeOfDay(hour: 9, minute: 30);
      final time2 = CustomTimeOfDay(hour: 9, minute: 30);
      final time3 = CustomTimeOfDay(hour: 10, minute: 30);
      
      expect(time1, equals(time2));
      expect(time1, isNot(equals(time3)));
    });
  });
}

