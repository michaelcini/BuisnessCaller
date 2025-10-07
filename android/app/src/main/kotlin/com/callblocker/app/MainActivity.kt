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
import android.view.accessibility.AccessibilityManager
import androidx.annotation.NonNull
import com.callblocker.app.receiver.PhoneStateReceiver
import com.callblocker.app.receiver.SMSReceiver
import com.callblocker.app.service.CallBlockerService
import com.callblocker.app.service.DNDService
import com.callblocker.app.service.CallDeclinerAccessibilityService
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
                "testCallScreeningWithFakeCall" -> {
                    testCallScreeningWithFakeCall()
                    result.success(null)
                }
                "enableDND" -> {
                    val success = enableDND()
                    result.success(success)
                }
                "disableDND" -> {
                    val success = disableDND()
                    result.success(success)
                }
                "isDNDEnabled" -> {
                    val isEnabled = isDNDEnabled()
                    result.success(isEnabled)
                }
                "hasDNDPermission" -> {
                    val hasPermission = hasDNDPermission()
                    result.success(hasPermission)
                }
                "getDNDStatus" -> {
                    val status = getDNDStatus()
                    result.success(status)
                }
                "testDND" -> {
                    testDND()
                    result.success(null)
                }
                "isAccessibilityServiceEnabled" -> {
                    val isEnabled = isAccessibilityServiceEnabled()
                    result.success(isEnabled)
                }
                "openAccessibilitySettings" -> {
                    openAccessibilitySettings()
                    result.success(null)
                }
                "testAccessibilityService" -> {
                    testAccessibilityService()
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
                var defaultApp: String? = null
                
                Log.i("MainActivity", "Android version: ${android.os.Build.VERSION.SDK_INT}")
                Log.i("MainActivity", "Our package: $packageName")
                
                // Try multiple methods to check if we're the default call screening app
                try {
                    // Method 1: Use reflection (works on Android 10+)
                    try {
                        val method = telecomManager.javaClass.getMethod("getDefaultCallScreeningApp")
                        defaultApp = method.invoke(telecomManager) as String?
                        isDefault = defaultApp == packageName
                        Log.i("MainActivity", "Method 1 - Default call screening app: $defaultApp")
                        Log.i("MainActivity", "Method 1 - Is our app default: $isDefault")
                    } catch (reflectionException: Exception) {
                        Log.w("MainActivity", "Method 1 failed: ${reflectionException.message}")
                        
                        // Method 2: Check via Settings (Android 11+ specific)
                        try {
                            val settingsValue = android.provider.Settings.Secure.getString(
                                contentResolver, 
                                "call_screening_app"
                            )
                            defaultApp = settingsValue
                            isDefault = settingsValue == packageName
                            Log.i("MainActivity", "Method 2 - Settings call_screening_app: $settingsValue")
                            Log.i("MainActivity", "Method 2 - Is our app default: $isDefault")
                        } catch (settingsException: Exception) {
                            Log.w("MainActivity", "Method 2 failed: ${settingsException.message}")
                            
                            // Method 3: Check via another settings key
                            try {
                                val settingsValue2 = android.provider.Settings.Secure.getString(
                                    contentResolver, 
                                    "default_call_screening_app"
                                )
                                defaultApp = settingsValue2
                                isDefault = settingsValue2 == packageName
                                Log.i("MainActivity", "Method 3 - Settings default_call_screening_app: $settingsValue2")
                                Log.i("MainActivity", "Method 3 - Is our app default: $isDefault")
                            } catch (settingsException2: Exception) {
                                Log.w("MainActivity", "Method 3 failed: ${settingsException2.message}")
                                
                                // Method 4: For Android 11+, check if we can access the property directly
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                        // Android 11+ might have the property available
                                        val field = telecomManager.javaClass.getDeclaredField("mDefaultCallScreeningApp")
                                        field.isAccessible = true
                                        val fieldValue = field.get(telecomManager) as String?
                                        defaultApp = fieldValue
                                        isDefault = fieldValue == packageName
                                        Log.i("MainActivity", "Method 4 - Field mDefaultCallScreeningApp: $fieldValue")
                                        Log.i("MainActivity", "Method 4 - Is our app default: $isDefault")
                                    } else {
                                        Log.i("MainActivity", "Method 4 - Android version too old for field access")
                                        isDefault = false
                                    }
                                } catch (fieldException: Exception) {
                                    Log.w("MainActivity", "Method 4 failed: ${fieldException.message}")
                                    
                                    // Method 5: Check if the service is actually being called
                                    // If we can't determine the default app, assume we're not it
                                    // but log that the service is registered
                                    Log.w("MainActivity", "Could not determine default call screening app")
                                    Log.w("MainActivity", "Service is registered, but default status unknown")
                                    isDefault = false
                                }
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
                    Log.i("MainActivity", "Android version: ${android.os.Build.VERSION.SDK_INT}")
                    Log.i("MainActivity", "Our package: $packageName")
                    
                    // Method 1: Use reflection (works on Android 10+)
                    try {
                        val method = telecomManager.javaClass.getMethod("getDefaultCallScreeningApp")
                        defaultApp = method.invoke(telecomManager) as String?
                        isDefault = defaultApp == packageName
                        Log.i("MainActivity", "Method 1 - Default call screening app: $defaultApp")
                        Log.i("MainActivity", "Method 1 - Is our app default: $isDefault")
                    } catch (reflectionException: Exception) {
                        Log.w("MainActivity", "Method 1 failed: ${reflectionException.message}")
                        
                        // Method 2: Check via Settings (Android 11+ specific)
                        try {
                            val settingsValue = android.provider.Settings.Secure.getString(
                                contentResolver, 
                                "call_screening_app"
                            )
                            defaultApp = settingsValue
                            isDefault = settingsValue == packageName
                            Log.i("MainActivity", "Method 2 - Settings call_screening_app: $settingsValue")
                            Log.i("MainActivity", "Method 2 - Is our app default: $isDefault")
                        } catch (settingsException: Exception) {
                            Log.w("MainActivity", "Method 2 failed: ${settingsException.message}")
                            
                            // Method 3: Check via another settings key
                            try {
                                val settingsValue2 = android.provider.Settings.Secure.getString(
                                    contentResolver, 
                                    "default_call_screening_app"
                                )
                                defaultApp = settingsValue2
                                isDefault = settingsValue2 == packageName
                                Log.i("MainActivity", "Method 3 - Settings default_call_screening_app: $settingsValue2")
                                Log.i("MainActivity", "Method 3 - Is our app default: $isDefault")
                            } catch (settingsException2: Exception) {
                                Log.w("MainActivity", "Method 3 failed: ${settingsException2.message}")
                                
                                // Method 4: For Android 11+, check if we can access the property directly
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                        // Android 11+ might have the property available
                                        val field = telecomManager.javaClass.getDeclaredField("mDefaultCallScreeningApp")
                                        field.isAccessible = true
                                        val fieldValue = field.get(telecomManager) as String?
                                        defaultApp = fieldValue
                                        isDefault = fieldValue == packageName
                                        Log.i("MainActivity", "Method 4 - Field mDefaultCallScreeningApp: $fieldValue")
                                        Log.i("MainActivity", "Method 4 - Is our app default: $isDefault")
                                    } else {
                                        Log.i("MainActivity", "Method 4 - Android version too old for field access")
                                        isDefault = false
                                    }
                                } catch (fieldException: Exception) {
                                    Log.w("MainActivity", "Method 4 failed: ${fieldException.message}")
                                    
                                    // Method 5: Check if the service is actually being called
                                    // If we can't determine the default app, assume we're not it
                                    // but log that the service is registered
                                    Log.w("MainActivity", "Could not determine default call screening app")
                                    Log.w("MainActivity", "Service is registered, but default status unknown")
                                    isDefault = false
                                }
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
    
    private fun testCallScreeningWithFakeCall() {
        Log.i("MainActivity", "=== TESTING CALL SCREENING WITH FAKE CALL ===")
        
        // This method tests if the CallScreeningService is actually being triggered
        // by simulating a call screening event
        
        try {
            // Check if service is registered first
            val packageManager = packageManager
            val serviceIntent = Intent("android.telecom.CallScreeningService")
            serviceIntent.setPackage(packageName)
            val resolveInfo = packageManager.resolveService(serviceIntent, 0)
            
            if (resolveInfo == null) {
                Log.e("MainActivity", "CallScreeningService is not registered!")
                return
            }
            
            Log.i("MainActivity", "CallScreeningService is registered")
            
            // Try to start the service to see if it gets created
            val serviceStartIntent = Intent(this, com.callblocker.app.service.CallScreeningService::class.java)
            startService(serviceStartIntent)
            
            Log.i("MainActivity", "CallScreeningService started - check logs for 'CALL SCREENING SERVICE CREATED'")
            Log.i("MainActivity", "If you see that message, the service is working")
            Log.i("MainActivity", "If calls are still not being screened, the issue is that the app is not set as default")
            
            // For Android 11, we need to be more specific about the detection
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.R) {
                Log.i("MainActivity", "Android 11 detected - call screening detection can be tricky")
                Log.i("MainActivity", "Even if detection shows 'not default', the service might still work")
                Log.i("MainActivity", "Test by making an actual call to see if it gets screened")
            }
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error testing call screening with fake call: ${e.message}")
        }
        
        Log.i("MainActivity", "=== FAKE CALL TEST COMPLETED ===")
    }
    
    private fun enableDND(): Boolean {
        Log.i("MainActivity", "Enabling DND...")
        val dndService = DNDService(this)
        return dndService.enableDND()
    }
    
    private fun disableDND(): Boolean {
        Log.i("MainActivity", "Disabling DND...")
        val dndService = DNDService(this)
        return dndService.disableDND()
    }
    
    private fun isDNDEnabled(): Boolean {
        val dndService = DNDService(this)
        return dndService.isDNDEnabled()
    }
    
    private fun hasDNDPermission(): Boolean {
        val dndService = DNDService(this)
        return dndService.hasDNDPermission()
    }
    
    private fun getDNDStatus(): String {
        val dndService = DNDService(this)
        return dndService.getDNDStatus()
    }
    
    private fun testDND() {
        Log.i("MainActivity", "=== TESTING DND FUNCTIONALITY ===")
        
        val dndService = DNDService(this)
        
        Log.i("MainActivity", "DND supported: ${dndService.isDNDSupported()}")
        Log.i("MainActivity", "DND permission: ${dndService.hasDNDPermission()}")
        Log.i("MainActivity", "DND enabled: ${dndService.isDNDEnabled()}")
        Log.i("MainActivity", "DND status: ${dndService.getDNDStatus()}")
        Log.i("MainActivity", "Should enable DND: ${dndService.shouldEnableDND()}")
        
        // Test enabling DND
        if (dndService.hasDNDPermission()) {
            Log.i("MainActivity", "Testing DND enable...")
            val enableResult = dndService.enableDND()
            Log.i("MainActivity", "DND enable result: $enableResult")
            
            // Wait a moment then disable
            Thread.sleep(2000)
            
            Log.i("MainActivity", "Testing DND disable...")
            val disableResult = dndService.disableDND()
            Log.i("MainActivity", "DND disable result: $disableResult")
        } else {
            Log.w("MainActivity", "DND permission not granted - cannot test DND functionality")
            Log.w("MainActivity", "User needs to grant notification policy access")
        }
        
        Log.i("MainActivity", "=== DND TEST COMPLETED ===")
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        Log.i("MainActivity", "Checking accessibility service status...")
        
        try {
            val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            
            // Use reflection to access the method for better compatibility
            val method = accessibilityManager.javaClass.getMethod("getEnabledAccessibilityServiceList", Int::class.java)
            val enabledServices = method.invoke(accessibilityManager, 0xFFFFFFFF) as List<*>
            
            val serviceName = "${packageName}/${CallDeclinerAccessibilityService::class.java.name}"
            Log.i("MainActivity", "Looking for service: $serviceName")
            
            for (service in enabledServices) {
                try {
                    val serviceInfo = service?.javaClass?.getMethod("getResolveInfo")?.invoke(service)
                    val serviceInfoObj = serviceInfo?.javaClass?.getMethod("getServiceInfo")?.invoke(serviceInfo)
                    val packageName = serviceInfoObj?.javaClass?.getMethod("getPackageName")?.invoke(serviceInfoObj) as? String
                    val className = serviceInfoObj?.javaClass?.getMethod("getName")?.invoke(serviceInfoObj) as? String
                    
                    if (packageName != null && className != null) {
                        val serviceId = "$packageName/$className"
                        Log.d("MainActivity", "Found enabled service: $serviceId")
                        if (serviceId == serviceName) {
                            Log.i("MainActivity", "Accessibility service is ENABLED")
                            return true
                        }
                    }
                } catch (e: Exception) {
                    Log.d("MainActivity", "Error processing service: ${e.message}")
                }
            }
            
            Log.w("MainActivity", "Accessibility service is NOT ENABLED")
            return false
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking accessibility service: ${e.message}")
            // Fallback: assume not enabled if we can't check
            return false
        }
    }
    
    private fun openAccessibilitySettings() {
        Log.i("MainActivity", "Opening accessibility settings...")
        try {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Log.i("MainActivity", "Accessibility settings opened")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening accessibility settings: ${e.message}")
            // Fallback to general settings
            try {
                val fallbackIntent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
                Log.i("MainActivity", "General settings opened as fallback")
            } catch (fallbackException: Exception) {
                Log.e("MainActivity", "Error opening fallback settings: ${fallbackException.message}")
            }
        }
    }
    
    private fun testAccessibilityService() {
        Log.i("MainActivity", "=== TESTING ACCESSIBILITY SERVICE ===")
        
        val isEnabled = isAccessibilityServiceEnabled()
        Log.i("MainActivity", "Accessibility service enabled: $isEnabled")
        
        if (isEnabled) {
            Log.i("MainActivity", "✅ Accessibility service is ENABLED")
            Log.i("MainActivity", "Service will automatically decline calls during non-business hours")
            Log.i("MainActivity", "Make a test call to verify functionality")
        } else {
            Log.w("MainActivity", "❌ Accessibility service is NOT ENABLED")
            Log.w("MainActivity", "User must enable it in Accessibility Settings")
            Log.w("MainActivity", "Look for 'Call Blocker' in the accessibility services list")
        }
        
        // Check if the service is properly configured
        try {
            val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            
            // Use reflection to access installed services
            val method = accessibilityManager.javaClass.getMethod("getInstalledAccessibilityServiceList")
            val installedServices = method.invoke(accessibilityManager) as List<*>
            
            val serviceName = "${packageName}/${CallDeclinerAccessibilityService::class.java.name}"
            var isInstalled = false
            
            for (service in installedServices) {
                try {
                    val serviceInfo = service?.javaClass?.getMethod("getResolveInfo")?.invoke(service)
                    val serviceInfoObj = serviceInfo?.javaClass?.getMethod("getServiceInfo")?.invoke(serviceInfo)
                    val packageName = serviceInfoObj?.javaClass?.getMethod("getPackageName")?.invoke(serviceInfoObj) as? String
                    val className = serviceInfoObj?.javaClass?.getMethod("getName")?.invoke(serviceInfoObj) as? String
                    
                    if (packageName != null && className != null) {
                        val serviceId = "$packageName/$className"
                        if (serviceId == serviceName) {
                            isInstalled = true
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.d("MainActivity", "Error processing installed service: ${e.message}")
                }
            }
            
            Log.i("MainActivity", "Service installed: $isInstalled")
            
            if (!isInstalled) {
                Log.e("MainActivity", "Service is not properly installed!")
            }
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking service installation: ${e.message}")
        }
        
        Log.i("MainActivity", "=== ACCESSIBILITY SERVICE TEST COMPLETED ===")
    }
}