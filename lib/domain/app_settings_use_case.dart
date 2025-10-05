import '../models/app_settings.dart';
import '../repositories/app_settings_repository.dart';

class WeeklySchedule {
  final Map<String, DaySchedule> schedule;

  WeeklySchedule({required this.schedule});

  bool isBusinessHours() {
    final now = DateTime.now();
    final dayName = _getDayName(now.weekday);
    final daySchedule = schedule[dayName];

    if (daySchedule == null || !daySchedule.isEnabled) {
      return false;
    }

    final currentTime = CustomTimeOfDay(hour: now.hour, minute: now.minute);
    return _isTimeInRange(currentTime, daySchedule.startTime, daySchedule.endTime);
  }

  bool shouldBlockCall() {
    return !isBusinessHours();
  }

  bool shouldSendSMS() {
    return !isBusinessHours();
  }

  String _getDayName(int weekday) {
    switch (weekday) {
      case 1: return 'monday';
      case 2: return 'tuesday';
      case 3: return 'wednesday';
      case 4: return 'thursday';
      case 5: return 'friday';
      case 6: return 'saturday';
      case 7: return 'sunday';
      default: return 'monday';
    }
  }

  bool _isTimeInRange(CustomTimeOfDay current, CustomTimeOfDay start, CustomTimeOfDay end) {
    final currentMinutes = current.hour * 60 + current.minute;
    final startMinutes = start.hour * 60 + start.minute;
    final endMinutes = end.hour * 60 + end.minute;

    if (startMinutes <= endMinutes) {
      return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
    } else {
      // Handle overnight schedules (e.g., 22:00 to 06:00)
      return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
    }
  }
}

class AppSettingsUseCase {
  final AppSettingsRepository _repository;

  AppSettingsUseCase(this._repository);

  Future<AppSettings> getSettings() async {
    return await _repository.getSettings();
  }

  Future<void> saveSettings(AppSettings settings) async {
    await _repository.saveSettings(settings);
  }

  Future<void> updateSettings(AppSettings Function(AppSettings) updater) async {
    await _repository.updateSettings(updater);
  }

  Future<WeeklySchedule> getWeeklySchedule() async {
    final settings = await getSettings();
    return WeeklySchedule(schedule: settings.weeklySchedule);
  }

  Future<bool> isCallBlockingEnabled() async {
    final settings = await getSettings();
    return settings.isEnabled && settings.blockCalls;
  }

  Future<bool> isSMSReplyEnabled() async {
    final settings = await getSettings();
    return settings.isEnabled && settings.sendSMS;
  }

  Future<String> getCustomMessage() async {
    final settings = await getSettings();
    return settings.customMessage;
  }

  Future<void> enableCallBlocking(bool enabled) async {
    await updateSettings((settings) => settings.copyWith(isEnabled: enabled));
  }

  Future<void> enableSMSReply(bool enabled) async {
    await updateSettings((settings) => settings.copyWith(sendSMS: enabled));
  }

  Future<void> setCustomMessage(String message) async {
    await updateSettings((settings) => settings.copyWith(customMessage: message));
  }

  Future<void> updateDaySchedule(String day, DaySchedule schedule) async {
    await updateSettings((settings) {
      final updatedSchedule = Map<String, DaySchedule>.from(settings.weeklySchedule);
      updatedSchedule[day] = schedule;
      return settings.copyWith(weeklySchedule: updatedSchedule);
    });
  }

  Future<void> markFirstRunComplete() async {
    await updateSettings((settings) => settings.copyWith(isFirstRun: false));
  }
}
