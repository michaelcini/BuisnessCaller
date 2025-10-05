class LogEntry {
  final String id;
  final DateTime timestamp;
  final LogLevel level;
  final String message;
  final String? details;
  final String? phoneNumber;

  LogEntry({
    required this.id,
    required this.timestamp,
    required this.level,
    required this.message,
    this.details,
    this.phoneNumber,
  });

  factory LogEntry.info(String message, {String? details, String? phoneNumber}) {
    return LogEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      timestamp: DateTime.now(),
      level: LogLevel.info,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
    );
  }

  factory LogEntry.warning(String message, {String? details, String? phoneNumber}) {
    return LogEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      timestamp: DateTime.now(),
      level: LogLevel.warning,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
    );
  }

  factory LogEntry.error(String message, {String? details, String? phoneNumber}) {
    return LogEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      timestamp: DateTime.now(),
      level: LogLevel.error,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
    );
  }

  factory LogEntry.success(String message, {String? details, String? phoneNumber}) {
    return LogEntry(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      timestamp: DateTime.now(),
      level: LogLevel.success,
      message: message,
      details: details,
      phoneNumber: phoneNumber,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'timestamp': timestamp.toIso8601String(),
      'level': level.name,
      'message': message,
      'details': details,
      'phoneNumber': phoneNumber,
    };
  }

  factory LogEntry.fromJson(Map<String, dynamic> json) {
    return LogEntry(
      id: json['id'],
      timestamp: DateTime.parse(json['timestamp']),
      level: LogLevel.values.firstWhere((e) => e.name == json['level']),
      message: json['message'],
      details: json['details'],
      phoneNumber: json['phoneNumber'],
    );
  }

  String get formattedTime {
    return '${timestamp.hour.toString().padLeft(2, '0')}:${timestamp.minute.toString().padLeft(2, '0')}:${timestamp.second.toString().padLeft(2, '0')}';
  }

  String get formattedDate {
    return '${timestamp.day}/${timestamp.month}/${timestamp.year}';
  }
}

enum LogLevel {
  info,
  warning,
  error,
  success,
}

extension LogLevelExtension on LogLevel {
  String get displayName {
    switch (this) {
      case LogLevel.info:
        return 'INFO';
      case LogLevel.warning:
        return 'WARN';
      case LogLevel.error:
        return 'ERROR';
      case LogLevel.success:
        return 'SUCCESS';
    }
  }
}
