package com.callblocker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import io.flutter.plugin.common.MethodChannel

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
}

