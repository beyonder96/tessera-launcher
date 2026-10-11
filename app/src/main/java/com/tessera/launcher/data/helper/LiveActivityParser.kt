package com.tessera.launcher.data.helper

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

/**
 * Parser inteligente de Ongoing Notifications do Android para o formato Live Activities do Tessera.
 * Identifica eventos de transporte (Uber/99), delivery (iFood), timer, navegação (Maps/Waze) e fitness (Strava).
 */
object LiveActivityParser {

    private val RIDE_PACKAGES = setOf(
        "com.ubercab",
        "com.taxis99",
        "com.didiglobal.passenger",
        "ee.mtakso.client",
        "me.lyft.android"
    )

    private val DELIVERY_PACKAGES = setOf(
        "br.com.brainweb.ifood",
        "com.rappi",
        "com.deliveryhero",
        "com.cerveceriamodelo.zedelivery",
        "com.ubercab.eats",
        "com.dd.doordash"
    )

    private val TIMER_PACKAGES = setOf(
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.coloros.alarmclock",
        "com.xiaomi.calendar"
    )

    private val NAVIGATION_PACKAGES = setOf(
        "com.google.android.apps.maps",
        "com.waze"
    )

    private val FITNESS_PACKAGES = setOf(
        "com.strava",
        "com.nike.plusgps"
    )

    private val CALL_PACKAGES = setOf(
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.whatsapp"
    )

    fun parse(sbn: StatusBarNotification, context: Context): LiveActivityInfo? {
        val notification = sbn.notification ?: return null
        val pkg = sbn.packageName

        // Filtro base: Notificação contínua ou com categoria de serviço em primeiro plano
        val isOngoing = sbn.isOngoing
        val category = notification.category

        val isEligibleCategory = category in listOf(
            Notification.CATEGORY_TRANSPORT,
            Notification.CATEGORY_NAVIGATION,
            Notification.CATEGORY_CALL,
            Notification.CATEGORY_STOPWATCH,
            Notification.CATEGORY_WORKOUT,
            Notification.CATEGORY_PROGRESS
        )

        val isKnownPackage = pkg in RIDE_PACKAGES ||
                pkg in DELIVERY_PACKAGES ||
                pkg in TIMER_PACKAGES ||
                pkg in NAVIGATION_PACKAGES ||
                pkg in FITNESS_PACKAGES ||
                (pkg in CALL_PACKAGES && (category == Notification.CATEGORY_CALL || isOngoing))

        if (!isOngoing && !isEligibleCategory && !isKnownPackage) {
            return null
        }

        // Ignora apps de música comuns pois estes já são tratados pelo MediaSession
        val isMusicApp = pkg in setOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.deezer.android.app",
            "com.apple.android.music",
            "com.amazon.mp3"
        )
        if (isMusicApp) return null

        val extras = notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim()
            ?: return null

        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()

        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()

        val type = when {
            pkg in RIDE_PACKAGES || category == Notification.CATEGORY_TRANSPORT -> LiveActivityType.RIDE
            pkg in DELIVERY_PACKAGES -> LiveActivityType.DELIVERY
            pkg in TIMER_PACKAGES || category == Notification.CATEGORY_STOPWATCH || extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) -> LiveActivityType.TIMER
            pkg in NAVIGATION_PACKAGES || category == Notification.CATEGORY_NAVIGATION -> LiveActivityType.NAVIGATION
            pkg in FITNESS_PACKAGES || category == Notification.CATEGORY_WORKOUT -> LiveActivityType.FITNESS
            pkg in CALL_PACKAGES || category == Notification.CATEGORY_CALL -> LiveActivityType.CALL
            else -> LiveActivityType.OTHER
        }

        val showChronometer = extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false)
        val chronometerBase = if (showChronometer) {
            val base = extras.getLong("android.chronometerBase", 0L)
            if (base > 0) base else notification.`when`
        } else null

        val isCountDown = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false)
        } else false

        val progress = run {
            val max = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
            val current = extras.getInt(Notification.EXTRA_PROGRESS, 0)
            if (max > 0) ((current.toFloat() / max) * 100).toInt().coerceIn(0, 100) else null
        }

        val artworkBitmap = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notification.largeIcon
            }
        }.getOrNull()

        val priorityScore = when (type) {
            LiveActivityType.CALL -> 100
            LiveActivityType.TIMER -> 90
            LiveActivityType.RIDE -> 80
            LiveActivityType.DELIVERY -> 75
            LiveActivityType.NAVIGATION -> 70
            LiveActivityType.FITNESS -> 60
            LiveActivityType.OTHER -> 30
        }

        return LiveActivityInfo(
            id = sbn.key,
            packageName = pkg,
            type = type,
            title = title,
            subtitle = text,
            extraInfo = subText,
            progress = progress,
            chronometerBaseMs = chronometerBase,
            isChronometerCountDown = isCountDown,
            artwork = artworkBitmap,
            priorityScore = priorityScore,
            timestamp = sbn.postTime
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
