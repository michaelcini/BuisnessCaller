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
        
        // Log service capabilities
        Log.i(TAG, "Service capabilities:")
        Log.i(TAG, "- Can retrieve window content: ${serviceInfo?.canRetrieveWindowContent}")
        Log.i(TAG, "- Event types: ${serviceInfo?.eventTypes}")
        Log.i(TAG, "- Feedback type: ${serviceInfo?.feedbackType}")
        Log.i(TAG, "- Flags: ${serviceInfo?.flags}")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                        AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 50
            canRetrieveWindowContent = true
        }
        serviceInfo = info
        
        Log.i(TAG, "Service configuration updated with enhanced event detection")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Log all events for debugging
        Log.d(TAG, "=== ACCESSIBILITY EVENT ===")
        Log.d(TAG, "Event type: ${event.eventType}")
        Log.d(TAG, "Package: ${event.packageName}")
        Log.d(TAG, "Class: ${event.className}")
        Log.d(TAG, "Text: ${event.text}")
        Log.d(TAG, "Content description: ${event.contentDescription}")
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "Processing WINDOW_STATE_CHANGED event")
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                Log.d(TAG, "Processing WINDOW_CONTENT_CHANGED event")
                handleWindowContentChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.d(TAG, "Processing VIEW_CLICKED event")
                handleViewClicked(event)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                Log.d(TAG, "Processing VIEW_FOCUSED event")
                handleViewFocused(event)
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
        val className = event.className?.toString()
        
        Log.d(TAG, "Window content changed - Package: $packageName, Class: $className")
        
        if (isCallScreen(packageName, className)) {
            Log.i(TAG, "=== CALL SCREEN CONTENT CHANGED ===")
            Log.i(TAG, "Package: $packageName, Class: $className")
            
            // Check if we should decline the call
            if (shouldDeclineCall()) {
                Log.i(TAG, "Call should be declined - attempting to decline...")
                declineCall()
            } else {
                Log.i(TAG, "Call should be allowed - not declining")
            }
        }
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val text = event.text?.toString()
        val contentDesc = event.contentDescription?.toString()
        
        Log.d(TAG, "View clicked - Package: $packageName, Text: $text, Content: $contentDesc")
        
        // Check if this might be a call-related click
        if (isCallScreen(packageName, null)) {
            Log.i(TAG, "Click detected in call screen")
        }
    }

    private fun handleViewFocused(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val text = event.text?.toString()
        val contentDesc = event.contentDescription?.toString()
        
        Log.d(TAG, "View focused - Package: $packageName, Text: $text, Content: $contentDesc")
        
        // Check if this might be a call-related focus
        if (isCallScreen(packageName, null)) {
            Log.i(TAG, "Focus detected in call screen")
        }
    }

    private fun isCallScreen(packageName: String?, className: String?): Boolean {
        Log.d(TAG, "=== CHECKING IF THIS IS A CALL SCREEN ===")
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "Class: $className")
        
        // Common call screen packages and classes
        val callPackages = listOf(
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
            "com.android.telecom",
            "com.android.phone.incallui",
            "com.android.incallui.incallui",
            "com.android.incallui.incallui2",
            "com.android.incallui.incallui3"
        )
        
        val callClasses = listOf(
            "com.android.incallui.InCallActivity",
            "com.android.incallui.InCallServiceImpl",
            "com.android.phone.InCallScreen",
            "com.samsung.android.incallui.InCallActivity",
            "com.google.android.dialer.app.incallui.InCallActivity",
            "com.oneplus.incallui.OPInCallActivity",
            "com.miui.incallui.InCallActivity",
            "com.huawei.incallui.InCallActivity",
            "com.oppo.incallui.OppoInCallActivity",
            "com.vivo.incallui.VivoInCallActivity",
            "com.realme.incallui.RealmeInCallActivity",
            "com.android.dialer.app.incallui.InCallActivity",
            "com.android.server.telecom.InCallActivity",
            "com.android.telecom.InCallActivity",
            "com.android.phone.incallui.InCallActivity",
            "com.android.incallui.incallui.InCallActivity",
            "com.android.incallui.incallui2.InCallActivity",
            "com.android.incallui.incallui3.InCallActivity"
        )
        
        // Check for partial matches (some devices use different naming)
        val packageNameLower = packageName?.lowercase() ?: ""
        val classNameLower = className?.lowercase() ?: ""
        
        val isCallPackage = callPackages.any { it.lowercase() == packageNameLower } ||
                           packageNameLower.contains("incallui") ||
                           packageNameLower.contains("dialer") ||
                           packageNameLower.contains("phone") ||
                           packageNameLower.contains("telecom")
        
        val isCallClass = callClasses.any { it.lowercase() == classNameLower } ||
                         classNameLower.contains("incall") ||
                         classNameLower.contains("incallui") ||
                         classNameLower.contains("incallactivity") ||
                         classNameLower.contains("incallservice")
        
        Log.d(TAG, "Call package match: $isCallPackage")
        Log.d(TAG, "Call class match: $isCallClass")
        Log.d(TAG, "Is call screen: ${isCallPackage || isCallClass}")
        
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
            
            Log.i(TAG, "Root node found, searching for decline buttons...")
            
            // Look for decline/end call buttons
            val declineButtons = findDeclineButtons(rootNode)
            Log.i(TAG, "Found ${declineButtons.size} decline buttons")
            
            if (declineButtons.isNotEmpty()) {
                // Click the first decline button found
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
                    // Send SMS auto-reply if enabled
                    sendAutoReplySMS()
                } else {
                    Log.w(TAG, "All click methods failed, trying alternative methods...")
                    tryAlternativeDeclineMethods(rootNode)
                }
            } else {
                Log.w(TAG, "No decline buttons found, trying alternative methods...")
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
        
        Log.d(TAG, "=== SEARCHING FOR DECLINE BUTTONS ===")
        
        // Common decline button texts and content descriptions
        val declineTexts = listOf(
            "Decline", "Reject", "End", "Hang up", "Cancel", "Dismiss",
            "decline", "reject", "end", "hang up", "cancel", "dismiss",
            "DECLINE", "REJECT", "END", "HANG UP", "CANCEL", "DISMISS",
            "✕", "×", "❌", "⛔", "🚫", "No", "NO", "no"
        )
        
        // Common decline button content descriptions
        val declineContentDescriptions = listOf(
            "Decline call", "Reject call", "End call", "Hang up call", "Cancel call",
            "decline call", "reject call", "end call", "hang up call", "cancel call",
            "Decline", "Reject", "End", "Hang up", "Cancel", "Dismiss",
            "decline", "reject", "end", "hang up", "cancel", "dismiss"
        )
        
        // Resource IDs that might be decline buttons (common patterns)
        val declineResourceIds = listOf(
            "decline", "reject", "end", "hangup", "cancel", "dismiss",
            "btn_decline", "btn_reject", "btn_end", "btn_hangup", "btn_cancel",
            "button_decline", "button_reject", "button_end", "button_hangup",
            "call_decline", "call_reject", "call_end", "call_hangup"
        )
        
        fun searchNodes(node: AccessibilityNodeInfo, depth: Int = 0) {
            if (node == null) return
            
            val indent = "  ".repeat(depth)
            val text = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            val resourceId = node.viewIdResourceName ?: ""
            val className = node.className?.toString() ?: ""
            
            Log.d(TAG, "${indent}Node: Class=$className, Text='$text', Desc='$contentDesc', ID='$resourceId', Clickable=${node.isClickable}")
            
            // Check if this node is clickable and has decline text
            if (node.isClickable) {
                val textLower = text.lowercase()
                val contentDescLower = contentDesc.lowercase()
                val resourceIdLower = resourceId.lowercase()
                
                val isDeclineByText = textLower.isNotEmpty() && declineTexts.any { textLower.contains(it.lowercase()) }
                val isDeclineByContentDesc = contentDescLower.isNotEmpty() && declineContentDescriptions.any { contentDescLower.contains(it.lowercase()) }
                val isDeclineByResourceId = resourceIdLower.isNotEmpty() && declineResourceIds.any { resourceIdLower.contains(it.lowercase()) }
                
                if (isDeclineByText || isDeclineByContentDesc || isDeclineByResourceId) {
                    Log.i(TAG, "${indent}*** FOUND DECLINE BUTTON ***")
                    Log.i(TAG, "${indent}Text: '$text'")
                    Log.i(TAG, "${indent}Content Description: '$contentDesc'")
                    Log.i(TAG, "${indent}Resource ID: '$resourceId'")
                    Log.i(TAG, "${indent}Class: '$className'")
                    declineButtons.add(node)
                }
            }
            
            // Search child nodes
            for (i in 0 until node.childCount) {
                searchNodes(node.getChild(i), depth + 1)
            }
        }
        
        searchNodes(rootNode)
        Log.i(TAG, "Total decline buttons found: ${declineButtons.size}")
        return declineButtons
    }

    private fun tryAlternativeDeclineMethods(rootNode: AccessibilityNodeInfo) {
        Log.i(TAG, "=== TRYING ALTERNATIVE DECLINE METHODS ===")
        
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
            
            // Log all clickable nodes for debugging
            for (i in clickableNodes.indices) {
                val node = clickableNodes[i]
                val text = node.text?.toString() ?: ""
                val contentDesc = node.contentDescription?.toString() ?: ""
                val resourceId = node.viewIdResourceName ?: ""
                val className = node.className?.toString() ?: ""
                
                Log.d(TAG, "Clickable node $i: Class=$className, Text='$text', Desc='$contentDesc', ID='$resourceId'")
            }
            
            // Try clicking nodes that might be decline buttons
            var success = false
            for (node in clickableNodes) {
                val text = node.text?.toString()?.lowercase() ?: ""
                val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
                val resourceId = node.viewIdResourceName?.lowercase() ?: ""
                
                // Look for any button that might be related to declining calls
                if (text.contains("call") || contentDesc.contains("call") ||
                    text.contains("phone") || contentDesc.contains("phone") ||
                    text.contains("end") || contentDesc.contains("end") ||
                    text.contains("hang") || contentDesc.contains("hang") ||
                    text.contains("reject") || contentDesc.contains("reject") ||
                    text.contains("decline") || contentDesc.contains("decline") ||
                    text.contains("cancel") || contentDesc.contains("cancel") ||
                    text.contains("dismiss") || contentDesc.contains("dismiss") ||
                    resourceId.contains("decline") || resourceId.contains("reject") ||
                    resourceId.contains("end") || resourceId.contains("hang") ||
                    resourceId.contains("cancel") || resourceId.contains("dismiss")) {
                    
                    Log.i(TAG, "Trying to click potential decline button:")
                    Log.i(TAG, "  Text: '$text'")
                    Log.i(TAG, "  Content Description: '$contentDesc'")
                    Log.i(TAG, "  Resource ID: '$resourceId'")
                    
                    try {
                        success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.i(TAG, "Click result: $success")
                        if (success) {
                            Log.i(TAG, "Alternative decline method succeeded!")
                            break
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Click failed: ${e.message}")
                    }
                }
            }
            
            if (!success) {
                Log.w(TAG, "All alternative decline methods failed")
                // Try pressing back button as last resort
                try {
                    Log.i(TAG, "Trying back button as last resort...")
                    performGlobalAction(GLOBAL_ACTION_BACK)
                } catch (e: Exception) {
                    Log.e(TAG, "Back button failed: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in alternative decline methods: ${e.message}")
            e.printStackTrace()
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