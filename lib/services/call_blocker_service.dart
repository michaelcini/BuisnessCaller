import 'package:flutter/services.dart';
import '../domain/app_settings_use_case.dart';
import '../services/log_service.dart';
import '../services/super_log_service.dart';

class CallBlockerService {
  static const MethodChannel _channel = MethodChannel('call_blocker_service');
  static const MethodChannel _phoneChannel = MethodChannel('phone_state_receiver');
  static const MethodChannel _smsChannel = MethodChannel('sms_receiver');
  
  final AppSettingsUseCase _settingsUseCase;
  final LogService _logService = LogService();
  SuperLogService? _superLogService;
  
  bool _isServiceRunning = false;
  
  CallBlockerService(this._settingsUseCase);

  void setSuperLogService(SuperLogService superLogService) {
    _superLogService = superLogService;
  }

  Future<void> initialize() async {
    _phoneChannel.setMethodCallHandler(_handlePhoneStateCall);
    _smsChannel.setMethodCallHandler(_handleSMSCall);
  }

  Future<void> startService() async {
    print('🔄 CallBlockerService: Starting service...');
    _superLogService?.addInfo('CallBlockerService', '=== STARTING CALL BLOCKER SERVICE ===');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    if (_isServiceRunning) {
      print('⚠️ CallBlockerService: Service already running');
      _superLogService?.addWarning('CallBlockerService', 'Service already running - skipping start');
      await _logService.addWarning('Service already running');
      return;
    }
    
    try {
      print('📡 CallBlockerService: Invoking native startService method');
      _superLogService?.addDebug('CallBlockerService', 'Invoking native startService method');
      _superLogService?.addDebug('CallBlockerService', 'Method channel: call_blocker_service');
      
      await _channel.invokeMethod('startService');
      _isServiceRunning = true;
      
      print('✅ CallBlockerService: Service started successfully');
      _superLogService?.addInfo('CallBlockerService', '✅ Service started successfully');
      _superLogService?.addInfo('CallBlockerService', 'Service status: Running');
      _superLogService?.addInfo('CallBlockerService', 'Call screening should now be active');
      await _logService.addSuccess('Call Blocker service started');
      
      // Check call screening status after starting
      _superLogService?.addInfo('CallBlockerService', 'Checking call screening status...');
      final isCallScreeningEnabled = await isCallScreeningEnabled();
      _superLogService?.addInfo('CallBlockerService', 'Call screening enabled: $isCallScreeningEnabled');
      
    } catch (e) {
      print('❌ CallBlockerService: Failed to start service: $e');
      _superLogService?.addError('CallBlockerService', '❌ Failed to start service', details: e.toString());
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      await _logService.addError('Failed to start service', details: e.toString());
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== SERVICE STARTUP COMPLETE ===');
  }

  Future<void> stopService() async {
    print('🔄 CallBlockerService: Stopping service...');
    if (!_isServiceRunning) {
      print('⚠️ CallBlockerService: Service not running');
      await _logService.addWarning('Service not running');
      return;
    }
    
    try {
      print('📡 CallBlockerService: Invoking native stopService method');
      await _channel.invokeMethod('stopService');
      _isServiceRunning = false;
      print('✅ CallBlockerService: Service stopped successfully');
      await _logService.addSuccess('Call Blocker service stopped');
    } catch (e) {
      print('❌ CallBlockerService: Failed to stop service: $e');
      await _logService.addError('Failed to stop service', details: e.toString());
    }
  }

  Future<void> requestCallScreeningPermission() async {
    print('🔄 CallBlockerService: Requesting call screening permission...');
    try {
      print('📡 CallBlockerService: Invoking native requestCallScreeningPermission method');
      await _channel.invokeMethod('requestCallScreeningPermission');
      print('✅ CallBlockerService: Call screening permission request sent');
      await _logService.addInfo('Requested call screening permission');
    } catch (e) {
      print('❌ CallBlockerService: Failed to request call screening permission: $e');
      await _logService.addError('Failed to request call screening permission', details: e.toString());
    }
  }

  Future<bool> isCallScreeningEnabled() async {
    print('🔄 CallBlockerService: Checking call screening status...');
    _superLogService?.addInfo('CallBlockerService', '=== CHECKING CALL SCREENING STATUS ===');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    try {
      print('📡 CallBlockerService: Invoking native isCallScreeningEnabled method');
      _superLogService?.addDebug('CallBlockerService', 'Invoking native isCallScreeningEnabled method');
      _superLogService?.addDebug('CallBlockerService', 'Method channel: call_blocker_service');
      
      final isEnabled = await _channel.invokeMethod('isCallScreeningEnabled') ?? false;
      
      print('📊 CallBlockerService: Call screening enabled: $isEnabled');
      _superLogService?.addInfo('CallBlockerService', '📊 Call screening enabled: $isEnabled');
      
      if (isEnabled) {
        _superLogService?.addInfo('CallBlockerService', '✅ Call screening is ENABLED');
        _superLogService?.addInfo('CallBlockerService', 'App is set as default call screening app');
        _superLogService?.addInfo('CallBlockerService', 'Calls should be screened and blocked when appropriate');
      } else {
        _superLogService?.addWarning('CallBlockerService', '⚠️ Call screening is DISABLED');
        _superLogService?.addWarning('CallBlockerService', 'App is NOT set as default call screening app');
        _superLogService?.addWarning('CallBlockerService', 'Calls will NOT be screened or blocked');
        _superLogService?.addWarning('CallBlockerService', 'User must set this app as default in Android settings');
      }
      
      await _logService.addInfo('Call screening status checked', details: 'Enabled: $isEnabled');
      _superLogService?.addInfo('CallBlockerService', '=== CALL SCREENING STATUS CHECK COMPLETE ===');
      return isEnabled;
    } catch (e) {
      print('❌ CallBlockerService: Failed to check call screening status: $e');
      _superLogService?.addError('CallBlockerService', '❌ Failed to check call screening status');
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      await _logService.addError('Failed to check call screening status', details: e.toString());
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
    print('📞 CallBlockerService: Handling incoming call...');
    _superLogService?.addInfo('CallBlockerService', '=== INCOMING CALL DETECTED ===');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    final phoneNumber = arguments['phoneNumber'] as String?;
    final timestamp = arguments['timestamp'] as int?;
    
    print('📞 CallBlockerService: Phone number: $phoneNumber');
    print('📞 CallBlockerService: Timestamp: $timestamp');
    _superLogService?.addInfo('CallBlockerService', 'Phone number: $phoneNumber');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: $timestamp');
    
    if (phoneNumber == null) {
      print('⚠️ CallBlockerService: Incoming call with no phone number');
      _superLogService?.addWarning('CallBlockerService', '⚠️ Incoming call with no phone number');
      _superLogService?.addWarning('CallBlockerService', 'Cannot process call without phone number');
      await _logService.addWarning('Incoming call with no phone number');
      return;
    }
    
    print('📞 CallBlockerService: Processing call from: $phoneNumber');
    _superLogService?.addInfo('CallBlockerService', '📞 Processing call from: $phoneNumber');
    await _logService.addInfo('Incoming call detected', phoneNumber: phoneNumber);
    
    print('📊 CallBlockerService: Loading settings...');
    _superLogService?.addDebug('CallBlockerService', 'Loading app settings...');
    final settings = await _settingsUseCase.getSettings();
    final weeklySchedule = await _settingsUseCase.getWeeklySchedule();
    
    print('📊 CallBlockerService: App enabled: ${settings.isEnabled}');
    print('📊 CallBlockerService: Call blocking enabled: ${settings.blockCalls}');
    _superLogService?.addInfo('CallBlockerService', '📱 App enabled: ${settings.isEnabled}');
    _superLogService?.addInfo('CallBlockerService', '🚫 Call blocking enabled: ${settings.blockCalls}');
    
    if (!settings.isEnabled) {
      print('❌ CallBlockerService: App is disabled, allowing call');
      _superLogService?.addInfo('CallBlockerService', '✅ ALLOWING CALL - App is disabled');
      _superLogService?.addInfo('CallBlockerService', 'Reason: App is disabled in settings');
      await _logService.addInfo('App is disabled, allowing call', phoneNumber: phoneNumber);
      return;
    }
    
    if (!settings.blockCalls) {
      print('❌ CallBlockerService: Call blocking is disabled, allowing call');
      _superLogService?.addInfo('CallBlockerService', '✅ ALLOWING CALL - Call blocking is disabled');
      _superLogService?.addInfo('CallBlockerService', 'Reason: Call blocking is disabled in settings');
      await _logService.addInfo('Call blocking is disabled, allowing call', phoneNumber: phoneNumber);
      return;
    }
    
    print('🕐 CallBlockerService: Checking business hours...');
    _superLogService?.addInfo('CallBlockerService', '🕐 Checking business hours...');
    final shouldBlock = weeklySchedule.shouldBlockCall();
    print('🕐 CallBlockerService: Should block call: $shouldBlock');
    _superLogService?.addInfo('CallBlockerService', '🕐 Should block call: $shouldBlock');
    
    if (shouldBlock) {
      print('🚫 CallBlockerService: Outside business hours, attempting to block call');
      _superLogService?.addWarning('CallBlockerService', '🚫 BLOCKING CALL - Outside business hours');
      _superLogService?.addWarning('CallBlockerService', 'Phone number: $phoneNumber');
      _superLogService?.addWarning('CallBlockerService', 'Reason: Call is outside business hours');
      await _logService.addInfo('Outside business hours, attempting to block call', phoneNumber: phoneNumber);
      await _blockCall(phoneNumber);
    } else {
      print('✅ CallBlockerService: Within business hours, allowing call');
      _superLogService?.addInfo('CallBlockerService', '✅ ALLOWING CALL - Within business hours');
      _superLogService?.addInfo('CallBlockerService', 'Phone number: $phoneNumber');
      _superLogService?.addInfo('CallBlockerService', 'Reason: Call is within business hours');
      await _logService.addInfo('Within business hours, allowing call', phoneNumber: phoneNumber);
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== INCOMING CALL PROCESSING COMPLETE ===');
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
    _superLogService?.addInfo('CallBlockerService', '=== ATTEMPTING TO BLOCK CALL ===');
    _superLogService?.addInfo('CallBlockerService', 'Phone number: $phoneNumber');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    try {
      await _logService.addInfo('Attempting to block call', phoneNumber: phoneNumber);
      _superLogService?.addInfo('CallBlockerService', 'Calling native blockCall method...');
      
      // Call the native Android method
      await _channel.invokeMethod('blockCall', {'phoneNumber': phoneNumber});
      
      await _logService.addSuccess('Call blocking request sent', phoneNumber: phoneNumber);
      _superLogService?.addInfo('CallBlockerService', '✅ Call blocking request sent successfully');
      _superLogService?.addInfo('CallBlockerService', 'Note: Actual blocking requires CallScreeningService');
      
      // Note: Actual call blocking on Android 10+ requires:
      // 1. Call Screening Service (implemented above)
      // 2. User to set the app as default call screening app
      // 3. Proper permissions and Play Store compliance
      _superLogService?.addInfo('CallBlockerService', 'Call screening service should handle actual blocking');
      
    } catch (e) {
      await _logService.addError('Failed to block call', details: e.toString(), phoneNumber: phoneNumber);
      _superLogService?.addError('CallBlockerService', '❌ Failed to block call');
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      print('Error blocking call: $e');
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== CALL BLOCKING ATTEMPT COMPLETE ===');
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

  Future<void> testCallScreening() async {
    print('🧪 CallBlockerService: Testing call screening setup...');
    _superLogService?.addInfo('CallBlockerService', '=== STARTING CALL SCREENING TEST ===');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    try {
      _superLogService?.addInfo('CallBlockerService', 'Invoking native testCallScreening method...');
      await _channel.invokeMethod('testCallScreening');
      print('✅ CallBlockerService: Call screening test completed');
      _superLogService?.addInfo('CallBlockerService', '✅ Call screening test completed successfully');
      _superLogService?.addInfo('CallBlockerService', 'Check Android logs for detailed test results');
    } catch (e) {
      print('❌ CallBlockerService: Call screening test failed: $e');
      _superLogService?.addError('CallBlockerService', '❌ Call screening test failed');
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      rethrow;
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== CALL SCREENING TEST COMPLETE ===');
  }

  Future<void> testSMS(String phoneNumber, String message) async {
    print('🧪 CallBlockerService: Testing SMS functionality...');
    _superLogService?.addInfo('CallBlockerService', 'Starting SMS test...');
    try {
      await _channel.invokeMethod('testSMS', {
        'phoneNumber': phoneNumber,
        'message': message,
      });
      print('✅ CallBlockerService: SMS test completed');
      _superLogService?.addInfo('CallBlockerService', 'SMS test completed successfully');
    } catch (e) {
      print('❌ CallBlockerService: SMS test failed: $e');
      _superLogService?.addError('CallBlockerService', 'SMS test failed', details: e.toString());
      rethrow;
    }
  }

  Future<void> openCallScreeningSettings() async {
    print('⚙️ CallBlockerService: Opening call screening settings...');
    _superLogService?.addInfo('CallBlockerService', '=== OPENING CALL SCREENING SETTINGS ===');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    try {
      _superLogService?.addInfo('CallBlockerService', 'Invoking native openCallScreeningSettings method...');
      await _channel.invokeMethod('openCallScreeningSettings');
      print('✅ CallBlockerService: Call screening settings opened');
      _superLogService?.addInfo('CallBlockerService', '✅ Call screening settings opened successfully');
      _superLogService?.addInfo('CallBlockerService', 'User should set this app as default call screening app');
    } catch (e) {
      print('❌ CallBlockerService: Failed to open call screening settings: $e');
      _superLogService?.addError('CallBlockerService', '❌ Failed to open call screening settings');
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      rethrow;
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== CALL SCREENING SETTINGS OPENED ===');
  }

  Future<void> simulateIncomingCall(String phoneNumber) async {
    print('🧪 CallBlockerService: Simulating incoming call...');
    _superLogService?.addInfo('CallBlockerService', '=== SIMULATING INCOMING CALL ===');
    _superLogService?.addInfo('CallBlockerService', 'Phone number: $phoneNumber');
    _superLogService?.addInfo('CallBlockerService', 'Timestamp: ${DateTime.now().toIso8601String()}');
    
    try {
      // Simulate the incoming call by calling the handler directly
      _superLogService?.addInfo('CallBlockerService', 'Simulating call processing...');
      await _handleIncomingCall({
        'phoneNumber': phoneNumber,
        'timestamp': DateTime.now().millisecondsSinceEpoch,
      });
      
      _superLogService?.addInfo('CallBlockerService', '✅ Call simulation completed');
      _superLogService?.addInfo('CallBlockerService', 'Check logs for processing details');
    } catch (e) {
      _superLogService?.addError('CallBlockerService', '❌ Call simulation failed');
      _superLogService?.addError('CallBlockerService', 'Error type: ${e.runtimeType}');
      _superLogService?.addError('CallBlockerService', 'Error message: ${e.toString()}');
      rethrow;
    }
    
    _superLogService?.addInfo('CallBlockerService', '=== CALL SIMULATION COMPLETE ===');
  }
}

