package com.callblocker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import java.util.Calendar

class SMSReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "SMSReceiver"
        const val PREFS_NAME = "call_blocker_settings"
        var methodChannel: MethodChannel? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (message in messages) {
                val phoneNumber = message.originatingAddress
                val messageBody = message.messageBody
                val timestamp = message.timestampMillis
                
                Log.d(TAG, "SMS received from: $phoneNumber, Body: $messageBody")
                
                // Check if we should send auto-reply
                if (shouldSendAutoReply(context)) {
                    sendAutoReply(context, phoneNumber)
                }
                
                methodChannel?.invokeMethod("onSMSReceived", mapOf(
                    "phoneNumber" to phoneNumber,
                    "messageBody" to messageBody,
                    "timestamp" to timestamp
                ))
            }
        }
    }

    private fun shouldSendAutoReply(context: Context): Boolean {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            if (!isEnabled) return false
            
            // Check if SMS auto-reply is enabled
            val sendSMS = prefs.getBoolean("sendSMS", true)
            if (!sendSMS) return false
            
            // Check if current time is outside business hours
            return !isBusinessHours(prefs)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking SMS auto-reply settings: ${e.message}")
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
        
        // Check if this day is enabled
        val dayEnabled = prefs.getBoolean("${dayName}_enabled", true)
        if (!dayEnabled) {
            return false
        }
        
        // Get start and end times for this day
        val startHour = prefs.getInt("${dayName}_startHour", 9)
        val startMinute = prefs.getInt("${dayName}_startMinute", 0)
        val endHour = prefs.getInt("${dayName}_endHour", 17)
        val endMinute = prefs.getInt("${dayName}_endMinute", 0)
        
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute
        
        return if (startTimeMinutes <= endTimeMinutes) {
            // Normal schedule (e.g., 9:00 to 17:00)
            currentTimeMinutes >= startTimeMinutes && currentTimeMinutes <= endTimeMinutes
        } else {
            // Overnight schedule (e.g., 22:00 to 06:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        }
    }

    private fun sendAutoReply(context: Context, phoneNumber: String?) {
        if (phoneNumber == null) return
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val customMessage = prefs.getString("customMessage", 
                "I am currently unavailable. Please call back during business hours.")
            
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, customMessage, null, null)
            
            Log.d(TAG, "Auto-reply sent to $phoneNumber: $customMessage")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending auto-reply: ${e.message}")
        }
    }
}

