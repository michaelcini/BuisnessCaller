import 'package:flutter/services.dart';
import '../domain/app_settings_use_case.dart';
import '../services/log_service.dart';

class CallBlockerService {
  static const MethodChannel _channel = MethodChannel('call_blocker_service');
  static const MethodChannel _phoneChannel = MethodChannel('phone_state_receiver');
  static const MethodChannel _smsChannel = MethodChannel('sms_receiver');
  
  final AppSettingsUseCase _settingsUseCase;
  final LogService _logService = LogService();
  
  bool _isServiceRunning = false;
  
  CallBlockerService(this._settingsUseCase);

  Future<void> initialize() async {
    _phoneChannel.setMethodCallHandler(_handlePhoneStateCall);
    _smsChannel.setMethodCallHandler(_handleSMSCall);
  }

  Future<void> startService() async {
    if (_isServiceRunning) {
      await _logService.addWarning('Service already running');
      return;
    }
    
    try {
      await _channel.invokeMethod('startService');
      _isServiceRunning = true;
      await _logService.addSuccess('Call Blocker service started');
    } catch (e) {
      await _logService.addError('Failed to start service', details: e.toString());
      print('Error starting service: $e');
    }
  }

  Future<void> stopService() async {
    if (!_isServiceRunning) {
      await _logService.addWarning('Service not running');
      return;
    }
    
    try {
      await _channel.invokeMethod('stopService');
      _isServiceRunning = false;
      await _logService.addSuccess('Call Blocker service stopped');
    } catch (e) {
      await _logService.addError('Failed to stop service', details: e.toString());
      print('Error stopping service: $e');
    }
  }

  Future<void> requestCallScreeningPermission() async {
    try {
      await _channel.invokeMethod('requestCallScreeningPermission');
      await _logService.addInfo('Requested call screening permission');
    } catch (e) {
      await _logService.addError('Failed to request call screening permission', details: e.toString());
      print('Error requesting call screening permission: $e');
    }
  }

  Future<bool> isCallScreeningEnabled() async {
    try {
      final isEnabled = await _channel.invokeMethod('isCallScreeningEnabled') ?? false;
      await _logService.addInfo('Call screening status checked', details: 'Enabled: $isEnabled');
      return isEnabled;
    } catch (e) {
      await _logService.addError('Failed to check call screening status', details: e.toString());
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
    
    if (phoneNumber == null) {
      await _logService.addWarning('Incoming call with no phone number');
      return;
    }
    
    await _logService.addInfo('Incoming call detected', phoneNumber: phoneNumber);
    
    final settings = await _settingsUseCase.getSettings();
    final weeklySchedule = await _settingsUseCase.getWeeklySchedule();
    
    if (!settings.isEnabled) {
      await _logService.addInfo('App is disabled, allowing call', phoneNumber: phoneNumber);
      return;
    }
    
    if (!settings.blockCalls) {
      await _logService.addInfo('Call blocking is disabled, allowing call', phoneNumber: phoneNumber);
      return;
    }
    
    if (weeklySchedule.shouldBlockCall()) {
      await _logService.addInfo('Outside business hours, attempting to block call', phoneNumber: phoneNumber);
      await _blockCall(phoneNumber);
    } else {
      await _logService.addInfo('Within business hours, allowing call', phoneNumber: phoneNumber);
    }
  }

  Future<void> _handleSMSReceived(Map<dynamic, dynamic> arguments) async {
    final phoneNumber = arguments['phoneNumber'] as String?;
    
    if (phoneNumber == null) {
      await _logService.addWarning('SMS received with no phone number');
      return;
    }
    
    await _logService.addInfo('SMS received', phoneNumber: phoneNumber);
    
    final settings = await _settingsUseCase.getSettings();
    final weeklySchedule = await _settingsUseCase.getWeeklySchedule();
    
    if (!settings.isEnabled) {
      await _logService.addInfo('App is disabled, not sending auto-reply', phoneNumber: phoneNumber);
      return;
    }
    
    if (!settings.sendSMS) {
      await _logService.addInfo('SMS auto-reply is disabled', phoneNumber: phoneNumber);
      return;
    }
    
    if (weeklySchedule.shouldSendSMS()) {
      await _logService.addInfo('Outside business hours, sending auto-reply', phoneNumber: phoneNumber);
      await _sendAutoReply(phoneNumber, settings.customMessage);
    } else {
      await _logService.addInfo('Within business hours, not sending auto-reply', phoneNumber: phoneNumber);
    }
  }

  Future<void> _blockCall(String phoneNumber) async {
    try {
      await _logService.addInfo('Attempting to block call', phoneNumber: phoneNumber);
      
      // Call the native Android method
      await _channel.invokeMethod('blockCall', {'phoneNumber': phoneNumber});
      
      await _logService.addSuccess('Call blocking request sent', phoneNumber: phoneNumber);
      
      // Note: Actual call blocking on Android 10+ requires:
      // 1. Call Screening Service (implemented above)
      // 2. User to set the app as default call screening app
      // 3. Proper permissions and Play Store compliance
      
    } catch (e) {
      await _logService.addError('Failed to block call', details: e.toString(), phoneNumber: phoneNumber);
      print('Error blocking call: $e');
    }
  }

  Future<void> _sendAutoReply(String phoneNumber, String message) async {
    try {
      await _logService.addInfo('Attempting to send auto-reply', phoneNumber: phoneNumber, details: 'Message: $message');
      
      await _channel.invokeMethod('sendSMS', {
        'phoneNumber': phoneNumber,
        'message': message,
      });
      
      await _logService.addSuccess('Auto-reply sent successfully', phoneNumber: phoneNumber);
      print('SMS sent successfully to $phoneNumber');
    } catch (e) {
      await _logService.addError('Failed to send auto-reply', details: e.toString(), phoneNumber: phoneNumber);
      print('Error sending SMS: $e');
    }
  }

  bool get isServiceRunning => _isServiceRunning;
}

