package com.callblocker.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.ConcurrentLinkedQueue

class SystemEventLogger(private val context: Context) {
    companion object {
        const val TAG = "SystemEventLogger"
    }

    private var methodChannel: MethodChannel? = null
    private val eventQueue = ConcurrentLinkedQueue<SystemEvent>()
    private var isLogging = false
    private val receivers = mutableListOf<BroadcastReceiver>()

    data class SystemEvent(
        val timestamp: Long,
        val type: String,
        val category: String,
        val message: String,
        val data: Map<String, Any> = emptyMap()
    )

    fun setMethodChannel(channel: MethodChannel) {
        this.methodChannel = channel
    }

    fun startLogging() {
        if (isLogging) {
            Log.w(TAG, "System event logging is already active")
            return
        }

        Log.i(TAG, "Starting system event logging...")
        isLogging = true

        // Register broadcast receivers for system events
        registerSystemReceivers()

        // Start periodic event sending
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sendEventsToFlutter()
        }, 1000)

        Log.i(TAG, "System event logging started")
    }

    fun stopLogging() {
        if (!isLogging) {
            Log.w(TAG, "System event logging is not active")
            return
        }

        Log.i(TAG, "Stopping system event logging...")
        isLogging = false

        // Unregister all receivers
        receivers.forEach { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver: ${e.message}")
            }
        }
        receivers.clear()

        Log.i(TAG, "System event logging stopped")
    }

    private fun registerSystemReceivers() {
        // Phone state changes
        val phoneStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                logSystemEvent("PHONE_STATE", "TELEPHONY", "Phone state changed", mapOf(
                    "action" to (intent.action ?: "unknown"),
                    "state" to intent.getStringExtra(TelephonyManager.EXTRA_STATE),
                    "incomingNumber" to intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                ))
            }
        }
        context.registerReceiver(phoneStateReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
        receivers.add(phoneStateReceiver)

        // Battery changes
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

                logSystemEvent("BATTERY", "POWER", "Battery status changed", mapOf(
                    "level" to batteryLevel,
                    "isCharging" to isCharging,
                    "status" to intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                ))
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        receivers.add(batteryReceiver)

        // Screen on/off
        val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                val screenState = when (action) {
                    Intent.ACTION_SCREEN_ON -> "ON"
                    Intent.ACTION_SCREEN_OFF -> "OFF"
                    Intent.ACTION_USER_PRESENT -> "UNLOCKED"
                    else -> "UNKNOWN"
                }

                logSystemEvent("SCREEN", "DISPLAY", "Screen state changed", mapOf(
                    "action" to action,
                    "state" to screenState
                ))
            }
        }
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(screenReceiver, screenFilter)
        receivers.add(screenReceiver)

        // Network connectivity
        val networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                logSystemEvent("NETWORK", "CONNECTIVITY", "Network state changed", mapOf(
                    "action" to (intent.action ?: "unknown"),
                    "noConnectivity" to intent.getBooleanExtra("noConnectivity", false)
                ))
            }
        }
        context.registerReceiver(networkReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        receivers.add(networkReceiver)

        // Boot completed
        val bootReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                logSystemEvent("BOOT", "SYSTEM", "Device booted", mapOf(
                    "action" to (intent.action ?: "unknown")
                ))
            }
        }
        context.registerReceiver(bootReceiver, IntentFilter(Intent.ACTION_BOOT_COMPLETED))
        receivers.add(bootReceiver)

        // Package changes
        val packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                logSystemEvent("PACKAGE", "SYSTEM", "Package changed", mapOf(
                    "action" to (intent.action ?: "unknown"),
                    "package" to intent.data?.schemeSpecificPart
                ))
            }
        }
        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, packageFilter)
        receivers.add(packageReceiver)

        // SMS received
        val smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                logSystemEvent("SMS", "MESSAGING", "SMS received", mapOf(
                    "action" to (intent.action ?: "unknown")
                ))
            }
        }
        context.registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
        receivers.add(smsReceiver)
    }

    private fun logSystemEvent(type: String, category: String, message: String, data: Map<String, Any> = emptyMap()) {
        if (!isLogging) return

        val event = SystemEvent(
            timestamp = System.currentTimeMillis(),
            type = type,
            category = category,
            message = message,
            data = data
        )

        eventQueue.offer(event)

        // Log to Android logcat as well
        Log.d(TAG, "[$category] $type: $message - $data")
    }

    private fun sendEventsToFlutter() {
        if (!isLogging || methodChannel == null) return

        try {
            val eventsToSend = mutableListOf<SystemEvent>()
            repeat(50) { // Send up to 50 events at a time
                eventQueue.poll()?.let { event ->
                    eventsToSend.add(event)
                } ?: return@repeat
            }

            if (eventsToSend.isNotEmpty()) {
                val eventData = eventsToSend.map { event ->
                    mapOf(
                        "timestamp" to event.timestamp,
                        "type" to event.type,
                        "category" to event.category,
                        "message" to event.message,
                        "data" to event.data
                    )
                }

                methodChannel?.invokeMethod("onSystemEvent", mapOf(
                    "events" to eventData,
                    "count" to eventsToSend.size
                ))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending system events to Flutter: ${e.message}")
        }

        // Schedule next send
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sendEventsToFlutter()
        }, 1000)
    }

    fun logAccessibilityEvent(event: AccessibilityEvent) {
        if (!isLogging) return

        val eventType = when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WINDOW_STATE_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "VIEW_CLICKED"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> "VIEW_FOCUSED"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "VIEW_SCROLLED"
            else -> "UNKNOWN_ACCESSIBILITY_EVENT"
        }

        logSystemEvent("ACCESSIBILITY", "UI", "Accessibility event", mapOf(
            "eventType" to eventType,
            "packageName" to (event.packageName?.toString() ?: "unknown"),
            "className" to (event.className?.toString() ?: "unknown"),
            "text" to (event.text?.toString() ?: ""),
            "contentDescription" to (event.contentDescription?.toString() ?: "")
        ))
    }

    fun logCustomEvent(type: String, category: String, message: String, data: Map<String, Any> = emptyMap()) {
        logSystemEvent(type, category, message, data)
    }

    fun getEventStats(): Map<String, Any> {
        val typeCounts = eventQueue.groupBy { it.type }.mapValues { it.value.size }
        val categoryCounts = eventQueue.groupBy { it.category }.mapValues { it.value.size }

        return mapOf(
            "totalEvents" to eventQueue.size,
            "typeCounts" to typeCounts,
            "categoryCounts" to categoryCounts,
            "isLogging" to isLogging
        )
    }

    fun clearEvents() {
        eventQueue.clear()
    }
}