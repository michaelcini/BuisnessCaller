package com.callblocker.app

import android.content.Context
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
                "openCallScreeningSettings" -> {
                    openCallScreeningSettings()
                    result.success(null)
                }
                "testCallScreeningService" -> {
                    testCallScreeningService()
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
            Log.i("MainActivity", "=== CALL SCREENING STATUS CHECK ===")
            Log.i("MainActivity", "Our package name: $packageName")
            
            // Check if service is registered
            val packageManager = packageManager
            val serviceIntent = Intent("android.telecom.CallScreeningService")
            serviceIntent.setPackage(packageName)
            val resolveInfo = packageManager.resolveService(serviceIntent, 0)
            val isRegistered = resolveInfo != null
            
            Log.i("MainActivity", "CallScreeningService registered: $isRegistered")
            
            if (!isRegistered) {
                Log.w("MainActivity", "Call screening service is NOT REGISTERED")
                return false
            }
            
        // Check if we're the default call screening app (Android 10+)
        var isDefault = false
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                // Try multiple methods to check if we're the default call screening app
                try {
                    // Method 1: Try direct property access
                    val defaultApp = try {
                        telecomManager.defaultCallScreeningApp
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (defaultApp != null) {
                        isDefault = defaultApp == packageName
                        Log.i("MainActivity", "Method 1 - Default call screening app: $defaultApp")
                        Log.i("MainActivity", "Method 1 - Is our app default: $isDefault")
                    } else {
                        // Method 2: Use reflection
                        try {
                            val method = telecomManager.javaClass.getMethod("getDefaultCallScreeningApp")
                            val defaultAppReflection = method.invoke(telecomManager) as String?
                            isDefault = defaultAppReflection == packageName
                            Log.i("MainActivity", "Method 2 - Default call screening app: $defaultAppReflection")
                            Log.i("MainActivity", "Method 2 - Is our app default: $isDefault")
                        } catch (reflectionException: Exception) {
                            Log.w("MainActivity", "Method 2 failed: ${reflectionException.message}")
                            
                            // Method 3: Check via Settings
                            try {
                                val settingsValue = android.provider.Settings.Secure.getString(
                                    contentResolver, 
                                    "call_screening_app"
                                )
                                isDefault = settingsValue == packageName
                                Log.i("MainActivity", "Method 3 - Settings call_screening_app: $settingsValue")
                                Log.i("MainActivity", "Method 3 - Is our app default: $isDefault")
                            } catch (settingsException: Exception) {
                                Log.w("MainActivity", "Method 3 failed: ${settingsException.message}")
                                // If all methods fail, assume we're not default
                                isDefault = false
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error checking default call screening app: ${e.message}")
                    isDefault = false
                }
            } else {
                Log.i("MainActivity", "Android version < 10, default call screening not available")
                isDefault = true // For older versions, just being registered is enough
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking default call screening app: ${e.message}")
        }
            
            if (isRegistered && isDefault) {
                Log.i("MainActivity", "Call screening is FULLY ENABLED")
            } else if (isRegistered && !isDefault) {
                Log.w("MainActivity", "Service registered but NOT set as default")
                Log.w("MainActivity", "User must set this app as default call screening app")
            }
            
            isRegistered && isDefault
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking call screening status: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun testCallScreening() {
        Log.i("MainActivity", "=== COMPREHENSIVE CALL SCREENING TEST ===")
        Log.i("MainActivity", "Package name: $packageName")
        
        // Check if service is registered
        val packageManager = packageManager
        val serviceIntent = Intent("android.telecom.CallScreeningService")
        serviceIntent.setPackage(packageName)
        val resolveInfo = packageManager.resolveService(serviceIntent, 0)
        
        Log.i("MainActivity", "CallScreeningService registered: ${resolveInfo != null}")
        if (resolveInfo != null) {
            Log.i("MainActivity", "Service info: ${resolveInfo.serviceInfo}")
            Log.i("MainActivity", "Service enabled: ${resolveInfo.serviceInfo.enabled}")
            Log.i("MainActivity", "Service exported: ${resolveInfo.serviceInfo.exported}")
        }
        
        // Check if we're registered as a call screening service
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        Log.i("MainActivity", "TelecomManager available: ${telecomManager != null}")
        
        // Check if we're the default call screening app (Android 10+)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Use reflection to access defaultCallScreeningApp for API 29+
                try {
                    val method = telecomManager.javaClass.getMethod("getDefaultCallScreeningApp")
                    val defaultApp = method.invoke(telecomManager) as String?
                    Log.i("MainActivity", "Default call screening app: $defaultApp")
                    Log.i("MainActivity", "Is our app default: ${defaultApp == packageName}")
                } catch (reflectionException: Exception) {
                    Log.w("MainActivity", "Could not access defaultCallScreeningApp via reflection: ${reflectionException.message}")
                    Log.i("MainActivity", "Cannot determine if app is default call screening app")
                }
            } else {
                Log.i("MainActivity", "Android version < 10, default call screening not available")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking default call screening app: ${e.message}")
        }
        
        // Check all relevant permissions
        val permissions = listOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.MODIFY_PHONE_STATE,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW
        )
        
        Log.i("MainActivity", "=== PERMISSION STATUS ===")
        for (permission in permissions) {
            val status = checkSelfPermission(permission)
            val statusText = when (status) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> "GRANTED"
                android.content.pm.PackageManager.PERMISSION_DENIED -> "DENIED"
                else -> "UNKNOWN ($status)"
            }
            Log.i("MainActivity", "$permission: $statusText")
        }
        
        // Check battery optimization
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isBatteryOptimized = powerManager.isIgnoringBatteryOptimizations(packageName)
        Log.i("MainActivity", "Battery optimization exempted: $isBatteryOptimized")
        
        // Check if app is enabled in settings
        try {
            val prefs = getSharedPreferences("call_blocker_settings", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("isEnabled", false)
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i("MainActivity", "App enabled in settings: $isEnabled")
            Log.i("MainActivity", "Call blocking enabled: $blockCalls")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading app settings: ${e.message}")
        }
        
        Log.i("MainActivity", "=== NEXT STEPS ===")
        Log.i("MainActivity", "1. Ensure all permissions are granted")
        Log.i("MainActivity", "2. Set this app as default call screening app in Settings")
        Log.i("MainActivity", "3. Disable battery optimization for this app")
        Log.i("MainActivity", "4. Enable the app in settings")
    }

    private fun testSMS(phoneNumber: String?, message: String?) {
        Log.i("MainActivity", "=== COMPREHENSIVE SMS TEST ===")
        Log.i("MainActivity", "Phone number: $phoneNumber")
        Log.i("MainActivity", "Message: $message")
        
        // Check SMS permissions
        val sendSmsPermission = checkSelfPermission(android.Manifest.permission.SEND_SMS)
        val receiveSmsPermission = checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)
        
        Log.i("MainActivity", "SEND_SMS permission: $sendSmsPermission")
        Log.i("MainActivity", "RECEIVE_SMS permission: $receiveSmsPermission")
        
        // Check if SMS is enabled in settings
        try {
            val prefs = getSharedPreferences("call_blocker_settings", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("isEnabled", false)
            val sendAutoReply = prefs.getBoolean("sendSMS", true)
            Log.i("MainActivity", "App enabled: $isEnabled")
            Log.i("MainActivity", "SMS auto-reply enabled: $sendAutoReply")
            
            // Check business hours logic
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
            Log.i("MainActivity", "Current time: ${String.format("%02d:%02d", currentHour, currentMinute)}")
            
            // Test the business hours logic
            val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
            val dayName = when (dayOfWeek) {
                java.util.Calendar.MONDAY -> "monday"
                java.util.Calendar.TUESDAY -> "tuesday"
                java.util.Calendar.WEDNESDAY -> "wednesday"
                java.util.Calendar.THURSDAY -> "thursday"
                java.util.Calendar.FRIDAY -> "friday"
                java.util.Calendar.SATURDAY -> "saturday"
                java.util.Calendar.SUNDAY -> "sunday"
                else -> "monday"
            }
            
            val dayEnabled = prefs.getBoolean("${dayName}_enabled", true)
            val startHour = prefs.getInt("${dayName}_startHour", 9)
            val startMinute = prefs.getInt("${dayName}_startMinute", 0)
            val endHour = prefs.getInt("${dayName}_endHour", 17)
            val endMinute = prefs.getInt("${dayName}_endMinute", 0)
            
            Log.i("MainActivity", "Day '$dayName' enabled: $dayEnabled")
            Log.i("MainActivity", "Business hours: ${String.format("%02d:%02d", startHour, startMinute)} to ${String.format("%02d:%02d", endHour, endMinute)}")
            
            val currentTimeMinutes = currentHour * 60 + currentMinute
            val startTimeMinutes = startHour * 60 + startMinute
            val endTimeMinutes = endHour * 60 + endMinute
            
            val isBusinessHours = if (startTimeMinutes <= endTimeMinutes) {
                currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
            } else {
                currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
            }
            
            Log.i("MainActivity", "Is business hours: $isBusinessHours")
            Log.i("MainActivity", "Should send auto-reply (outside business hours): ${!isBusinessHours}")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error reading SMS settings: ${e.message}")
        }
        
        // Test SMS sending
        if (phoneNumber != null && message != null) {
            Log.i("MainActivity", "Attempting to send test SMS...")
            sendSMS(phoneNumber, message)
        } else {
            Log.w("MainActivity", "Invalid SMS test parameters - using defaults")
            // Use default test values
            val testNumber = "+1234567890"
            val testMessage = "Test SMS from Call Blocker App - ${System.currentTimeMillis()}"
            Log.i("MainActivity", "Sending test SMS to: $testNumber")
            sendSMS(testNumber, testMessage)
        }
        
        Log.i("MainActivity", "=== SMS TEST COMPLETED ===")
    }
    
    private fun openCallScreeningSettings() {
        Log.i("MainActivity", "Opening call screening settings...")
        try {
            // Try to open the specific call screening settings
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
            Log.i("MainActivity", "Call screening settings opened")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening call screening settings: ${e.message}")
            // Fallback to general settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
                Log.i("MainActivity", "General settings opened as fallback")
            } catch (fallbackException: Exception) {
                Log.e("MainActivity", "Error opening fallback settings: ${fallbackException.message}")
            }
        }
    }
    
    private fun testCallScreeningService() {
        Log.i("MainActivity", "=== TESTING CALL SCREENING SERVICE ACTIVATION ===")
        
        // Check if service is registered
        val packageManager = packageManager
        val serviceIntent = Intent("android.telecom.CallScreeningService")
        serviceIntent.setPackage(packageName)
        val resolveInfo = packageManager.resolveService(serviceIntent, 0)
        
        Log.i("MainActivity", "Service registered: ${resolveInfo != null}")
        
        if (resolveInfo != null) {
            Log.i("MainActivity", "Service is REGISTERED - this is good!")
            Log.i("MainActivity", "Service enabled: ${resolveInfo.serviceInfo.enabled}")
            Log.i("MainActivity", "Service exported: ${resolveInfo.serviceInfo.exported}")
            
            // Check if we can start the service
            try {
                val serviceStartIntent = Intent(this, com.callblocker.app.service.CallScreeningService::class.java)
                Log.i("MainActivity", "Attempting to start CallScreeningService for testing...")
                startService(serviceStartIntent)
                Log.i("MainActivity", "CallScreeningService started successfully")
                Log.i("MainActivity", "Check logs for 'CALL SCREENING SERVICE CREATED' message")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to start CallScreeningService: ${e.message}")
            }
        } else {
            Log.e("MainActivity", "Service is NOT REGISTERED - this is the problem!")
            Log.e("MainActivity", "The CallScreeningService is not properly registered in AndroidManifest.xml")
        }
        
        // Check if we're the default call screening app
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                var isDefault = false
                var defaultApp: String? = null
                
                // Try multiple methods to check if we're the default call screening app
                try {
                    // Method 1: Try direct property access
                    defaultApp = try {
                        telecomManager.defaultCallScreeningApp
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (defaultApp != null) {
                        isDefault = defaultApp == packageName
                        Log.i("MainActivity", "Method 1 - Default call screening app: $defaultApp")
                        Log.i("MainActivity", "Method 1 - Is our app default: $isDefault")
                    } else {
                        // Method 2: Use reflection
                        try {
                            val method = telecomManager.javaClass.getMethod("getDefaultCallScreeningApp")
                            defaultApp = method.invoke(telecomManager) as String?
                            isDefault = defaultApp == packageName
                            Log.i("MainActivity", "Method 2 - Default call screening app: $defaultApp")
                            Log.i("MainActivity", "Method 2 - Is our app default: $isDefault")
                        } catch (reflectionException: Exception) {
                            Log.w("MainActivity", "Method 2 failed: ${reflectionException.message}")
                            
                            // Method 3: Check via Settings
                            try {
                                val settingsValue = android.provider.Settings.Secure.getString(
                                    contentResolver, 
                                    "call_screening_app"
                                )
                                defaultApp = settingsValue
                                isDefault = settingsValue == packageName
                                Log.i("MainActivity", "Method 3 - Settings call_screening_app: $settingsValue")
                                Log.i("MainActivity", "Method 3 - Is our app default: $isDefault")
                            } catch (settingsException: Exception) {
                                Log.w("MainActivity", "Method 3 failed: ${settingsException.message}")
                                isDefault = false
                            }
                        }
                    }
                    
                    Log.i("MainActivity", "Our package: $packageName")
                    Log.i("MainActivity", "Final result - Is our app default: $isDefault")
                    
                    if (isDefault) {
                        Log.i("MainActivity", "✅ WE ARE THE DEFAULT CALL SCREENING APP!")
                        Log.i("MainActivity", "Calls should be screened by our service")
                    } else {
                        Log.w("MainActivity", "❌ WE ARE NOT THE DEFAULT CALL SCREENING APP")
                        Log.w("MainActivity", "Current default app: $defaultApp")
                        Log.w("MainActivity", "User must set this app as default in Settings")
                        Log.w("MainActivity", "Go to Settings > Apps > Default Apps > Call Screening App")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error checking default call screening app: ${e.message}")
                }
            } else {
                Log.i("MainActivity", "Android version < 10, default call screening not available")
                Log.i("MainActivity", "Service registration is sufficient for older versions")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking call screening status: ${e.message}")
        }
        
        Log.i("MainActivity", "=== CALL SCREENING SERVICE TEST COMPLETED ===")
    }
}