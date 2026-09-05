package com.tessera.launcher.data.preference

import android.content.Context
import android.content.SharedPreferences

enum class WidgetType(val displayName: String) {
    BATTERY("Bateria & Sinais"),
    CALENDAR("Data & Calendário"),
    MEDIA("Player de Mídia")
}

class LauncherPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tessera_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENABLED_WIDGETS = "enabled_widgets"
        private const val KEY_DEFAULT_MUSIC_PKG = "default_music_package"
        private const val KEY_DEFAULT_CALENDAR_PKG = "default_calendar_package"
        private const val KEY_HOSTED_WIDGET_IDS = "hosted_widget_ids"
        private const val KEY_AMOLED_MODE = "amoled_mode"
    }

    fun getEnabledWidgets(): List<WidgetType> {
        val saved = prefs.getString(KEY_ENABLED_WIDGETS, null)
        if (saved.isNullOrBlank()) {
            return listOf(WidgetType.CALENDAR, WidgetType.BATTERY, WidgetType.MEDIA)
        }
        return saved.split(",")
            .mapNotNull { name -> runCatching { WidgetType.valueOf(name) }.getOrNull() }
            .ifEmpty { listOf(WidgetType.CALENDAR, WidgetType.BATTERY, WidgetType.MEDIA) }
    }

    fun setEnabledWidgets(widgets: List<WidgetType>) {
        prefs.edit()
            .putString(KEY_ENABLED_WIDGETS, widgets.joinToString(",") { it.name })
            .apply()
    }

    fun getDefaultMusicPackage(): String? {
        return prefs.getString(KEY_DEFAULT_MUSIC_PKG, null)
    }

    fun setDefaultMusicPackage(pkg: String?) {
        prefs.edit().putString(KEY_DEFAULT_MUSIC_PKG, pkg).apply()
    }

    fun getDefaultCalendarPackage(): String? {
        return prefs.getString(KEY_DEFAULT_CALENDAR_PKG, null)
    }

    fun setDefaultCalendarPackage(pkg: String?) {
        prefs.edit().putString(KEY_DEFAULT_CALENDAR_PKG, pkg).apply()
    }

    fun getHostedWidgetIds(): List<Int> {
        val saved = prefs.getString(KEY_HOSTED_WIDGET_IDS, null) ?: return emptyList()
        return saved.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun addHostedWidgetId(id: Int) {
        val current = getHostedWidgetIds().toMutableList()
        if (!current.contains(id)) {
            current.add(id)
            prefs.edit().putString(KEY_HOSTED_WIDGET_IDS, current.joinToString(",")).apply()
        }
    }

    fun removeHostedWidgetId(id: Int) {
        val current = getHostedWidgetIds().toMutableList()
        if (current.remove(id)) {
            prefs.edit().putString(KEY_HOSTED_WIDGET_IDS, current.joinToString(",")).apply()
        }
    }

    fun isAmoledMode(): Boolean {
        return prefs.getBoolean(KEY_AMOLED_MODE, true)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
    }
}
