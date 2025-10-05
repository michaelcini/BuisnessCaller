import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/log_entry.dart';

class LogService {
  static const String _logsKey = 'app_logs';
  static const int _maxLogs = 1000; // Keep only last 1000 logs

  static final LogService _instance = LogService._internal();
  factory LogService() => _instance;
  LogService._internal();

  final List<LogEntry> _logs = [];

  List<LogEntry> get logs => List.unmodifiable(_logs);

  Future<void> initialize() async {
    await _loadLogs();
  }

  Future<void> addLog(LogEntry logEntry) async {
    _logs.insert(0, logEntry); // Add to beginning for newest first
    
    // Keep only the last _maxLogs entries
    if (_logs.length > _maxLogs) {
      _logs.removeRange(_maxLogs, _logs.length);
    }
    
    await _saveLogs();
    print('[LOG] ${logEntry.level.displayName}: ${logEntry.message}');
  }

  Future<void> addInfo(String message, {String? details, String? phoneNumber}) async {
    await addLog(LogEntry.info(message, details: details, phoneNumber: phoneNumber));
  }

  Future<void> addWarning(String message, {String? details, String? phoneNumber}) async {
    await addLog(LogEntry.warning(message, details: details, phoneNumber: phoneNumber));
  }

  Future<void> addError(String message, {String? details, String? phoneNumber}) async {
    await addLog(LogEntry.error(message, details: details, phoneNumber: phoneNumber));
  }

  Future<void> addSuccess(String message, {String? details, String? phoneNumber}) async {
    await addLog(LogEntry.success(message, details: details, phoneNumber: phoneNumber));
  }

  Future<void> clearLogs() async {
    _logs.clear();
    await _saveLogs();
  }

  List<LogEntry> getLogsByLevel(LogLevel level) {
    return _logs.where((log) => log.level == level).toList();
  }

  List<LogEntry> getLogsByPhoneNumber(String phoneNumber) {
    return _logs.where((log) => log.phoneNumber == phoneNumber).toList();
  }

  Future<void> _loadLogs() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final logsJson = prefs.getString(_logsKey);
      
      if (logsJson != null) {
        final List<dynamic> logsList = json.decode(logsJson);
        _logs.clear();
        _logs.addAll(logsList.map((json) => LogEntry.fromJson(json)));
      }
    } catch (e) {
      print('Error loading logs: $e');
    }
  }

  Future<void> _saveLogs() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final logsJson = json.encode(_logs.map((log) => log.toJson()).toList());
      await prefs.setString(_logsKey, logsJson);
    } catch (e) {
      print('Error saving logs: $e');
    }
  }
}
