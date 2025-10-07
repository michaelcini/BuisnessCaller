package com.callblocker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.MethodChannel
import java.util.Calendar

class PhoneStateReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "PhoneStateReceiver"
        var methodChannel: MethodChannel? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== PHONE STATE RECEIVER TRIGGERED ===")
        Log.d(TAG, "Action: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras}")
        
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val timestamp = System.currentTimeMillis()
            
            Log.i(TAG, "Phone state changed: $state")
            Log.i(TAG, "Phone number: $phoneNumber")
            Log.i(TAG, "Timestamp: $timestamp")
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.w(TAG, "INCOMING CALL from: $phoneNumber")
                    Log.d(TAG, "Notifying Flutter about incoming call")
                    
                    // Check if we should block this call
                    if (shouldBlockCall(context, phoneNumber)) {
                        Log.i(TAG, "Call should be blocked - triggering accessibility service")
                        triggerCallBlocking(context, phoneNumber)
                    }
                    
                    if (methodChannel != null) {
                        try {
                            methodChannel?.invokeMethod("onIncomingCall", mapOf(
                                "phoneNumber" to phoneNumber,
                                "timestamp" to timestamp
                            ))
                            Log.i(TAG, "Successfully notified Flutter about incoming call")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error notifying Flutter about incoming call: ${e.message}")
                            e.printStackTrace()
                        }
                    } else {
                        Log.e(TAG, "MethodChannel is null - cannot notify Flutter")
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.i(TAG, "Call answered")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.i(TAG, "Call ended")
                }
                else -> {
                    Log.d(TAG, "Unknown phone state: $state")
                }
            }
        } else {
            Log.d(TAG, "Ignoring non-phone state intent: ${intent.action}")
        }
        Log.d(TAG, "=== PHONE STATE RECEIVER COMPLETE ===")
    }

    private fun shouldBlockCall(context: Context, phoneNumber: String?): Boolean {
        Log.d(TAG, "=== CHECKING IF CALL SHOULD BE BLOCKED ===")
        
        try {
            val prefs = context.getSharedPreferences("call_blocker_settings", Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, not blocking call")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i(TAG, "Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.i(TAG, "Call blocking is disabled, not blocking call")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            val shouldBlock = !isBusinessHours
            Log.i(TAG, "Is business hours: $isBusinessHours")
            Log.i(TAG, "Should block call (outside business hours): $shouldBlock")
            
            return shouldBlock
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking call block settings: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun isBusinessHours(prefs: SharedPreferences): Boolean {
        Log.d(TAG, "=== CHECKING BUSINESS HOURS ===")
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute
        
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayName = when (dayOfWeek) {
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            Calendar.SUNDAY -> "sunday"
            else -> "monday"
        }
        
        Log.i(TAG, "Current time: ${String.format("%02d:%02d", currentHour, currentMinute)}")
        Log.i(TAG, "Current day: $dayName")
        
        // Check if this day is enabled
        val dayEnabled = prefs.getBoolean("${dayName}_enabled", true)
        Log.i(TAG, "Day '$dayName' enabled: $dayEnabled")
        if (!dayEnabled) {
            Log.i(TAG, "Day is disabled, not business hours")
            return false
        }
        
        // Get start and end times for this day
        val startHour = prefs.getInt("${dayName}_startHour", 9)
        val startMinute = prefs.getInt("${dayName}_startMinute", 0)
        val endHour = prefs.getInt("${dayName}_endHour", 17)
        val endMinute = prefs.getInt("${dayName}_endMinute", 0)
        
        Log.i(TAG, "Business hours for $dayName: ${String.format("%02d:%02d", startHour, startMinute)} to ${String.format("%02d:%02d", endHour, endMinute)}")
        
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute
        
        val isBusinessHours = if (startTimeMinutes <= endTimeMinutes) {
            // Normal schedule (e.g., 9:00 to 17:00)
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
        } else {
            // Overnight schedule (e.g., 22:00 to 06:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        }
        
        Log.i(TAG, "Is business hours: $isBusinessHours")
        return isBusinessHours
    }

    private fun triggerCallBlocking(context: Context, phoneNumber: String?) {
        Log.i(TAG, "=== TRIGGERING CALL BLOCKING ===")
        Log.i(TAG, "Phone number: $phoneNumber")
        
        try {
            // Check if accessibility service is enabled
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val isAccessibilityEnabled = accessibilityManager.isEnabled
            
            Log.i(TAG, "Accessibility enabled: $isAccessibilityEnabled")
            
            if (!isAccessibilityEnabled) {
                Log.w(TAG, "Accessibility service is not enabled - cannot block call")
                return
            }
            
            // Wait a moment for the call screen to appear, then try to decline
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    Log.i(TAG, "Attempting to decline call via accessibility service...")
                    // The accessibility service should handle the actual call declining
                    // This is just a trigger to ensure it's active
                } catch (e: Exception) {
                    Log.e(TAG, "Error triggering call blocking: ${e.message}")
                    e.printStackTrace()
                }
            }, 1000) // Wait 1 second for call screen to appear
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in triggerCallBlocking: ${e.message}")
            e.printStackTrace()
        }
    }
}

