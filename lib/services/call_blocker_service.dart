import 'package:flutter/services.dart';
import '../domain/app_settings_use_case.dart';

class CallBlockerService {
  static const MethodChannel _channel = MethodChannel('call_blocker_service');
  static const MethodChannel _phoneChannel = MethodChannel('phone_state_receiver');
  static const MethodChannel _smsChannel = MethodChannel('sms_receiver');
  
  final AppSettingsUseCase _settingsUseCase;
  
  bool _isServiceRunning = false;
  
  CallBlockerService(this._settingsUseCase);

  Future<void> initialize() async {
    _phoneChannel.setMethodCallHandler(_handlePhoneStateCall);
    _smsChannel.setMethodCallHandler(_handleSMSCall);
  }

  Future<void> startService() async {
    if (_isServiceRunning) return;
    
    try {
      await _channel.invokeMethod('startService');
      _isServiceRunning = true;
    } catch (e) {
      print('Error starting service: $e');
    }
  }

  Future<void> stopService() async {
    if (!_isServiceRunning) return;
    
    try {
      await _channel.invokeMethod('stopService');
      _isServiceRunning = false;
    } catch (e) {
      print('Error stopping service: $e');
    }
  }

  Future<void> _handlePhoneStateCall(MethodCall call) async {
    switch (call.method) {
      case 'onIncomingCall':
        await _handleIncomingCall(call.arguments);
        break;
    }
  }

  Future<void> _handleSMSCall(MethodCall call) async {
    switch (call.method) {
      case 'onSMSReceived':
        await _handleSMSReceived(call.arguments);
        break;
    }
  }

  Future<void> _handleIncomingCall(Map<dynamic, dynamic> arguments) async {
    final phoneNumber = arguments['phoneNumber'] as String?;
    
    if (phoneNumber == null) return;
    
    final settings = await _settingsUseCase.getSettings();
    final weeklySchedule = await _settingsUseCase.getWeeklySchedule();
    
    if (!settings.isEnabled || !settings.blockCalls) return;
    
    if (weeklySchedule.shouldBlockCall()) {
      await _blockCall(phoneNumber);
      print('Blocked call from: $phoneNumber');
    }
  }

  Future<void> _handleSMSReceived(Map<dynamic, dynamic> arguments) async {
    final phoneNumber = arguments['phoneNumber'] as String?;
    
    if (phoneNumber == null) return;
    
    final settings = await _settingsUseCase.getSettings();
    final weeklySchedule = await _settingsUseCase.getWeeklySchedule();
    
    if (!settings.isEnabled || !settings.sendSMS) return;
    
    if (weeklySchedule.shouldSendSMS()) {
      await _sendAutoReply(phoneNumber, settings.customMessage);
      print('Sent auto-reply to: $phoneNumber');
    }
  }

  Future<void> _blockCall(String phoneNumber) async {
    // Note: Actual call blocking requires system-level permissions
    // This is a placeholder for the blocking logic
    try {
      await _channel.invokeMethod('blockCall', {'phoneNumber': phoneNumber});
    } catch (e) {
      print('Error blocking call: $e');
    }
  }

  Future<void> _sendAutoReply(String phoneNumber, String message) async {
    try {
      await _channel.invokeMethod('sendSMS', {
        'phoneNumber': phoneNumber,
        'message': message,
      });
      print('SMS sent successfully to $phoneNumber');
    } catch (e) {
      print('Error sending SMS: $e');
    }
  }

  bool get isServiceRunning => _isServiceRunning;
}

