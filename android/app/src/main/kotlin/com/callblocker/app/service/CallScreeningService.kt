package com.callblocker.app.service

import android.telecom.CallScreeningService
import android.telecom.Call
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Calendar

class CallScreeningService : CallScreeningService() {
    companion object {
        const val TAG = "CallScreeningService"
        const val PREFS_NAME = "call_blocker_settings"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "=== CALL SCREENING SERVICE CREATED ===")
        Log.i(TAG, "Service is now active and ready to screen calls!")
        Log.i(TAG, "Package name: ${packageName}")
        Log.i(TAG, "Service class: ${this.javaClass.simpleName}")
        Log.i(TAG, "=== CALL SCREENING SERVICE READY ===")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "=== CALL SCREENING SERVICE DESTROYED ===")
        Log.i(TAG, "Service is no longer active")
        Log.i(TAG, "=== CALL SCREENING SERVICE SHUTDOWN ===")
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        val timestamp = System.currentTimeMillis()
        
        Log.i(TAG, "=== INCOMING CALL DETECTED ===")
        Log.i(TAG, "Timestamp: $timestamp")
        Log.i(TAG, "Phone Number: $phoneNumber")
        Log.i(TAG, "Call State: ${callDetails.state}")
        Log.i(TAG, "Call Handle: ${callDetails.handle}")
        Log.i(TAG, "Call Creation Time: ${callDetails.creationTimeMillis}")
        Log.i(TAG, "Call Direction: ${callDetails.callDirection}")
        Log.i(TAG, "Call Capabilities: ${callDetails.callCapabilities}")
        Log.i(TAG, "Call Properties: ${callDetails.callProperties}")
        Log.i(TAG, "Call Disconnect Cause: ${callDetails.disconnectCause}")
        Log.i(TAG, "Call Gateway Info: ${callDetails.gatewayInfo}")
        Log.i(TAG, "Call Video State: ${callDetails.videoState}")
        Log.i(TAG, "Call Conferenceable Calls: ${callDetails.conferenceableCalls}")
        Log.i(TAG, "Call Extras: ${callDetails.extras}")
        Log.i(TAG, "Call Intent Extras: ${callDetails.intentExtras}")

        Log.i(TAG, "=== EVALUATING CALL BLOCKING DECISION ===")
        val shouldBlock = shouldBlockCall(phoneNumber)
        Log.i(TAG, "Blocking decision: $shouldBlock")
        
        if (shouldBlock) {
            Log.w(TAG, "🚫 BLOCKING CALL FROM: $phoneNumber")
            Log.w(TAG, "Call will be rejected and logged")
            Log.w(TAG, "Reason: Outside business hours or app settings")
            
            try {
                // Actually block the call
                val response = CallResponse.Builder()
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
                
                respondToCall(callDetails, response)
                Log.i(TAG, "✅ Call blocking response sent successfully")
                Log.i(TAG, "Call has been blocked and will not ring")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send call blocking response: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.i(TAG, "✅ ALLOWING CALL FROM: $phoneNumber")
            Log.i(TAG, "Call will be allowed to proceed normally")
            Log.i(TAG, "Reason: Within business hours or app disabled")
            
            try {
                // Allow the call
                val response = CallResponse.Builder()
                    .setRejectCall(false)
                    .build()
                
                respondToCall(callDetails, response)
                Log.i(TAG, "✅ Call allow response sent successfully")
                Log.i(TAG, "Call will ring normally")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send call allow response: ${e.message}")
                e.printStackTrace()
            }
        }
        Log.i(TAG, "=== CALL SCREENING PROCESS COMPLETED ===")
    }

    private fun shouldBlockCall(phoneNumber: String?): Boolean {
        Log.i(TAG, "=== EVALUATING CALL BLOCKING DECISION ===")
        Log.i(TAG, "Phone number: $phoneNumber")
        Log.i(TAG, "Current time: ${System.currentTimeMillis()}")
        
        if (phoneNumber == null) {
            Log.w(TAG, "⚠️ Phone number is null, allowing call")
            Log.w(TAG, "Reason: Cannot evaluate blocking without phone number")
            return false
        }
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.i(TAG, "Retrieved SharedPreferences: $PREFS_NAME")
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "📱 App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "✅ ALLOWING CALL - App is disabled")
                Log.i(TAG, "Reason: App is disabled in settings")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i(TAG, "🚫 Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.i(TAG, "✅ ALLOWING CALL - Call blocking is disabled")
                Log.i(TAG, "Reason: Call blocking is disabled in settings")
                return false
            }
            
            // Check if current time is outside business hours
            Log.i(TAG, "🕐 Checking business hours...")
            val isBusinessHours = isBusinessHours(prefs)
            Log.i(TAG, "🕐 Is business hours: $isBusinessHours")
            if (isBusinessHours) {
                Log.i(TAG, "✅ ALLOWING CALL - Currently in business hours")
                Log.i(TAG, "Reason: Call is within allowed business hours")
                return false
            }
            
            Log.w(TAG, "🚫 BLOCKING CALL - Outside business hours")
            Log.w(TAG, "Phone number: $phoneNumber")
            Log.w(TAG, "Reason: Call is outside business hours and app is enabled")
            Log.i(TAG, "=== DECISION: CALL WILL BE BLOCKED ===")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking call blocking settings: ${e.message}")
            e.printStackTrace()
            Log.w(TAG, "✅ ALLOWING CALL - Error occurred during evaluation")
            Log.w(TAG, "Reason: Exception occurred, defaulting to allow for safety")
            return false
        }
    }

    private fun isBusinessHours(prefs: SharedPreferences): Boolean {
        Log.i(TAG, "=== CHECKING BUSINESS HOURS ===")
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
        
        Log.i(TAG, "🕐 Current time: ${String.format("%02d:%02d", currentHour, currentMinute)}")
        Log.i(TAG, "📅 Current day: $dayName")
        Log.i(TAG, "⏰ Current time in minutes: $currentTimeMinutes")
        
        // Check if this day is enabled
        val dayEnabled = prefs.getBoolean("${dayName}_enabled", true)
        Log.i(TAG, "📅 Day '$dayName' enabled: $dayEnabled")
        if (!dayEnabled) {
            Log.i(TAG, "❌ Day is disabled, not business hours")
            Log.i(TAG, "Reason: $dayName is disabled in schedule")
            return false
        }
        
        // Get start and end times for this day
        val startHour = prefs.getInt("${dayName}_startHour", 9)
        val startMinute = prefs.getInt("${dayName}_startMinute", 0)
        val endHour = prefs.getInt("${dayName}_endHour", 17)
        val endMinute = prefs.getInt("${dayName}_endMinute", 0)
        
        Log.i(TAG, "🕐 Business hours for $dayName: ${String.format("%02d:%02d", startHour, startMinute)} to ${String.format("%02d:%02d", endHour, endMinute)}")
        
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute
        
        Log.i(TAG, "⏰ Start time in minutes: $startTimeMinutes")
        Log.i(TAG, "⏰ End time in minutes: $endTimeMinutes")
        
        val result = if (startTimeMinutes <= endTimeMinutes) {
            // Normal schedule (e.g., 9:00 to 17:00)
            val isInRange = currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
            Log.i(TAG, "📊 Normal schedule check: $currentTimeMinutes >= $startTimeMinutes && $currentTimeMinutes <= $endTimeMinutes = $isInRange")
            isInRange
        } else {
            // Overnight schedule (e.g., 22:00 to 06:00)
            val isInRange = currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
            Log.i(TAG, "📊 Overnight schedule check: $currentTimeMinutes >= $startTimeMinutes || $currentTimeMinutes <= $endTimeMinutes = $isInRange")
            isInRange
        }
        
        Log.i(TAG, "✅ Is business hours result: $result")
        if (result) {
            Log.i(TAG, "✅ Currently in business hours - calls allowed")
        } else {
            Log.i(TAG, "❌ Currently outside business hours - calls will be blocked")
        }
        Log.i(TAG, "=== BUSINESS HOURS CHECK COMPLETE ===")
        return result
    }
}
