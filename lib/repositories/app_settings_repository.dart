import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/app_settings.dart';

class AppSettingsRepository {
  static const String _settingsKey = 'app_settings';

  Future<AppSettings> getSettings() async {
    final prefs = await SharedPreferences.getInstance();
    final settingsJson = prefs.getString(_settingsKey);
    
    if (settingsJson != null) {
      final settingsMap = json.decode(settingsJson) as Map<String, dynamic>;
      return AppSettings.fromJson(settingsMap);
    }
    
    return AppSettings.defaultSettings();
  }

  Future<void> saveSettings(AppSettings settings) async {
    final prefs = await SharedPreferences.getInstance();
    final settingsJson = json.encode(settings.toJson());
    await prefs.setString(_settingsKey, settingsJson);
    
    // Also save individual settings for Android service access
    await _saveSettingsForAndroidService(prefs, settings);
  }

  Future<void> _saveSettingsForAndroidService(SharedPreferences prefs, AppSettings settings) async {
    // Save main settings
    await prefs.setBool('isEnabled', settings.isEnabled);
    await prefs.setBool('blockCalls', settings.blockCalls);
    await prefs.setBool('sendSMS', settings.sendSMS);
    await prefs.setString('customMessage', settings.customMessage);
    
    // Save weekly schedule
    for (final entry in settings.weeklySchedule.entries) {
      final day = entry.key;
      final schedule = entry.value;
      
      await prefs.setBool('${day}_enabled', schedule.isEnabled);
      await prefs.setInt('${day}_startHour', schedule.startTime.hour);
      await prefs.setInt('${day}_startMinute', schedule.startTime.minute);
      await prefs.setInt('${day}_endHour', schedule.endTime.hour);
      await prefs.setInt('${day}_endMinute', schedule.endTime.minute);
    }
  }

  Future<void> updateSettings(AppSettings Function(AppSettings) updater) async {
    final currentSettings = await getSettings();
    final updatedSettings = updater(currentSettings);
    await saveSettings(updatedSettings);
  }

  Future<void> clearSettings() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_settingsKey);
  }
}

