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

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        Log.d(TAG, "Screening call from: $phoneNumber")

        val shouldBlock = shouldBlockCall(phoneNumber)
        
        if (shouldBlock) {
            Log.d(TAG, "BLOCKING call from: $phoneNumber")
            // Actually block the call
            respondToCall(callDetails, CallResponse.Builder()
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build())
        } else {
            Log.d(TAG, "ALLOWING call from: $phoneNumber")
            // Allow the call
            respondToCall(callDetails, CallResponse.Builder()
                .setRejectCall(false)
                .build())
        }
    }

    private fun shouldBlockCall(phoneNumber: String?): Boolean {
        if (phoneNumber == null) {
            Log.w(TAG, "Phone number is null, allowing call")
            return false
        }
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.d(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.d(TAG, "App is disabled, allowing call")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.d(TAG, "Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.d(TAG, "Call blocking is disabled, allowing call")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            Log.d(TAG, "Is business hours: $isBusinessHours")
            if (isBusinessHours) {
                Log.d(TAG, "Currently in business hours, allowing call")
                return false
            }
            
            Log.d(TAG, "Outside business hours, BLOCKING call from $phoneNumber")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking call blocking settings: ${e.message}")
            return false
        }
    }

    private fun isBusinessHours(prefs: SharedPreferences): Boolean {
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
        
        Log.d(TAG, "Current time: $currentHour:$currentMinute, Day: $dayName")
        
        // Check if this day is enabled
        val dayEnabled = prefs.getBoolean("${dayName}_enabled", true)
        Log.d(TAG, "Day enabled: $dayEnabled")
        if (!dayEnabled) {
            return false
        }
        
        // Get start and end times for this day
        val startHour = prefs.getInt("${dayName}_startHour", 9)
        val startMinute = prefs.getInt("${dayName}_startMinute", 0)
        val endHour = prefs.getInt("${dayName}_endHour", 17)
        val endMinute = prefs.getInt("${dayName}_endMinute", 0)
        
        Log.d(TAG, "Business hours: $startHour:$startMinute to $endHour:$endMinute")
        
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute
        
        val result = if (startTimeMinutes <= endTimeMinutes) {
            // Normal schedule (e.g., 9:00 to 17:00)
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
        } else {
            // Overnight schedule (e.g., 22:00 to 06:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        }
        
        Log.d(TAG, "Is business hours result: $result")
        return result
    }
}
