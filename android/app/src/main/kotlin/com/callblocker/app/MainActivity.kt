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
        val serviceIntent = Intent(this, CallBlockerService::class.java).apply {
            action = CallBlockerService.ACTION_START_SERVICE
        }
        startForegroundService(serviceIntent)
    }

    private fun stopCallBlockerService() {
        val serviceIntent = Intent(this, CallBlockerService::class.java).apply {
            action = CallBlockerService.ACTION_STOP_SERVICE
        }
        startService(serviceIntent)
    }

    private fun sendSMS(phoneNumber: String?, message: String?) {
        if (phoneNumber != null && message != null) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("MainActivity", "SMS sent to $phoneNumber: $message")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error sending SMS: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun blockCall(phoneNumber: String?) {
        if (phoneNumber != null) {
            try {
                // Note: Direct call blocking is not possible on Android 10+ due to security restrictions
                // This is a placeholder for logging and notification purposes
                Log.d("MainActivity", "Call blocked from: $phoneNumber")
                
                // In a real implementation, you would need to:
                // 1. Use AccessibilityService for call blocking (requires user setup)
                // 2. Or use Call Screening API (Android 10+)
                // 3. Or redirect calls to voicemail
                
                // For now, we'll just log the blocked call
                // The actual blocking would need to be implemented through other means
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error blocking call: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun requestBatteryOptimization() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun isBatteryOptimizationExempted(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestCallScreeningPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error requesting call screening permission: ${e.message}")
        }
    }

    private fun isCallScreeningEnabled(): Boolean {
        return try {
            val telecomManager = getSystemService(TELECOM_SERVICE) as android.telecom.TelecomManager
            val defaultCallScreeningApp = telecomManager.defaultCallScreeningApp
            defaultCallScreeningApp == packageName
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking call screening status: ${e.message}")
            false
        }
    }
}