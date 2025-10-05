import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

class PermissionManager {
  static const List<Permission> _requiredPermissions = [
    Permission.phone,
    Permission.sms,
    Permission.notification,
    Permission.systemAlertWindow,
  ];

  static Future<bool> requestAllPermissions() async {
    Map<Permission, PermissionStatus> statuses = await _requiredPermissions.request();
    
    bool allGranted = true;
    for (var status in statuses.values) {
      if (status != PermissionStatus.granted) {
        allGranted = false;
        break;
      }
    }
    
    return allGranted;
  }

  static Future<bool> checkAllPermissions() async {
    for (Permission permission in _requiredPermissions) {
      PermissionStatus status = await permission.status;
      if (status != PermissionStatus.granted) {
        return false;
      }
    }
    return true;
  }

  static Future<List<Permission>> getMissingPermissions() async {
    List<Permission> missing = [];
    
    for (Permission permission in _requiredPermissions) {
      PermissionStatus status = await permission.status;
      if (status != PermissionStatus.granted) {
        missing.add(permission);
      }
    }
    
    return missing;
  }

  static Future<bool> requestBatteryOptimizationExemption() async {
    return await Permission.ignoreBatteryOptimizations.request().isGranted;
  }

  static Future<bool> isBatteryOptimizationExempted() async {
    return await Permission.ignoreBatteryOptimizations.isGranted;
  }

  static Future<void> openSettings() async {
    await openAppSettings();
  }

  static String getPermissionDescription(Permission permission) {
    switch (permission) {
      case Permission.phone:
        return 'Required to detect incoming calls and block them';
      case Permission.sms:
        return 'Required to send automatic SMS replies';
      case Permission.notification:
        return 'Required to show foreground service notification';
      case Permission.systemAlertWindow:
        return 'Required for system-level call blocking';
      default:
        return 'Required for app functionality';
    }
  }

  static IconData getPermissionIcon(Permission permission) {
    switch (permission) {
      case Permission.phone:
        return Icons.phone;
      case Permission.sms:
        return Icons.sms;
      case Permission.notification:
        return Icons.notifications;
      case Permission.systemAlertWindow:
        return Icons.security;
      default:
        return Icons.info;
    }
  }
}