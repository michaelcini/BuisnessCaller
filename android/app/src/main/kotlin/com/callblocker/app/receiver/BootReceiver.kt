package com.callblocker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.callblocker.app.service.CallBlockerService

class BootReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Boot completed or package replaced, starting service")
                startCallBlockerService(context)
            }
        }
    }

    private fun startCallBlockerService(context: Context) {
        val serviceIntent = Intent(context, CallBlockerService::class.java).apply {
            action = CallBlockerService.ACTION_START_SERVICE
        }
        
        try {
            context.startForegroundService(serviceIntent)
            Log.d(TAG, "CallBlockerService started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start CallBlockerService", e)
        }
    }
}

