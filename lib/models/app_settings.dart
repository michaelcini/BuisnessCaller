class AppSettings {
  final bool isEnabled;
  final Map<String, DaySchedule> weeklySchedule;
  final String customMessage;
  final bool blockCalls;
  final bool sendSMS;
  final bool isFirstRun;

  AppSettings({
    required this.isEnabled,
    required this.weeklySchedule,
    required this.customMessage,
    required this.blockCalls,
    required this.sendSMS,
    required this.isFirstRun,
  });

  factory AppSettings.defaultSettings() {
    return AppSettings(
      isEnabled: false,
      weeklySchedule: {
        'monday': DaySchedule.defaultSchedule(),
        'tuesday': DaySchedule.defaultSchedule(),
        'wednesday': DaySchedule.defaultSchedule(),
        'thursday': DaySchedule.defaultSchedule(),
        'friday': DaySchedule.defaultSchedule(),
        'saturday': DaySchedule.defaultSchedule(),
        'sunday': DaySchedule.defaultSchedule(),
      },
      customMessage: 'I am currently unavailable. Please call back during business hours.',
      blockCalls: true,
      sendSMS: true,
      isFirstRun: true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'isEnabled': isEnabled,
      'weeklySchedule': weeklySchedule.map((key, value) => MapEntry(key, value.toJson())),
      'customMessage': customMessage,
      'blockCalls': blockCalls,
      'sendSMS': sendSMS,
      'isFirstRun': isFirstRun,
    };
  }

  factory AppSettings.fromJson(Map<String, dynamic> json) {
    return AppSettings(
      isEnabled: json['isEnabled'] ?? false,
      weeklySchedule: (json['weeklySchedule'] as Map<String, dynamic>).map(
        (key, value) => MapEntry(key, DaySchedule.fromJson(value)),
      ),
      customMessage: json['customMessage'] ?? '',
      blockCalls: json['blockCalls'] ?? true,
      sendSMS: json['sendSMS'] ?? true,
      isFirstRun: json['isFirstRun'] ?? true,
    );
  }

  AppSettings copyWith({
    bool? isEnabled,
    Map<String, DaySchedule>? weeklySchedule,
    String? customMessage,
    bool? blockCalls,
    bool? sendSMS,
    bool? isFirstRun,
  }) {
    return AppSettings(
      isEnabled: isEnabled ?? this.isEnabled,
      weeklySchedule: weeklySchedule ?? this.weeklySchedule,
      customMessage: customMessage ?? this.customMessage,
      blockCalls: blockCalls ?? this.blockCalls,
      sendSMS: sendSMS ?? this.sendSMS,
      isFirstRun: isFirstRun ?? this.isFirstRun,
    );
  }
}

class DaySchedule {
  final bool isEnabled;
  final CustomTimeOfDay startTime;
  final CustomTimeOfDay endTime;

  DaySchedule({
    required this.isEnabled,
    required this.startTime,
    required this.endTime,
  });

  factory DaySchedule.defaultSchedule() {
    return DaySchedule(
      isEnabled: true,
      startTime: CustomTimeOfDay(hour: 9, minute: 0),
      endTime: CustomTimeOfDay(hour: 17, minute: 0),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'isEnabled': isEnabled,
      'startTime': {
        'hour': startTime.hour,
        'minute': startTime.minute,
      },
      'endTime': {
        'hour': endTime.hour,
        'minute': endTime.minute,
      },
    };
  }

  factory DaySchedule.fromJson(Map<String, dynamic> json) {
    return DaySchedule(
      isEnabled: json['isEnabled'] ?? true,
      startTime: CustomTimeOfDay(
        hour: json['startTime']['hour'] ?? 9,
        minute: json['startTime']['minute'] ?? 0,
      ),
      endTime: CustomTimeOfDay(
        hour: json['endTime']['hour'] ?? 17,
        minute: json['endTime']['minute'] ?? 0,
      ),
    );
  }

  DaySchedule copyWith({
    bool? isEnabled,
    CustomTimeOfDay? startTime,
    CustomTimeOfDay? endTime,
  }) {
    return DaySchedule(
      isEnabled: isEnabled ?? this.isEnabled,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
    );
  }
}

class CustomTimeOfDay {
  final int hour;
  final int minute;

  CustomTimeOfDay({required this.hour, required this.minute});

  @override
  String toString() {
    return '${hour.toString().padLeft(2, '0')}:${minute.toString().padLeft(2, '0')}';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is CustomTimeOfDay && other.hour == hour && other.minute == minute;
  }

  @override
  int get hashCode => hour.hashCode ^ minute.hashCode;
}
