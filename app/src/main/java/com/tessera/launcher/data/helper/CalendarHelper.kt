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
    val isAllDay: Boolean = false
)

class CalendarHelper(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getNextUpcomingEvent(): CalendarEventInfo? {
        if (!hasCalendarPermission()) return null

        val now = System.currentTimeMillis()
        val endOfDay = now + 24 * 60 * 60 * 1000L

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, endOfDay)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.ALL_DAY
        )

        return try {
            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val titleIdx = it.getColumnIndex(CalendarContract.Instances.TITLE)
                    val beginIdx = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                    val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                    val title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Compromisso" else "Compromisso"
                    val begin = if (beginIdx >= 0) it.getLong(beginIdx) else now
                    val isAllDay = if (allDayIdx >= 0) it.getInt(allDayIdx) == 1 else false

                    val timeFormatted = if (isAllDay) {
                        "O dia todo"
                    } else {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        timeFormat.format(Date(begin))
                    }

                    CalendarEventInfo(title, timeFormatted, isAllDay)
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
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
