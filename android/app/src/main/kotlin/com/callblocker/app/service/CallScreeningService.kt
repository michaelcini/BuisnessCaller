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
        Log.i(TAG, "CallScreeningService created - Service is active!")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "CallScreeningService destroyed")
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        val callId = callDetails.callId
        Log.i(TAG, "=== CALL SCREENING STARTED ===")
        Log.i(TAG, "Call ID: $callId")
        Log.i(TAG, "Phone Number: $phoneNumber")
        Log.i(TAG, "Call State: ${callDetails.state}")
        Log.i(TAG, "Call Capabilities: ${callDetails.capabilities}")
        Log.i(TAG, "Call Properties: ${callDetails.callProperties}")

        val shouldBlock = shouldBlockCall(phoneNumber)
        
        if (shouldBlock) {
            Log.w(TAG, "BLOCKING call from: $phoneNumber")
            Log.i(TAG, "Call will be rejected and logged")
            // Actually block the call
            respondToCall(callDetails, CallResponse.Builder()
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build())
            Log.i(TAG, "Call blocking response sent")
        } else {
            Log.i(TAG, "ALLOWING call from: $phoneNumber")
            Log.i(TAG, "Call will be allowed to proceed")
            // Allow the call
            respondToCall(callDetails, CallResponse.Builder()
                .setRejectCall(false)
                .build())
            Log.i(TAG, "Call allow response sent")
        }
        Log.i(TAG, "=== CALL SCREENING COMPLETED ===")
    }

    private fun shouldBlockCall(phoneNumber: String?): Boolean {
        Log.d(TAG, "=== EVALUATING CALL BLOCKING ===")
        Log.d(TAG, "Phone number: $phoneNumber")
        
        if (phoneNumber == null) {
            Log.w(TAG, "Phone number is null, allowing call")
            return false
        }
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "Retrieved SharedPreferences: $PREFS_NAME")
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, allowing call")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i(TAG, "Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.i(TAG, "Call blocking is disabled, allowing call")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            Log.i(TAG, "Is business hours: $isBusinessHours")
            if (isBusinessHours) {
                Log.i(TAG, "Currently in business hours, allowing call")
                return false
            }
            
            Log.w(TAG, "Outside business hours, BLOCKING call from $phoneNumber")
            Log.i(TAG, "=== CALL WILL BE BLOCKED ===")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking call blocking settings: ${e.message}")
            e.printStackTrace()
            Log.i(TAG, "Allowing call due to error")
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
        Log.i(TAG, "Current time in minutes: $currentTimeMinutes")
        
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
        
        Log.d(TAG, "Start time in minutes: $startTimeMinutes")
        Log.d(TAG, "End time in minutes: $endTimeMinutes")
        
        val result = if (startTimeMinutes <= endTimeMinutes) {
            // Normal schedule (e.g., 9:00 to 17:00)
            val isInRange = currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
            Log.d(TAG, "Normal schedule check: $currentTimeMinutes >= $startTimeMinutes && $currentTimeMinutes <= $endTimeMinutes = $isInRange")
            isInRange
        } else {
            // Overnight schedule (e.g., 22:00 to 06:00)
            val isInRange = currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
            Log.d(TAG, "Overnight schedule check: $currentTimeMinutes >= $startTimeMinutes || $currentTimeMinutes <= $endTimeMinutes = $isInRange")
            isInRange
        }
        
        Log.i(TAG, "Is business hours result: $result")
        Log.d(TAG, "=== BUSINESS HOURS CHECK COMPLETE ===")
        return result
    }
}
