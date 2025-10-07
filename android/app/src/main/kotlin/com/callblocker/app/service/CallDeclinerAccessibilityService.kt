package com.callblocker.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Calendar

class CallDeclinerAccessibilityService : AccessibilityService() {
    companion object {
        const val TAG = "CallDeclinerAccessibilityService"
        const val PREFS_NAME = "call_blocker_settings"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "=== ACCESSIBILITY SERVICE CONNECTED ===")
        Log.i(TAG, "Call Decliner Accessibility Service is now active!")
        Log.i(TAG, "Service will automatically decline calls during non-business hours")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        Log.d(TAG, "Window state changed - Package: $packageName, Class: $className")
        
        // Check if this is a call screen
        if (isCallScreen(packageName, className)) {
            Log.i(TAG, "=== INCOMING CALL DETECTED ===")
            Log.i(TAG, "Package: $packageName")
            Log.i(TAG, "Class: $className")
            
            // Check if we should decline the call
            if (shouldDeclineCall()) {
                Log.i(TAG, "Call should be declined - attempting to decline...")
                declineCall()
            } else {
                Log.i(TAG, "Call should be allowed - not declining")
            }
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        // This can be used for more detailed call screen detection
        val packageName = event.packageName?.toString()
        if (isCallScreen(packageName, null)) {
            Log.d(TAG, "Call screen content changed - Package: $packageName")
        }
    }

    private fun isCallScreen(packageName: String?, className: String?): Boolean {
        // Common call screen packages and classes
        val callPackages = listOf(
            "com.android.incallui",
            "com.android.phone",
            "com.samsung.android.incallui",
            "com.google.android.dialer",
            "com.oneplus.incallui",
            "com.miui.incallui",
            "com.huawei.incallui"
        )
        
        val callClasses = listOf(
            "com.android.incallui.InCallActivity",
            "com.android.incallui.InCallServiceImpl",
            "com.android.phone.InCallScreen",
            "com.samsung.android.incallui.InCallActivity",
            "com.google.android.dialer.app.incallui.InCallActivity"
        )
        
        val isCallPackage = packageName?.let { callPackages.contains(it) } ?: false
        val isCallClass = className?.let { callClasses.contains(it) } ?: false
        
        Log.d(TAG, "Checking call screen - Package: $isCallPackage, Class: $isCallClass")
        
        return isCallPackage || isCallClass
    }

    private fun shouldDeclineCall(): Boolean {
        Log.d(TAG, "=== CHECKING IF CALL SHOULD BE DECLINED ===")
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if app is enabled
            val isEnabled = prefs.getBoolean("isEnabled", false)
            Log.i(TAG, "App enabled: $isEnabled")
            if (!isEnabled) {
                Log.i(TAG, "App is disabled, not declining call")
                return false
            }
            
            // Check if call blocking is enabled
            val blockCalls = prefs.getBoolean("blockCalls", true)
            Log.i(TAG, "Call blocking enabled: $blockCalls")
            if (!blockCalls) {
                Log.i(TAG, "Call blocking is disabled, not declining call")
                return false
            }
            
            // Check if accessibility service is enabled
            val accessibilityEnabled = prefs.getBoolean("accessibilityEnabled", true)
            Log.i(TAG, "Accessibility service enabled: $accessibilityEnabled")
            if (!accessibilityEnabled) {
                Log.i(TAG, "Accessibility service is disabled, not declining call")
                return false
            }
            
            // Check if current time is outside business hours
            val isBusinessHours = isBusinessHours(prefs)
            val shouldDecline = !isBusinessHours
            Log.i(TAG, "Is business hours: $isBusinessHours")
            Log.i(TAG, "Should decline call (outside business hours): $shouldDecline")
            
            return shouldDecline
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking call decline settings: ${e.message}")
            e.printStackTrace()
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

    private fun declineCall() {
        Log.i(TAG, "=== ATTEMPTING TO DECLINE CALL ===")
        
        try {
            // Find the root node
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.w(TAG, "Root node is null, cannot decline call")
                return
            }
            
            // Look for decline/end call buttons
            val declineButtons = findDeclineButtons(rootNode)
            Log.i(TAG, "Found ${declineButtons.size} decline buttons")
            
            if (declineButtons.isNotEmpty()) {
                // Click the first decline button found
                val button = declineButtons[0]
                Log.i(TAG, "Clicking decline button: ${button.text}")
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.i(TAG, "Call declined successfully!")
                
                // Send SMS auto-reply if enabled
                sendAutoReplySMS()
            } else {
                Log.w(TAG, "No decline buttons found")
                // Try alternative methods
                tryAlternativeDeclineMethods(rootNode)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error declining call: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun findDeclineButtons(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val declineButtons = mutableListOf<AccessibilityNodeInfo>()
        
        // Common decline button texts and content descriptions
        val declineTexts = listOf(
            "Decline", "Reject", "End", "Hang up", "Cancel",
            "decline", "reject", "end", "hang up", "cancel",
            "DECLINE", "REJECT", "END", "HANG UP", "CANCEL"
        )
        
        // Common decline button content descriptions
        val declineContentDescriptions = listOf(
            "Decline call", "Reject call", "End call", "Hang up call",
            "decline call", "reject call", "end call", "hang up call"
        )
        
        fun searchNodes(node: AccessibilityNodeInfo) {
            if (node == null) return
            
            // Check if this node is clickable and has decline text
            if (node.isClickable) {
                val text = node.text?.toString()?.lowercase()
                val contentDesc = node.contentDescription?.toString()?.lowercase()
                
                if (text != null && declineTexts.any { text.contains(it.lowercase()) }) {
                    Log.d(TAG, "Found decline button by text: $text")
                    declineButtons.add(node)
                } else if (contentDesc != null && declineContentDescriptions.any { contentDesc.contains(it.lowercase()) }) {
                    Log.d(TAG, "Found decline button by content description: $contentDesc")
                    declineButtons.add(node)
                }
            }
            
            // Search child nodes
            for (i in 0 until node.childCount) {
                searchNodes(node.getChild(i))
            }
        }
        
        searchNodes(rootNode)
        return declineButtons
    }

    private fun tryAlternativeDeclineMethods(rootNode: AccessibilityNodeInfo) {
        Log.i(TAG, "Trying alternative decline methods...")
        
        try {
            // Try to find any clickable element that might be a decline button
            val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
            
            fun findClickableNodes(node: AccessibilityNodeInfo) {
                if (node == null) return
                
                if (node.isClickable) {
                    clickableNodes.add(node)
                }
                
                for (i in 0 until node.childCount) {
                    findClickableNodes(node.getChild(i))
                }
            }
            
            findClickableNodes(rootNode)
            
            Log.i(TAG, "Found ${clickableNodes.size} clickable nodes")
            
            // Try clicking nodes that might be decline buttons
            for (node in clickableNodes) {
                val text = node.text?.toString()?.lowercase() ?: ""
                val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                
                if (text.contains("call") || contentDesc.contains("call") ||
                    text.contains("phone") || contentDesc.contains("phone")) {
                    Log.i(TAG, "Trying to click potential decline button: $text / $contentDesc")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    break
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in alternative decline methods: ${e.message}")
        }
    }

    private fun sendAutoReplySMS() {
        Log.i(TAG, "=== SENDING AUTO-REPLY SMS ===")
        
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Check if SMS auto-reply is enabled
            val sendSMS = prefs.getBoolean("sendSMS", true)
            Log.i(TAG, "SMS auto-reply enabled: $sendSMS")
            if (!sendSMS) {
                Log.i(TAG, "SMS auto-reply is disabled")
                return
            }
            
            // Get the custom message
            val customMessage = prefs.getString("customMessage", 
                "I am currently unavailable. Please call back during business hours.")
            
            Log.i(TAG, "Auto-reply message: $customMessage")
            
            // Note: We can't get the caller's number from the accessibility service
            // This would need to be handled by the PhoneStateReceiver
            Log.i(TAG, "SMS auto-reply will be handled by PhoneStateReceiver")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in SMS auto-reply: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Accessibility service destroyed")
    }
}