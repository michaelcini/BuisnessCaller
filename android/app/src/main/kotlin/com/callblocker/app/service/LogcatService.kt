package com.callblocker.app.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class LogcatService(private val context: Context) {
    companion object {
        const val TAG = "LogcatService"
        const val PREFS_NAME = "logcat_settings"
        const val MAX_LOG_ENTRIES = 10000
    }

    private var methodChannel: MethodChannel? = null
    private var logcatProcess: Process? = null
    private var isRunning = false
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val logEntries = mutableListOf<LogEntry>()
    private val logLock = Any()

    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String,
        val pid: Int,
        val tid: Int
    )

    fun setMethodChannel(channel: MethodChannel) {
        this.methodChannel = channel
    }

    fun startLogcat() {
        if (isRunning) {
            Log.w(TAG, "Logcat is already running")
            return
        }

        Log.i(TAG, "Starting logcat service...")
        isRunning = true

        try {
            // Start logcat process
            val processBuilder = ProcessBuilder(
                "logcat",
                "-v", "time",
                "-s", "*:V" // Verbose level for all tags
            )
            logcatProcess = processBuilder.start()

            // Start reading logcat output
            executor.submit {
                readLogcatOutput()
            }

            // Start periodic log sending
            executor.scheduleWithFixedDelay({
                sendLogsToFlutter()
            }, 1, 1, TimeUnit.SECONDS)

            Log.i(TAG, "Logcat service started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start logcat service: ${e.message}")
            e.printStackTrace()
            isRunning = false
        }
    }

    fun stopLogcat() {
        if (!isRunning) {
            Log.w(TAG, "Logcat is not running")
            return
        }

        Log.i(TAG, "Stopping logcat service...")
        isRunning = false

        try {
            logcatProcess?.destroy()
            logcatProcess = null
            executor.shutdown()
            Log.i(TAG, "Logcat service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping logcat service: ${e.message}")
        }
    }

    private fun readLogcatOutput() {
        try {
            val reader = BufferedReader(InputStreamReader(logcatProcess?.inputStream))
            var line: String?

            while (isRunning && reader.readLine().also { line = it } != null) {
                line?.let { logLine ->
                    parseAndStoreLogEntry(logLine)
                }
            }

            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading logcat output: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun parseAndStoreLogEntry(logLine: String) {
        try {
            // Parse logcat format: MM-DD HH:MM:SS.fff PID/TID LEVEL/TAG: MESSAGE
            val parts = logLine.split(" ", limit = 6)
            if (parts.size < 6) return

            val dateTime = parts[0] + " " + parts[1] // MM-DD HH:MM:SS.fff
            val pidTid = parts[2] // PID/TID
            val levelTag = parts[3] // LEVEL/TAG
            val message = parts[4] + if (parts.size > 5) " " + parts[5] else ""

            val pidTidParts = pidTid.split("/")
            val pid = pidTidParts[0].toIntOrNull() ?: 0
            val tid = pidTidParts.getOrNull(1)?.toIntOrNull() ?: 0

            val levelTagParts = levelTag.split("/")
            val level = levelTagParts[0]
            val tag = levelTagParts.getOrNull(1) ?: "Unknown"

            val logEntry = LogEntry(
                timestamp = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
                pid = pid,
                tid = tid
            )

            synchronized(logLock) {
                logEntries.add(logEntry)
                
                // Keep only the last MAX_LOG_ENTRIES
                if (logEntries.size > MAX_LOG_ENTRIES) {
                    logEntries.removeAt(0)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing log entry: ${e.message}")
        }
    }

    private fun sendLogsToFlutter() {
        if (!isRunning || methodChannel == null) return

        try {
            val logsToSend = synchronized(logLock) {
                val logs = logEntries.toList()
                logEntries.clear()
                logs
            }

            if (logsToSend.isNotEmpty()) {
                val logData = logsToSend.map { entry ->
                    mapOf(
                        "timestamp" to entry.timestamp,
                        "level" to entry.level,
                        "tag" to entry.tag,
                        "message" to entry.message,
                        "pid" to entry.pid,
                        "tid" to entry.tid
                    )
                }

                methodChannel?.invokeMethod("onLogcatData", mapOf(
                    "logs" to logData,
                    "count" to logsToSend.size
                ))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending logs to Flutter: ${e.message}")
        }
    }

    fun getLogs(filter: String = "", level: String = "", tag: String = ""): List<LogEntry> {
        return synchronized(logLock) {
            logEntries.filter { entry ->
                val matchesFilter = filter.isEmpty() || entry.message.contains(filter, ignoreCase = true)
                val matchesLevel = level.isEmpty() || entry.level.equals(level, ignoreCase = true)
                val matchesTag = tag.isEmpty() || entry.tag.contains(tag, ignoreCase = true)
                
                matchesFilter && matchesLevel && matchesTag
            }
        }
    }

    fun clearLogs() {
        synchronized(logLock) {
            logEntries.clear()
        }
    }

    fun getLogStats(): Map<String, Any> {
        return synchronized(logLock) {
            val levelCounts = logEntries.groupBy { it.level }.mapValues { it.value.size }
            val tagCounts = logEntries.groupBy { it.tag }.mapValues { it.value.size }
            
            mapOf(
                "totalLogs" to logEntries.size,
                "levelCounts" to levelCounts,
                "topTags" to tagCounts.toList().sortedByDescending { it.second }.take(10),
                "isRunning" to isRunning
            )
        }
    }
}