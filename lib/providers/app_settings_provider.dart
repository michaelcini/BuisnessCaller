import 'package:flutter/material.dart';
import '../domain/app_settings_use_case.dart';
import '../models/app_settings.dart';

class AppSettingsProvider extends ChangeNotifier {
  final AppSettingsUseCase _settingsUseCase;
  
  AppSettings _settings = AppSettings.defaultSettings();
  bool _isLoading = false;

  AppSettingsProvider(this._settingsUseCase) {
    _loadSettings();
  }

  AppSettings get settings => _settings;
  bool get isLoading => _isLoading;

  Future<void> _loadSettings() async {
    _isLoading = true;
    notifyListeners();
    
    try {
      _settings = await _settingsUseCase.getSettings();
    } catch (e) {
      print('Error loading settings: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> toggleEnabled(bool enabled) async {
    try {
      await _settingsUseCase.enableCallBlocking(enabled);
      _settings = _settings.copyWith(isEnabled: enabled);
      notifyListeners();
    } catch (e) {
      print('Error toggling enabled state: $e');
    }
  }

  Future<void> toggleCallBlocking(bool enabled) async {
    try {
      await _settingsUseCase.updateSettings((settings) => 
        settings.copyWith(blockCalls: enabled));
      _settings = _settings.copyWith(blockCalls: enabled);
      notifyListeners();
    } catch (e) {
      print('Error toggling call blocking: $e');
    }
  }

  Future<void> toggleSMSReply(bool enabled) async {
    try {
      await _settingsUseCase.enableSMSReply(enabled);
      _settings = _settings.copyWith(sendSMS: enabled);
      notifyListeners();
    } catch (e) {
      print('Error toggling SMS reply: $e');
    }
  }

  Future<void> setCustomMessage(String message) async {
    try {
      await _settingsUseCase.setCustomMessage(message);
      _settings = _settings.copyWith(customMessage: message);
      notifyListeners();
    } catch (e) {
      print('Error setting custom message: $e');
    }
  }

  Future<void> updateDaySchedule(String day, DaySchedule schedule) async {
    try {
      await _settingsUseCase.updateDaySchedule(day, schedule);
      final updatedSchedule = Map<String, DaySchedule>.from(_settings.weeklySchedule);
      updatedSchedule[day] = schedule;
      _settings = _settings.copyWith(weeklySchedule: updatedSchedule);
      notifyListeners();
    } catch (e) {
      print('Error updating day schedule: $e');
    }
  }

  Future<void> markFirstRunComplete() async {
    try {
      await _settingsUseCase.markFirstRunComplete();
      _settings = _settings.copyWith(isFirstRun: false);
      notifyListeners();
    } catch (e) {
      print('Error marking first run complete: $e');
    }
  }

  Future<void> refreshSettings() async {
    await _loadSettings();
  }
}

