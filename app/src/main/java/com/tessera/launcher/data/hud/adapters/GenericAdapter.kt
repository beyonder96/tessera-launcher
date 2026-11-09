package com.tessera.launcher.data.hud.adapters

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.StatusBarNotification
import com.tessera.launcher.data.hud.HudNotificationSnapshot
import com.tessera.launcher.data.hud.HudSignalClassifier
import com.tessera.launcher.data.model.HudSource
import com.tessera.launcher.data.model.LiveActivityAction
import com.tessera.launcher.data.model.LiveActivityInfo
import com.tessera.launcher.data.model.LiveActivityType

class GenericAdapter : LiveActivityAdapter {

    override val type: LiveActivityType = LiveActivityType.OTHER

    override fun matches(snapshot: HudNotificationSnapshot): Boolean = true // Fallback geral

    override fun parse(
        snapshot: HudNotificationSnapshot,
        sbn: StatusBarNotification,
        context: Context
    ): LiveActivityInfo {
        val notif = sbn.notification
        val actions = notif?.actions?.mapNotNull { act ->
            val actTitle = act.title?.toString()?.trim() ?: return@mapNotNull null
            LiveActivityAction(actTitle, act.actionIntent)
        } ?: emptyList()

        val isFitness = snapshot.packageName in HudSignalClassifier.FITNESS_PACKAGES ||
                snapshot.category == Notification.CATEGORY_WORKOUT

        val inferredType = when {
            isFitness -> LiveActivityType.FITNESS
            snapshot.progress != null -> LiveActivityType.PROGRESS
            else -> LiveActivityType.OTHER
        }

        val artworkBitmap = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notif?.getLargeIcon()?.loadDrawable(context)?.let { drawableToBitmap(it) }
            } else {
                @Suppress("DEPRECATION")
                notif?.largeIcon
            }
        }.getOrNull()

        val criticalText = when {
            snapshot.progress != null -> "${snapshot.progress}%"
            snapshot.text.isNotBlank() -> snapshot.text
            else -> snapshot.appName
        }

        return LiveActivityInfo(
            id = sbn.key ?: "${snapshot.packageName}_${sbn.id}",
            packageName = snapshot.packageName,
            type = inferredType,
            title = snapshot.title.ifBlank { snapshot.appName },
            subtitle = snapshot.text.ifBlank { snapshot.subText },
            extraInfo = snapshot.subText.takeIf { it.isNotBlank() },
            progress = snapshot.progress,
            chronometerBaseMs = snapshot.chronometerBaseMs,
            isChronometerCountDown = snapshot.isChronometerCountDown,
            artwork = artworkBitmap,
            priorityScore = if (isFitness) 60 else if (snapshot.progress != null) 55 else 30,
            timestamp = snapshot.postTime,
            appName = snapshot.appName,
            brandColor = snapshot.color,
            criticalText = criticalText,
            source = HudSource.HEURISTIC,
            actions = actions
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(1, 128) else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(1, 128) else 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
