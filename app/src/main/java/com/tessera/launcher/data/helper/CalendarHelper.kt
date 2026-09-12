package com.tessera.launcher.data.helper

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalendarEventInfo(
    val title: String,
    val timeFormatted: String,
    val isAllDay: Boolean = false,
    val beginTimestamp: Long = 0L,
    val endTimestamp: Long = 0L
) {
    val minutesUntil: Long
        get() = ((beginTimestamp - System.currentTimeMillis()) / 60_000).coerceAtLeast(0)

    val isOngoing: Boolean
        get() {
            val now = System.currentTimeMillis()
            return !isAllDay && beginTimestamp <= now && (endTimestamp > now || (beginTimestamp + 3600_000) > now)
        }
}

data class CalendarConflictInfo(
    val eventCount: Int,
    val timeFormatted: String
)

class CalendarHelper(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getUpcomingEvents(limit: Int = 5): List<CalendarEventInfo> {
        if (!hasCalendarPermission()) return emptyList()

        val now = System.currentTimeMillis()
        val endOfDay = now + 24 * 60 * 60 * 1000L

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, endOfDay)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )

        val events = mutableListOf<CalendarEventInfo>()
        try {
            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                val titleIdx = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginIdx = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = it.getColumnIndex(CalendarContract.Instances.END)
                val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                while (it.moveToNext() && events.size < limit) {
                    val title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Compromisso" else "Compromisso"
                    val begin = if (beginIdx >= 0) it.getLong(beginIdx) else now
                    val end = if (endIdx >= 0) it.getLong(endIdx) else (begin + 3600_000L)
                    val isAllDay = if (allDayIdx >= 0) it.getInt(allDayIdx) == 1 else false

                    val timeFormatted = if (isAllDay) {
                        "O dia todo"
                    } else {
                        timeFormat.format(Date(begin))
                    }

                    events.add(CalendarEventInfo(title, timeFormatted, isAllDay, begin, end))
                }
            }
        } catch (_: Exception) {}

        return events
    }

    fun getNextUpcomingEvent(): CalendarEventInfo? {
        return getUpcomingEvents(1).firstOrNull()
    }

    /**
     * Detecta se existem 2 ou mais eventos sobrepostos no mesmo horário hoje.
     */
    fun detectConflicts(): CalendarConflictInfo? {
        val events = getUpcomingEvents(10).filter { !it.isAllDay }
        if (events.size < 2) return null

        for (i in 0 until events.size - 1) {
            val current = events[i]
            for (j in i + 1 until events.size) {
                val other = events[j]
                // Conflito se o início do outro evento for antes do término do atual
                if (other.beginTimestamp < current.endTimestamp && other.endTimestamp > current.beginTimestamp) {
                    return CalendarConflictInfo(2, current.timeFormatted)
                }
            }
        }
        return null
    }

    fun openCalendar(preferredPackage: String? = null) {
        if (!preferredPackage.isNullOrBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(preferredPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }
        }

        // Fallback: Tenta abrir Google Agenda ou Intent genérico de calendário
        val calendarIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(calendarIntent)
        } catch (_: Exception) {
            val genericIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(genericIntent) }
        }
    }
}
