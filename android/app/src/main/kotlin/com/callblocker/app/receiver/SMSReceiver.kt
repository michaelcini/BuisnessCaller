package com.callblocker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import io.flutter.plugin.common.MethodChannel

class SMSReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "SMSReceiver"
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
                
                methodChannel?.invokeMethod("onSMSReceived", mapOf(
                    "phoneNumber" to phoneNumber,
                    "messageBody" to messageBody,
                    "timestamp" to timestamp
                ))
            }
        }
    }
}

