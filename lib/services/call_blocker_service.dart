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

  Future<void> requestCallScreeningPermission() async {
    try {
      await _channel.invokeMethod('requestCallScreeningPermission');
    } catch (e) {
      print('Error requesting call screening permission: $e');
    }
  }

  Future<bool> isCallScreeningEnabled() async {
    try {
      return await _channel.invokeMethod('isCallScreeningEnabled') ?? false;
    } catch (e) {
      print('Error checking call screening status: $e');
      return false;
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
    try {
      // Log the blocked call for debugging
      print('Attempting to block call from: $phoneNumber');
      
      // Call the native Android method
      await _channel.invokeMethod('blockCall', {'phoneNumber': phoneNumber});
      
      // Note: Actual call blocking on Android 10+ requires:
      // 1. Call Screening Service (implemented above)
      // 2. User to set the app as default call screening app
      // 3. Proper permissions and Play Store compliance
      
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

