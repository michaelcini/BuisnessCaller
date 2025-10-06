package com.callblocker.app.service

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import java.util.Calendar

class DNDService(private val context: Context) {
    companion object {
        const val TAG = "DNDService"
        const val PREFS_NAME = "call_blocker_settings"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun isDNDSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun hasDNDPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun isDNDEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            false
        }
    }

    fun enableDND(): Boolean {
        Log.i(TAG, "=== ENABLING DND ===")
        
        if (!isDNDSupported()) {
            Log.w(TAG, "DND not supported on this Android version")
            return false
        }

        if (!hasDNDPermission()) {
            Log.w(TAG, "DND permission not granted")
            return false
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                Log.i(TAG, "DND enabled successfully")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling DND: ${e.message}")
        }
        
        return false
    }

    fun disableDND(): Boolean {
        Log.i(TAG, "=== DISABLING DND ===")
        
        if (!isDNDSupported()) {
            Log.w(TAG, "DND not supported on this Android version")
            return false
        }

        if (!hasDNDPermission()) {
            Log.w(TAG, "DND permission not granted")
            return false
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                Log.i(TAG, "DND disabled successfully")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling DND: ${e.message}")
        }
        
        return false
    }

    fun shouldEnableDND(): Boolean {
        Log.d(TAG, "=== CHECKING IF DND SHOULD BE ENABLED ===")
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, not enabling DND")
                return false
            }
            
            // Check if DND mode is enabled
            val dndEnabled = prefs.getBoolean("dndEnabled", true)
            Log.i(TAG, "DND mode enabled: $dndEnabled")
            if (!dndEnabled) {
                Log.i(TAG, "DND mode is disabled in settings")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            val shouldEnable = !isBusinessHours
            Log.i(TAG, "Is business hours: $isBusinessHours")
            Log.i(TAG, "Should enable DND (outside business hours): $shouldEnable")
            
            return shouldEnable
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking DND settings: ${e.message}")
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

    fun getDNDStatus(): String {
        return when {
            !isDNDSupported() -> "Not supported on this Android version"
            !hasDNDPermission() -> "Permission not granted"
            isDNDEnabled() -> "Enabled"
            else -> "Disabled"
        }
    }
}