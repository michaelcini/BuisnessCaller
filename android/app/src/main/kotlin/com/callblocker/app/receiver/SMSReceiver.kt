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

    init {
        Log.i(TAG, "SMSReceiver initialized")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "=== SMS RECEIVER TRIGGERED ===")
        Log.d(TAG, "Action: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras}")
        
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            Log.i(TAG, "Processing ${messages.size} SMS message(s)")
            
            for ((index, message) in messages.withIndex()) {
                val phoneNumber = message.originatingAddress
                val messageBody = message.messageBody
                val timestamp = message.timestampMillis
                
                Log.i(TAG, "SMS #${index + 1} received from: $phoneNumber")
                Log.d(TAG, "SMS body: $messageBody")
                Log.d(TAG, "SMS timestamp: $timestamp")
                
                // Check if we should send auto-reply
                val shouldReply = shouldSendAutoReply(context)
                Log.i(TAG, "Should send auto-reply: $shouldReply")
                
                if (shouldReply) {
                    Log.i(TAG, "Sending auto-reply to: $phoneNumber")
                    sendAutoReply(context, phoneNumber)
                } else {
                    Log.d(TAG, "Not sending auto-reply (business hours or disabled)")
                }
                
                // Notify Flutter
                if (methodChannel != null) {
                    try {
                        methodChannel?.invokeMethod("onSMSReceived", mapOf(
                            "phoneNumber" to phoneNumber,
                            "messageBody" to messageBody,
                            "timestamp" to timestamp
                        ))
                        Log.i(TAG, "Successfully notified Flutter about SMS")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error notifying Flutter about SMS: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    Log.e(TAG, "MethodChannel is null - cannot notify Flutter")
                }
            }
        } else {
            Log.d(TAG, "Ignoring non-SMS intent: ${intent.action}")
        }
        Log.d(TAG, "=== SMS RECEIVER COMPLETE ===")
    }

    private fun shouldSendAutoReply(context: Context): Boolean {
        Log.d(TAG, "=== CHECKING SMS AUTO-REPLY ===")
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, not sending auto-reply")
                return false
            }
            
            // Check if SMS auto-reply is enabled
            val sendSMS = prefs.getBoolean("sendSMS", true)
            Log.i(TAG, "SMS auto-reply enabled: $sendSMS")
            if (!sendSMS) {
                Log.i(TAG, "SMS auto-reply is disabled")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            val shouldReply = !isBusinessHours
            Log.i(TAG, "Is business hours: $isBusinessHours")
            Log.i(TAG, "Should send auto-reply: $shouldReply")
            return shouldReply
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking SMS auto-reply settings: ${e.message}")
            e.printStackTrace()
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
        Log.d(TAG, "=== SENDING AUTO-REPLY ===")
        Log.d(TAG, "Phone number: $phoneNumber")
        
        if (phoneNumber == null) {
            Log.w(TAG, "Phone number is null, cannot send auto-reply")
            return
        }
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val customMessage = prefs.getString("customMessage", 
                "I am currently unavailable. Please call back during business hours.")
            
            Log.i(TAG, "Sending auto-reply to: $phoneNumber")
            Log.d(TAG, "Message: $customMessage")
            
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, customMessage, null, null)
            
            Log.i(TAG, "Auto-reply sent successfully to $phoneNumber")
            Log.d(TAG, "=== AUTO-REPLY SENT ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending auto-reply to $phoneNumber: ${e.message}")
            e.printStackTrace()
        }
    }
}

