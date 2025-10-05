import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'permission_manager.dart';

class BatteryOptimizationManager {
  static const MethodChannel _channel = MethodChannel('call_blocker_service');

  static Future<bool> requestBatteryOptimizationExemption() async {
    try {
      await _channel.invokeMethod('requestBatteryOptimization');
      return true;
    } catch (e) {
      print('Error requesting battery optimization exemption: $e');
      return false;
    }
  }

  static Future<bool> isBatteryOptimizationExempted() async {
    try {
      final result = await _channel.invokeMethod('isBatteryOptimizationExempted');
      return result as bool? ?? false;
    } catch (e) {
      print('Error checking battery optimization status: $e');
      return false;
    }
  }

  static Future<void> showBatteryOptimizationDialog(BuildContext context) async {
    final isExempted = await isBatteryOptimizationExempted();
    
    if (!isExempted) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => AlertDialog(
          title: const Text('Battery Optimization'),
          content: const Text(
            'To ensure Call Blocker works properly in the background, please exempt it from battery optimization. This will prevent the system from killing the app when it\'s not actively being used.',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('Skip'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.of(context).pop();
                await requestBatteryOptimizationExemption();
              },
              child: const Text('Open Settings'),
            ),
          ],
        ),
      );
    }
  }
}

