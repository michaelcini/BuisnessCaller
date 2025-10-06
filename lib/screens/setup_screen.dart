import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/call_blocker_service.dart';
import '../services/log_service.dart';
import '../services/super_log_service.dart';

class SetupScreen extends StatefulWidget {
  const SetupScreen({Key? key}) : super(key: key);

  @override
  State<SetupScreen> createState() => _SetupScreenState();
}

class _SetupScreenState extends State<SetupScreen> {
  late CallBlockerService _callBlockerService;
  late LogService _logService;
  late SuperLogService _superLogService;
  bool _isCallScreeningEnabled = false;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _callBlockerService = context.read<CallBlockerService>();
    _logService = context.read<LogService>();
    _superLogService = context.read<SuperLogService>();
    _checkCallScreeningStatus();
  }

  Future<void> _checkCallScreeningStatus() async {
    setState(() => _isLoading = true);
    _superLogService.addInfo('SetupScreen', 'Checking call screening status...');
    try {
      final isEnabled = await _callBlockerService.isCallScreeningEnabled();
      setState(() {
        _isCallScreeningEnabled = isEnabled;
        _isLoading = false;
      });
      _superLogService.addInfo('SetupScreen', 'Call screening status: $isEnabled');
    } catch (e) {
      setState(() => _isLoading = false);
      _superLogService.addError('SetupScreen', 'Failed to check call screening status', details: e.toString());
      await _logService.addError('Failed to check call screening status', details: e.toString());
    }
  }

  Future<void> _requestCallScreeningPermission() async {
    setState(() => _isLoading = true);
    _superLogService.addInfo('SetupScreen', 'Requesting call screening permission...');
    try {
      await _callBlockerService.requestCallScreeningPermission();
      _superLogService.addInfo('SetupScreen', 'Call screening permission requested');
      await _logService.addInfo('User requested call screening permission');
      
      // Show instructions dialog
      if (mounted) {
        _showCallScreeningInstructions();
      }
    } catch (e) {
      _superLogService.addError('SetupScreen', 'Failed to request call screening permission', details: e.toString());
      await _logService.addError('Failed to request call screening permission', details: e.toString());
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _showCallScreeningInstructions() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Enable Call Screening'),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('To enable call blocking, you need to:'),
            SizedBox(height: 16),
            Text('1. In the settings that just opened, find "Call Screening App"'),
            SizedBox(height: 8),
            Text('2. Select "Call Blocker" as your default call screening app'),
            SizedBox(height: 8),
            Text('3. Return to this app and tap "Check Status"'),
            SizedBox(height: 16),
            Text('This is required for Android 10+ to allow call blocking.'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              _checkCallScreeningStatus();
            },
            child: const Text('Check Status'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Call Blocker Setup'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          _isCallScreeningEnabled ? Icons.check_circle : Icons.warning,
                          color: _isCallScreeningEnabled ? Colors.green : Colors.orange,
                          size: 32,
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Call Screening Status',
                                style: Theme.of(context).textTheme.titleLarge,
                              ),
                              Text(
                                _isCallScreeningEnabled 
                                    ? 'Enabled - Calls can be blocked'
                                    : 'Disabled - Calls cannot be blocked',
                                style: TextStyle(
                                  color: _isCallScreeningEnabled ? Colors.green : Colors.orange,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    if (_isLoading)
                      const Center(child: CircularProgressIndicator())
                    else
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          onPressed: _isCallScreeningEnabled 
                              ? _checkCallScreeningStatus
                              : _requestCallScreeningPermission,
                          icon: Icon(_isCallScreeningEnabled ? Icons.refresh : Icons.settings),
                          label: Text(_isCallScreeningEnabled ? 'Check Status' : 'Enable Call Screening'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: _isCallScreeningEnabled ? Colors.green : Colors.blue,
                            foregroundColor: Colors.white,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Why is this needed?',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Android 10+ requires apps to be set as the default call screening app to block calls. This is a security feature to prevent malicious apps from blocking important calls.',
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Steps to enable:',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 8),
                    const Text('1. Tap "Enable Call Screening" above'),
                    const Text('2. In Android Settings, find "Call Screening App"'),
                    const Text('3. Select "Call Blocker" as the default app'),
                    const Text('4. Return here and tap "Check Status"'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Alternative: SMS Auto-Reply',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Even if call blocking isn\'t working, SMS auto-reply should still work. Check the logs to see if SMS replies are being sent successfully.',
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Debug Tools',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 16),
                    // Test SuperLog button
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: () {
                          _superLogService.addInfo('SetupScreen', 'SuperLog test button pressed at ${DateTime.now()}');
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Test log added - check Super Log'),
                              backgroundColor: Colors.purple,
                            ),
                          );
                        },
                        icon: const Icon(Icons.bug_report_outlined),
                        label: const Text('Test SuperLog (Add Test Entry)'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.purple,
                          foregroundColor: Colors.white,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () async {
                              _superLogService.addInfo('SetupScreen', 'Test Call Screening button pressed');
                              try {
                                await _callBlockerService.testCallScreening();
                                _superLogService.addInfo('SetupScreen', 'Test Call Screening completed');
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Call Screening test completed - check Super Log for details'),
                                    backgroundColor: Colors.green,
                                  ),
                                );
                              } catch (e) {
                                _superLogService.addError('SetupScreen', 'Test Call Screening failed', details: e.toString());
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(
                                    content: Text('Test failed: $e'),
                                    backgroundColor: Colors.red,
                                  ),
                                );
                              }
                            },
                            icon: const Icon(Icons.bug_report),
                            label: const Text('Test Call Screening'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.orange,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () async {
                              _superLogService.addInfo('SetupScreen', 'Test SMS button pressed');
                              try {
                                await _callBlockerService.testSMS('+1234567890', 'Test SMS from Call Blocker');
                                _superLogService.addInfo('SetupScreen', 'Test SMS completed');
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('SMS test completed - check Super Log for details'),
                                    backgroundColor: Colors.green,
                                  ),
                                );
                              } catch (e) {
                                _superLogService.addError('SetupScreen', 'Test SMS failed', details: e.toString());
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(
                                    content: Text('Test failed: $e'),
                                    backgroundColor: Colors.red,
                                  ),
                                );
                              }
                            },
                            icon: const Icon(Icons.sms),
                            label: const Text('Test SMS'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.green,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                 const SizedBox(height: 8),
                 Row(
                   children: [
                     Expanded(
                       child: ElevatedButton.icon(
                         onPressed: () {
                           Navigator.pushNamed(context, '/superlog');
                         },
                         icon: const Icon(Icons.analytics),
                         label: const Text('Open Super Log'),
                         style: ElevatedButton.styleFrom(
                           backgroundColor: Colors.purple,
                           foregroundColor: Colors.white,
                         ),
                       ),
                     ),
                     const SizedBox(width: 8),
                     Expanded(
                       child: ElevatedButton.icon(
                         onPressed: () async {
                           _superLogService.addInfo('SetupScreen', 'Call Screening Settings button pressed');
                           try {
                             await _callBlockerService.openCallScreeningSettings();
                             _superLogService.addInfo('SetupScreen', 'Call Screening Settings opened');
                             ScaffoldMessenger.of(context).showSnackBar(
                               const SnackBar(
                                 content: Text('Settings opened - set this app as default call screening app'),
                                 backgroundColor: Colors.blue,
                               ),
                             );
                           } catch (e) {
                             _superLogService.addError('SetupScreen', 'Failed to open settings', details: e.toString());
                             ScaffoldMessenger.of(context).showSnackBar(
                               SnackBar(
                                 content: Text('Failed to open settings: $e'),
                                 backgroundColor: Colors.red,
                               ),
                             );
                           }
                         },
                         icon: const Icon(Icons.settings),
                         label: const Text('Call Screening Settings'),
                         style: ElevatedButton.styleFrom(
                           backgroundColor: Colors.blue,
                           foregroundColor: Colors.white,
                         ),
                       ),
                     ),
                   ],
                 ),
                    const SizedBox(height: 8),
                    const Text(
                      'Use these buttons to test functionality and check logs for debugging information.',
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
