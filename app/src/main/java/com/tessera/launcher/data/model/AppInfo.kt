package com.tessera.launcher.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?,
    val firstLetter: Char = computeFirstLetter(label)
) {
    companion object {
        fun computeFirstLetter(label: String): Char {
            val trimmed = label.trim()
            if (trimmed.isEmpty()) return '#'
            val first = trimmed.first().uppercaseChar()
            return if (first in 'A'..'Z') first else '#'
        }
    }
}
