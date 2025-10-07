import 'dart:collection';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class LogcatEntry {
  final int timestamp;
  final String level;
  final String tag;
  final String message;
  final int pid;
  final int tid;

  LogcatEntry({
    required this.timestamp,
    required this.level,
    required this.tag,
    required this.message,
    required this.pid,
    required this.tid,
  });

  String get formattedTime {
    final date = DateTime.fromMillisecondsSinceEpoch(timestamp);
    return '${date.hour.toString().padLeft(2, '0')}:'
           '${date.minute.toString().padLeft(2, '0')}:'
           '${date.second.toString().padLeft(2, '0')}.'
           '${date.millisecond.toString().padLeft(3, '0')}';
  }

  String get fullMessage => '[$formattedTime] [$level] [$tag] [$pid:$tid] $message';

  Map<String, dynamic> toJson() => {
    'timestamp': timestamp,
    'level': level,
    'tag': tag,
    'message': message,
    'pid': pid,
    'tid': tid,
  };

  factory LogcatEntry.fromJson(Map<String, dynamic> json) => LogcatEntry(
    timestamp: json['timestamp'],
    level: json['level'],
    tag: json['tag'],
    message: json['message'],
    pid: json['pid'],
    tid: json['tid'],
  );
}

class SystemEvent {
  final int timestamp;
  final String type;
  final String category;
  final String message;
  final Map<String, dynamic> data;

  SystemEvent({
    required this.timestamp,
    required this.type,
    required this.category,
    required this.message,
    required this.data,
  });

  String get formattedTime {
    final date = DateTime.fromMillisecondsSinceEpoch(timestamp);
    return '${date.hour.toString().padLeft(2, '0')}:'
           '${date.minute.toString().padLeft(2, '0')}:'
           '${date.second.toString().padLeft(2, '0')}.'
           '${date.millisecond.toString().padLeft(3, '0')}';
  }

  String get fullMessage {
    final dataStr = data.isNotEmpty ? ' - ${data.toString()}' : '';
    return '[$formattedTime] [$category] [$type] $message$dataStr';
  }

  Map<String, dynamic> toJson() => {
    'timestamp': timestamp,
    'type': type,
    'category': category,
    'message': message,
    'data': data,
  };

  factory SystemEvent.fromJson(Map<String, dynamic> json) => SystemEvent(
    timestamp: json['timestamp'],
    type: json['type'],
    category: json['category'],
    message: json['message'],
    data: Map<String, dynamic>.from(json['data'] ?? {}),
  );
}

class SuperLogEntry {
  final DateTime timestamp;
  final String level;
  final String tag;
  final String message;
  final String? details;
  final String? phoneNumber;
  final String source; // 'APP', 'LOGCAT', 'SYSTEM'

  SuperLogEntry({
    required this.timestamp,
    required this.level,
    required this.tag,
    required this.message,
    this.details,
    this.phoneNumber,
    this.source = 'APP',
  });

  String get formattedTime => 
      '${timestamp.hour.toString().padLeft(2, '0')}:'
      '${timestamp.minute.toString().padLeft(2, '0')}:'
      '${timestamp.second.toString().padLeft(2, '0')}.'
      '${timestamp.millisecond.toString().padLeft(3, '0')}';

  String get fullMessage {
    String result = '[$formattedTime] [$source] [$level] [$tag] $message';
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
    'source': source,
  };

  factory SuperLogEntry.fromJson(Map<String, dynamic> json) => SuperLogEntry(
    timestamp: DateTime.parse(json['timestamp']),
    level: json['level'],
    tag: json['tag'],
    message: json['message'],
    details: json['details'],
    phoneNumber: json['phoneNumber'],
    source: json['source'] ?? 'APP',
  );
}

class EnhancedSuperLogService extends ChangeNotifier {
  static const int _maxLogs = 50000; // Much larger for comprehensive logging
  final Queue<SuperLogEntry> _logs = Queue<SuperLogEntry>();
  final Queue<LogcatEntry> _logcatEntries = Queue<LogcatEntry>();
  final Queue<SystemEvent> _systemEvents = Queue<SystemEvent>();
  
  final List<String> _filteredTags = [];
  String _searchQuery = '';
  String _levelFilter = 'ALL';
  String _sourceFilter = 'ALL';
  bool _isLogcatEnabled = false;
  bool _isSystemEventsEnabled = false;
  
  Map<String, dynamic> _logStats = {};
  Map<String, dynamic> _systemEventStats = {};

  static const MethodChannel _channel = MethodChannel('call_blocker_service');

  List<SuperLogEntry> get logs => _logs.toList().reversed.toList();
  List<LogcatEntry> get logcatEntries => _logcatEntries.toList().reversed.toList();
  List<SystemEvent> get systemEvents => _systemEvents.toList().reversed.toList();
  
  List<SuperLogEntry> get filteredLogs {
    var filtered = logs;
    
    // Filter by level
    if (_levelFilter != 'ALL') {
      filtered = filtered.where((log) => log.level == _levelFilter).toList();
    }
    
    // Filter by source
    if (_sourceFilter != 'ALL') {
      filtered = filtered.where((log) => log.source == _sourceFilter).toList();
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

  List<String> get availableLevels => ['ALL', 'VERBOSE', 'DEBUG', 'INFO', 'WARNING', 'ERROR', 'FATAL'];
  List<String> get availableSources => ['ALL', 'APP', 'LOGCAT', 'SYSTEM'];

  String get searchQuery => _searchQuery;
  String get levelFilter => _levelFilter;
  String get sourceFilter => _sourceFilter;
  List<String> get filteredTags => List.from(_filteredTags);
  bool get isLogcatEnabled => _isLogcatEnabled;
  bool get isSystemEventsEnabled => _isSystemEventsEnabled;
  Map<String, dynamic> get logStats => _logStats;
  Map<String, dynamic> get systemEventStats => _systemEventStats;

  void addLog(String level, String tag, String message, {String? details, String? phoneNumber}) {
    final entry = SuperLogEntry(
      timestamp: DateTime.now(),
      level: level,
      tag: tag,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
      source: 'APP',
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

  void addLogcatEntry(LogcatEntry entry) {
    _logcatEntries.add(entry);
    
    // Keep only the latest entries
    while (_logcatEntries.length > _maxLogs) {
      _logcatEntries.removeFirst();
    }
    
    // Convert to SuperLogEntry for unified view
    final superLogEntry = SuperLogEntry(
      timestamp: DateTime.fromMillisecondsSinceEpoch(entry.timestamp),
      level: entry.level,
      tag: entry.tag,
      message: entry.message,
      source: 'LOGCAT',
    );
    
    _logs.add(superLogEntry);
    
    // Keep only the latest logs
    while (_logs.length > _maxLogs) {
      _logs.removeFirst();
    }
    
    notifyListeners();
  }

  void addSystemEvent(SystemEvent event) {
    _systemEvents.add(event);
    
    // Keep only the latest events
    while (_systemEvents.length > _maxLogs) {
      _systemEvents.removeFirst();
    }
    
    // Convert to SuperLogEntry for unified view
    final superLogEntry = SuperLogEntry(
      timestamp: DateTime.fromMillisecondsSinceEpoch(event.timestamp),
      level: 'INFO',
      tag: event.category,
      message: event.message,
      details: event.data.isNotEmpty ? event.data.toString() : null,
      source: 'SYSTEM',
    );
    
    _logs.add(superLogEntry);
    
    // Keep only the latest logs
    while (_logs.length > _maxLogs) {
      _logs.removeFirst();
    }
    
    notifyListeners();
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void setLevelFilter(String level) {
    _levelFilter = level;
    notifyListeners();
  }

  void setSourceFilter(String source) {
    _sourceFilter = source;
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
    _sourceFilter = 'ALL';
    _filteredTags.clear();
    notifyListeners();
  }

  void clearLogs() {
    _logs.clear();
    _logcatEntries.clear();
    _systemEvents.clear();
    notifyListeners();
  }

  Future<void> startLogcat() async {
    try {
      await _channel.invokeMethod('startLogcat');
      _isLogcatEnabled = true;
      addInfo('ENHANCED_LOG', 'Logcat logging started');
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to start logcat: $e');
    }
  }

  Future<void> stopLogcat() async {
    try {
      await _channel.invokeMethod('stopLogcat');
      _isLogcatEnabled = false;
      addInfo('ENHANCED_LOG', 'Logcat logging stopped');
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to stop logcat: $e');
    }
  }

  Future<void> startSystemEvents() async {
    try {
      await _channel.invokeMethod('startSystemEvents');
      _isSystemEventsEnabled = true;
      addInfo('ENHANCED_LOG', 'System events logging started');
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to start system events: $e');
    }
  }

  Future<void> stopSystemEvents() async {
    try {
      await _channel.invokeMethod('stopSystemEvents');
      _isSystemEventsEnabled = false;
      addInfo('ENHANCED_LOG', 'System events logging stopped');
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to stop system events: $e');
    }
  }

  Future<void> getLogStats() async {
    try {
      final stats = await _channel.invokeMethod('getLogStats');
      _logStats = Map<String, dynamic>.from(stats);
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to get log stats: $e');
    }
  }

  Future<void> getSystemEventStats() async {
    try {
      final stats = await _channel.invokeMethod('getSystemEventStats');
      _systemEventStats = Map<String, dynamic>.from(stats);
      notifyListeners();
    } catch (e) {
      addError('ENHANCED_LOG', 'Failed to get system event stats: $e');
    }
  }

  String exportLogs() {
    final buffer = StringBuffer();
    buffer.writeln('=== ENHANCED SUPER LOG EXPORT ===');
    buffer.writeln('Generated: ${DateTime.now().toIso8601String()}');
    buffer.writeln('Total app logs: ${_logs.where((l) => l.source == 'APP').length}');
    buffer.writeln('Total logcat entries: ${_logcatEntries.length}');
    buffer.writeln('Total system events: ${_systemEvents.length}');
    buffer.writeln('Logcat enabled: $_isLogcatEnabled');
    buffer.writeln('System events enabled: $_isSystemEventsEnabled');
    buffer.writeln('==================================\n');
    
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

  // Handle incoming logcat data from native
  void handleLogcatData(List<dynamic> logs) {
    for (final logData in logs) {
      final entry = LogcatEntry.fromJson(Map<String, dynamic>.from(logData));
      addLogcatEntry(entry);
    }
  }

  // Handle incoming system events from native
  void handleSystemEvents(List<dynamic> events) {
    for (final eventData in events) {
      final event = SystemEvent.fromJson(Map<String, dynamic>.from(eventData));
      addSystemEvent(event);
    }
  }
}