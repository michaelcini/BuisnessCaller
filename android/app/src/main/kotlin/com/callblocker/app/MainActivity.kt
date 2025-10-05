package com.callblocker.app

import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.NonNull
import com.callblocker.app.receiver.PhoneStateReceiver
import com.callblocker.app.receiver.SMSReceiver
import com.callblocker.app.service.CallBlockerService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "call_blocker_service"
    private val PHONE_CHANNEL = "phone_state_receiver"
    private val SMS_CHANNEL = "sms_receiver"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startService" -> {
                    startCallBlockerService()
                    result.success(null)
                }
                "stopService" -> {
                    stopCallBlockerService()
                    result.success(null)
                }
                "sendSMS" -> {
                    val phoneNumber = call.argument<String>("phoneNumber")
                    val message = call.argument<String>("message")
                    sendSMS(phoneNumber, message)
                    result.success(null)
                }
                "blockCall" -> {
                    val phoneNumber = call.argument<String>("phoneNumber")
                    blockCall(phoneNumber)
                    result.success(null)
                }
                "requestBatteryOptimization" -> {
                    requestBatteryOptimization()
                    result.success(null)
                }
                "isBatteryOptimizationExempted" -> {
                    val isExempted = isBatteryOptimizationExempted()
                    result.success(isExempted)
                }
                "requestCallScreeningPermission" -> {
                    requestCallScreeningPermission()
                    result.success(null)
                }
                "isCallScreeningEnabled" -> {
                    val isEnabled = isCallScreeningEnabled()
                    result.success(isEnabled)
                }
                "testCallScreening" -> {
                    testCallScreening()
                    result.success(null)
                }
                "testSMS" -> {
                    val phoneNumber = call.argument<String>("phoneNumber")
                    val message = call.argument<String>("message")
                    testSMS(phoneNumber, message)
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        // Set up method channels for receivers
        PhoneStateReceiver.methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PHONE_CHANNEL)
        SMSReceiver.methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SMS_CHANNEL)
    }

    private fun startCallBlockerService() {
        Log.d("MainActivity", "Starting CallBlockerService...")
        val serviceIntent = Intent(this, CallBlockerService::class.java).apply {
            action = CallBlockerService.ACTION_START_SERVICE
        }
        try {
            startForegroundService(serviceIntent)
            Log.d("MainActivity", "CallBlockerService started successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start CallBlockerService: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun stopCallBlockerService() {
        Log.d("MainActivity", "Stopping CallBlockerService...")
        val serviceIntent = Intent(this, CallBlockerService::class.java).apply {
            action = CallBlockerService.ACTION_STOP_SERVICE
        }
        try {
            startService(serviceIntent)
            Log.d("MainActivity", "CallBlockerService stop request sent")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to stop CallBlockerService: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun sendSMS(phoneNumber: String?, message: String?) {
        Log.d("MainActivity", "sendSMS called - Phone: $phoneNumber, Message: $message")
        if (phoneNumber != null && message != null) {
            try {
                Log.d("MainActivity", "Attempting to send SMS to $phoneNumber")
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.i("MainActivity", "SMS sent successfully to $phoneNumber: $message")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error sending SMS to $phoneNumber: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.w("MainActivity", "sendSMS called with null parameters - Phone: $phoneNumber, Message: $message")
        }
    }

    private fun blockCall(phoneNumber: String?) {
        Log.d("MainActivity", "blockCall called - Phone: $phoneNumber")
        if (phoneNumber != null) {
            try {
                // Note: Direct call blocking is not possible on Android 10+ due to security restrictions
                // This is a placeholder for logging and notification purposes
                Log.i("MainActivity", "Call blocking request for: $phoneNumber")
                Log.d("MainActivity", "Note: Actual call blocking requires CallScreeningService to be set as default")
                
                // In a real implementation, you would need to:
                // 1. Use AccessibilityService for call blocking (requires user setup)
                // 2. Or use Call Screening API (Android 10+)
                // 3. Or redirect calls to voicemail
                
                // For now, we'll just log the blocked call
                // The actual blocking would need to be implemented through other means
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in blockCall for $phoneNumber: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.w("MainActivity", "blockCall called with null phone number")
        }
    }

    private fun requestBatteryOptimization() {
        Log.d("MainActivity", "Requesting battery optimization exemption for package: $packageName")
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
            startActivity(intent)
            Log.d("MainActivity", "Battery optimization settings opened")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening battery optimization settings: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun isBatteryOptimizationExempted(): Boolean {
        return try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val isExempted = powerManager.isIgnoringBatteryOptimizations(packageName)
            Log.d("MainActivity", "Battery optimization status for $packageName: $isExempted")
            isExempted
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking battery optimization status: ${e.message}")
            false
        }
    }

    private fun requestCallScreeningPermission() {
        Log.d("MainActivity", "Requesting call screening permission - opening default apps settings")
        try {
            // Open the default apps settings where user can set call screening app
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
            Log.d("MainActivity", "Default apps settings opened successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening default apps settings: ${e.message}")
            // Fallback to general settings
            try {
                Log.d("MainActivity", "Falling back to general settings")
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
                Log.d("MainActivity", "General settings opened as fallback")
            } catch (fallbackException: Exception) {
                Log.e("MainActivity", "Error opening fallback settings: ${fallbackException.message}")
            }
        }
    }

    private fun isCallScreeningEnabled(): Boolean {
        return try {
            // For Android 10+ (API 29+), we need to check if our CallScreeningService is registered
            val packageManager = packageManager
            val serviceIntent = Intent("android.telecom.CallScreeningService")
            serviceIntent.setPackage(packageName)
            
            val resolveInfo = packageManager.resolveService(serviceIntent, 0)
            val isRegistered = resolveInfo != null
            
            Log.i("MainActivity", "=== CALL SCREENING STATUS CHECK ===")
            Log.i("MainActivity", "CallScreeningService registered: $isRegistered")
            Log.i("MainActivity", "Our package name: $packageName")
            
            if (isRegistered) {
                Log.i("MainActivity", "Call screening service is REGISTERED")
                Log.i("MainActivity", "User needs to set this app as default call screening app in Settings")
            } else {
                Log.w("MainActivity", "Call screening service is NOT REGISTERED")
            }
            
            isRegistered
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking call screening status: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun testCallScreening() {
        Log.i("MainActivity", "=== TESTING CALL SCREENING ===")
        Log.i("MainActivity", "Package name: $packageName")
        
        // Check if service is registered
        val packageManager = packageManager
        val serviceIntent = Intent("android.telecom.CallScreeningService")
        serviceIntent.setPackage(packageName)
        val resolveInfo = packageManager.resolveService(serviceIntent, 0)
        
        Log.i("MainActivity", "CallScreeningService registered: ${resolveInfo != null}")
        if (resolveInfo != null) {
            Log.i("MainActivity", "Service info: ${resolveInfo.serviceInfo}")
        }
        
        // Check if we're registered as a call screening service
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        Log.i("MainActivity", "TelecomManager available: ${telecomManager != null}")
        Log.i("MainActivity", "Note: User must manually set this app as default call screening app")
        
        // Check permissions
        val phonePermission = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
        val smsPermission = checkSelfPermission(android.Manifest.permission.SEND_SMS)
        val receiveSmsPermission = checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)
        
        Log.i("MainActivity", "READ_PHONE_STATE permission: $phonePermission")
        Log.i("MainActivity", "SEND_SMS permission: $smsPermission")
        Log.i("MainActivity", "RECEIVE_SMS permission: $receiveSmsPermission")
    }

    private fun testSMS(phoneNumber: String?, message: String?) {
        Log.i("MainActivity", "=== TESTING SMS ===")
        Log.i("MainActivity", "Phone number: $phoneNumber")
        Log.i("MainActivity", "Message: $message")
        
        if (phoneNumber != null && message != null) {
            sendSMS(phoneNumber, message)
        } else {
            Log.w("MainActivity", "Invalid SMS test parameters")
        }
    }
}