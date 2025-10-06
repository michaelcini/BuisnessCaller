import 'dart:async';
import 'dart:collection';
import 'package:flutter/foundation.dart';

class SuperLogEntry {
  final DateTime timestamp;
  final String level;
  final String tag;
  final String message;
  final String? details;
  final String? phoneNumber;

  SuperLogEntry({
    required this.timestamp,
    required this.level,
    required this.tag,
    required this.message,
    this.details,
    this.phoneNumber,
  });

  String get formattedTime => 
      '${timestamp.hour.toString().padLeft(2, '0')}:'
      '${timestamp.minute.toString().padLeft(2, '0')}:'
      '${timestamp.second.toString().padLeft(2, '0')}.'
      '${timestamp.millisecond.toString().padLeft(3, '0')}';

  String get fullMessage {
    String result = '[$formattedTime] [$level] [$tag] $message';
    if (details != null) {
      result += '\n  Details: $details';
    }
    if (phoneNumber != null) {
      result += '\n  Phone: $phoneNumber';
    }
    return result;
  }

  Map<String, dynamic> toJson() => {
    'timestamp': timestamp.toIso8601String(),
    'level': level,
    'tag': tag,
    'message': message,
    'details': details,
    'phoneNumber': phoneNumber,
  };

  factory SuperLogEntry.fromJson(Map<String, dynamic> json) => SuperLogEntry(
    timestamp: DateTime.parse(json['timestamp']),
    level: json['level'],
    tag: json['tag'],
    message: json['message'],
    details: json['details'],
    phoneNumber: json['phoneNumber'],
  );
}

class SuperLogService extends ChangeNotifier {
  static const int _maxLogs = 1000;
  final Queue<SuperLogEntry> _logs = Queue<SuperLogEntry>();
  final List<String> _filteredTags = [];
  String _searchQuery = '';
  String _levelFilter = 'ALL';

  List<SuperLogEntry> get logs => _logs.toList().reversed.toList();
  
  List<SuperLogEntry> get filteredLogs {
    var filtered = logs;
    
    // Filter by level
    if (_levelFilter != 'ALL') {
      filtered = filtered.where((log) => log.level == _levelFilter).toList();
    }
    
    // Filter by tags
    if (_filteredTags.isNotEmpty) {
      filtered = filtered.where((log) => _filteredTags.contains(log.tag)).toList();
    }
    
    // Filter by search query
    if (_searchQuery.isNotEmpty) {
      final query = _searchQuery.toLowerCase();
      filtered = filtered.where((log) => 
        log.message.toLowerCase().contains(query) ||
        log.tag.toLowerCase().contains(query) ||
        (log.details?.toLowerCase().contains(query) ?? false) ||
        (log.phoneNumber?.toLowerCase().contains(query) ?? false)
      ).toList();
    }
    
    return filtered;
  }

  List<String> get availableTags {
    final tags = <String>{};
    for (final log in _logs) {
      tags.add(log.tag);
    }
    return tags.toList()..sort();
  }

  List<String> get availableLevels => ['ALL', 'DEBUG', 'INFO', 'WARNING', 'ERROR'];

  String get searchQuery => _searchQuery;
  String get levelFilter => _levelFilter;
  List<String> get filteredTags => List.from(_filteredTags);

  void addLog(String level, String tag, String message, {String? details, String? phoneNumber}) {
    final entry = SuperLogEntry(
      timestamp: DateTime.now(),
      level: level,
      tag: tag,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
    );
    
    _logs.add(entry);
    
    // Keep only the latest logs
    while (_logs.length > _maxLogs) {
      _logs.removeFirst();
    }
    
    // Also print to console for debugging
    if (kDebugMode) {
      print('SUPERLOG: ${entry.fullMessage}');
    }
    
    notifyListeners();
  }

  void addDebug(String tag, String message, {String? details, String? phoneNumber}) {
    addLog('DEBUG', tag, message, details: details, phoneNumber: phoneNumber);
  }

  void addInfo(String tag, String message, {String? details, String? phoneNumber}) {
    addLog('INFO', tag, message, details: details, phoneNumber: phoneNumber);
  }

  void addWarning(String tag, String message, {String? details, String? phoneNumber}) {
    addLog('WARNING', tag, message, details: details, phoneNumber: phoneNumber);
  }

  void addError(String tag, String message, {String? details, String? phoneNumber}) {
    addLog('ERROR', tag, message, details: details, phoneNumber: phoneNumber);
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void setLevelFilter(String level) {
    _levelFilter = level;
    notifyListeners();
  }

  void toggleTagFilter(String tag) {
    if (_filteredTags.contains(tag)) {
      _filteredTags.remove(tag);
    } else {
      _filteredTags.add(tag);
    }
    notifyListeners();
  }

  void clearFilters() {
    _searchQuery = '';
    _levelFilter = 'ALL';
    _filteredTags.clear();
    notifyListeners();
  }

  void clearLogs() {
    _logs.clear();
    notifyListeners();
  }

  String exportLogs() {
    final buffer = StringBuffer();
    buffer.writeln('=== SUPER LOG EXPORT ===');
    buffer.writeln('Generated: ${DateTime.now().toIso8601String()}');
    buffer.writeln('Total logs: ${_logs.length}');
    buffer.writeln('========================\n');
    
    for (final log in logs) {
      buffer.writeln(log.fullMessage);
      buffer.writeln();
    }
    
    return buffer.toString();
  }

  void addSystemLog(String message) {
    addInfo('SYSTEM', message);
  }

  void addFlutterLog(String message) {
    addInfo('FLUTTER', message);
  }

  void addAndroidLog(String message) {
    addInfo('ANDROID', message);
  }
}