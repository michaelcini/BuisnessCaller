package com.callblocker.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityManager
import java.util.Calendar

class EnhancedCallBlockerService : AccessibilityService() {
    companion object {
        const val TAG = "EnhancedCallBlockerService"
        const val PREFS_NAME = "call_blocker_settings"
        
        // Call screen detection patterns
        private val CALL_PACKAGES = listOf(
            "com.android.incallui",
            "com.android.phone",
            "com.samsung.android.incallui",
            "com.google.android.dialer",
            "com.oneplus.incallui",
            "com.miui.incallui",
            "com.huawei.incallui",
            "com.oppo.incallui",
            "com.vivo.incallui",
            "com.realme.incallui",
            "com.android.dialer",
            "com.android.server.telecom",
            "com.android.telecom"
        )
        
        private val DECLINE_BUTTON_TEXTS = listOf(
            "Decline", "Reject", "End", "Hang up", "Cancel", "Dismiss",
            "decline", "reject", "end", "hang up", "cancel", "dismiss",
            "DECLINE", "REJECT", "END", "HANG UP", "CANCEL", "DISMISS",
            "✕", "×", "❌", "⛔", "🚫", "No", "NO", "no"
        )
        
        private val DECLINE_CONTENT_DESCRIPTIONS = listOf(
            "Decline call", "Reject call", "End call", "Hang up call", "Cancel call",
            "decline call", "reject call", "end call", "hang up call", "cancel call"
        )
    }

    private var isCallScreenActive = false
    private var callBlockAttempts = 0
    private val maxBlockAttempts = 3
    private val blockAttemptDelay = 500L // 500ms between attempts

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "=== ENHANCED CALL BLOCKER SERVICE CONNECTED ===")
        Log.i(TAG, "Service is now active and monitoring for calls")
        
        // Configure service for maximum compatibility
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                        AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_VIEW_FOCUSED or
                        AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                   AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 50
            canRetrieveWindowContent = true
        }
        serviceInfo = info
        
        Log.i(TAG, "Service configured for enhanced call blocking")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                handleViewFocused(event)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        Log.d(TAG, "Window state changed - Package: $packageName, Class: $className")
        
        if (isCallScreen(packageName, className)) {
            Log.i(TAG, "=== CALL SCREEN DETECTED ===")
            Log.i(TAG, "Package: $packageName, Class: $className")
            isCallScreenActive = true
            callBlockAttempts = 0
            
            // Check if we should block this call
            if (shouldBlockCall()) {
                Log.i(TAG, "Call should be blocked - starting decline process")
                attemptCallDecline()
            } else {
                Log.i(TAG, "Call should be allowed - not blocking")
            }
        } else {
            isCallScreenActive = false
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        if (isCallScreenActive && isCallScreen(event.packageName?.toString(), event.className?.toString())) {
            Log.d(TAG, "Call screen content changed - attempting decline")
            if (shouldBlockCall()) {
                attemptCallDecline()
            }
        }
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        if (isCallScreenActive) {
            Log.d(TAG, "View clicked in call screen")
        }
    }

    private fun handleViewFocused(event: AccessibilityEvent) {
        if (isCallScreenActive) {
            Log.d(TAG, "View focused in call screen")
        }
    }

    private fun isCallScreen(packageName: String?, className: String?): Boolean {
        if (packageName == null) return false
        
        val packageNameLower = packageName.lowercase()
        val classNameLower = className?.lowercase() ?: ""
        
        val isCallPackage = CALL_PACKAGES.any { it.lowercase() == packageNameLower } ||
                           packageNameLower.contains("incallui") ||
                           packageNameLower.contains("dialer") ||
                           packageNameLower.contains("phone") ||
                           packageNameLower.contains("telecom")
        
        val isCallClass = classNameLower.contains("incall") ||
                         classNameLower.contains("incallui") ||
                         classNameLower.contains("incallactivity") ||
                         classNameLower.contains("incallservice")
        
        return isCallPackage || isCallClass
    }

    private fun shouldBlockCall(): Boolean {
        Log.d(TAG, "=== CHECKING IF CALL SHOULD BE BLOCKED ===")
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, not blocking call")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i(TAG, "Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.i(TAG, "Call blocking is disabled, not blocking call")
                return false
            }
            
            // Check if accessibility service is enabled
            val accessibilityEnabled = prefs.getBoolean("accessibilityEnabled", true)
            Log.i(TAG, "Accessibility service enabled: $accessibilityEnabled")
            if (!accessibilityEnabled) {
                Log.i(TAG, "Accessibility service is disabled, not blocking call")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            val shouldBlock = !isBusinessHours
            Log.i(TAG, "Is business hours: $isBusinessHours")
            Log.i(TAG, "Should block call (outside business hours): $shouldBlock")
            
            return shouldBlock
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking call block settings: ${e.message}")
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

    private fun attemptCallDecline() {
        if (callBlockAttempts >= maxBlockAttempts) {
            Log.w(TAG, "Maximum decline attempts reached, giving up")
            return
        }
        
        callBlockAttempts++
        Log.i(TAG, "=== ATTEMPTING CALL DECLINE (Attempt $callBlockAttempts/$maxBlockAttempts) ===")
        
        try {
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.w(TAG, "Root node is null, cannot decline call")
                scheduleRetry()
                return
            }
            
            val declineButtons = findDeclineButtons(rootNode)
            Log.i(TAG, "Found ${declineButtons.size} decline buttons")
            
            if (declineButtons.isNotEmpty()) {
                val button = declineButtons[0]
                val buttonText = button.text?.toString() ?: "Unknown"
                val buttonDesc = button.contentDescription?.toString() ?: "Unknown"
                
                Log.i(TAG, "Clicking decline button - Text: '$buttonText', Description: '$buttonDesc'")
                
                // Try multiple click methods
                var success = false
                
                // Method 1: Direct click
                try {
                    success = button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.i(TAG, "Direct click result: $success")
                } catch (e: Exception) {
                    Log.w(TAG, "Direct click failed: ${e.message}")
                }
                
                // Method 2: Long click if direct click failed
                if (!success) {
                    try {
                        success = button.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                        Log.i(TAG, "Long click result: $success")
                    } catch (e: Exception) {
                        Log.w(TAG, "Long click failed: ${e.message}")
                    }
                }
                
                if (success) {
                    Log.i(TAG, "Call declined successfully!")
                    callBlockAttempts = 0 // Reset attempts on success
                } else {
                    Log.w(TAG, "All click methods failed, scheduling retry")
                    scheduleRetry()
                }
            } else {
                Log.w(TAG, "No decline buttons found, scheduling retry")
                scheduleRetry()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in attemptCallDecline: ${e.message}")
            e.printStackTrace()
            scheduleRetry()
        }
    }

    private fun scheduleRetry() {
        if (callBlockAttempts < maxBlockAttempts) {
            Log.i(TAG, "Scheduling retry in ${blockAttemptDelay}ms")
            Handler(Looper.getMainLooper()).postDelayed({
                attemptCallDecline()
            }, blockAttemptDelay)
        }
    }

    private fun findDeclineButtons(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val declineButtons = mutableListOf<AccessibilityNodeInfo>()
        
        fun searchNodes(node: AccessibilityNodeInfo, depth: Int = 0) {
            if (node == null) return
            
            if (node.isClickable) {
                val text = node.text?.toString()?.lowercase() ?: ""
                val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                
                val isDeclineByText = text.isNotEmpty() && DECLINE_BUTTON_TEXTS.any { text.contains(it.lowercase()) }
                val isDeclineByContentDesc = contentDesc.isNotEmpty() && DECLINE_CONTENT_DESCRIPTIONS.any { contentDesc.contains(it.lowercase()) }
                
                if (isDeclineByText || isDeclineByContentDesc) {
                    Log.i(TAG, "Found decline button - Text: '$text', Content: '$contentDesc'")
                    declineButtons.add(node)
                }
            }
            
            // Search child nodes
            for (i in 0 until node.childCount) {
                searchNodes(node.getChild(i), depth + 1)
            }
        }
        
        searchNodes(rootNode)
        return declineButtons
    }

    override fun onInterrupt() {
        Log.i(TAG, "Enhanced call blocker service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Enhanced call blocker service destroyed")
    }
}