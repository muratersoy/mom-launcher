package com.momlauncher.data

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.text.format.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED,
    UNKNOWN
}

data class CallLogEntry(
    val id: String,
    val name: String,
    val number: String,
    val type: CallDirection,
    val formattedDate: String,
    val durationSeconds: Long,
    val timestamp: Long = 0L
)

class CallLogRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("call_log_prefs", Context.MODE_PRIVATE)

    fun dismissMissedCall(id: String) {
        if (id.isBlank()) return
        val set = prefs.getStringSet("dismissed_missed_calls", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(id)
        prefs.edit().putStringSet("dismissed_missed_calls", set).apply()
    }

    fun getDismissedMissedCallIds(): Set<String> {
        return prefs.getStringSet("dismissed_missed_calls", emptySet()) ?: emptySet()
    }

    suspend fun getRecentMissedCalls(limit: Int = 3): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val allLogs = getCallLogs(limit = 40)
        val dismissedIds = getDismissedMissedCallIds()
        allLogs
            .filter { it.type == CallDirection.MISSED && !dismissedIds.contains(it.id) }
            .take(limit)
    }

    suspend fun getCallLogs(limit: Int = 60): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntry>()

        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            val timeFormat = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
            val dateFormat = SimpleDateFormat("dd MMMM", Locale("tr", "TR"))

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getString(idIdx) ?: "" else ""
                    val cachedName = if (nameIdx != -1) it.getString(nameIdx) ?: "" else ""
                    val number = if (numIdx != -1) it.getString(numIdx) ?: "" else ""
                    val rawType = if (typeIdx != -1) it.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val timestamp = if (dateIdx != -1) it.getLong(dateIdx) else 0L
                    val duration = if (durIdx != -1) it.getLong(durIdx) else 0L

                    val direction = when (rawType) {
                        CallLog.Calls.INCOMING_TYPE -> CallDirection.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallDirection.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallDirection.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallDirection.MISSED
                        else -> CallDirection.UNKNOWN
                    }

                    val dateStr = formatCallDate(timestamp, timeFormat, dateFormat)

                    list.add(
                        CallLogEntry(
                            id = id,
                            name = cachedName.ifBlank { number },
                            number = number,
                            type = direction,
                            formattedDate = dateStr,
                            durationSeconds = duration,
                            timestamp = timestamp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        list
    }

    private fun formatCallDate(
        timestamp: Long,
        timeFormat: SimpleDateFormat,
        dateFormat: SimpleDateFormat
    ): String {
        if (timestamp <= 0) return ""
        return if (DateUtils.isToday(timestamp)) {
            "Bugün " + timeFormat.format(Date(timestamp))
        } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
            "Dün " + timeFormat.format(Date(timestamp))
        } else {
            dateFormat.format(Date(timestamp)) + " " + timeFormat.format(Date(timestamp))
        }
    }
}
