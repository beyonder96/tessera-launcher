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
        private const val KEY_PHOTO_WIDGET_URI = "photo_widget_uri"
        private const val KEY_PHOTO_WIDGET_ENABLED = "photo_widget_enabled"
        private const val KEY_LIQUID_GLASS_MODE = "liquid_glass_mode"
        private const val KEY_AUTO_OPEN_KEYBOARD = "auto_open_keyboard"
        private const val KEY_AUTO_LAUNCH_APP = "auto_launch_app"
        private const val KEY_COLLAPSE_DOCK = "collapse_dock"
        private const val KEY_EXACT_APP_SEARCH = "exact_app_search"
        private const val KEY_SHOW_APP_SHORTCUTS = "show_app_shortcuts"
        private const val KEY_WEB_SEARCH_ENABLED = "web_search_enabled"
        private const val KEY_SEARCH_CONTACTS = "search_contacts"
        private const val KEY_SEARCH_MESSAGES = "search_messages"
        private const val KEY_CALCULATOR_CARD = "calculator_card"
        private const val KEY_WIDGET_DINO = "widget_dino"
        private const val KEY_WIDGET_NOTES = "widget_notes"
        private const val KEY_SWITCH_ON_MUSIC_PLAY = "switch_on_music_play"
        private const val KEY_DEFAULT_WIDGET_CARD_INDEX = "default_widget_card_index"
        private const val KEY_SHOW_STATUS_BAR = "show_status_bar"
    }

    fun getPhotoWidgetUri(): String? {
        return prefs.getString(KEY_PHOTO_WIDGET_URI, null)
    }

    fun setPhotoWidgetUri(uri: String?) {
        prefs.edit().putString(KEY_PHOTO_WIDGET_URI, uri).apply()
    }

    fun isPhotoWidgetEnabled(): Boolean {
        return prefs.getBoolean(KEY_PHOTO_WIDGET_ENABLED, true)
    }

    fun setPhotoWidgetEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PHOTO_WIDGET_ENABLED, enabled).apply()
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

    fun isLiquidGlassEnabled(): Boolean {
        return prefs.getBoolean(KEY_LIQUID_GLASS_MODE, true)
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LIQUID_GLASS_MODE, enabled).apply()
    }

    fun isAutoOpenKeyboard(): Boolean = prefs.getBoolean(KEY_AUTO_OPEN_KEYBOARD, true)
    fun setAutoOpenKeyboard(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_OPEN_KEYBOARD, enabled).apply()

    fun isAutoLaunchEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_LAUNCH_APP, true)
    fun setAutoLaunchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_LAUNCH_APP, enabled).apply()

    fun isCollapseDockEnabled(): Boolean = prefs.getBoolean(KEY_COLLAPSE_DOCK, true)
    fun setCollapseDockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_COLLAPSE_DOCK, enabled).apply()

    fun isExactSearchEnabled(): Boolean = prefs.getBoolean(KEY_EXACT_APP_SEARCH, false)
    fun setExactSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_EXACT_APP_SEARCH, enabled).apply()

    fun isAppShortcutsEnabled(): Boolean = prefs.getBoolean(KEY_SHOW_APP_SHORTCUTS, true)
    fun setAppShortcutsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SHOW_APP_SHORTCUTS, enabled).apply()

    fun isWebSearchEnabled(): Boolean = prefs.getBoolean(KEY_WEB_SEARCH_ENABLED, true)
    fun setWebSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_WEB_SEARCH_ENABLED, enabled).apply()

    fun isContactsSearchEnabled(): Boolean = prefs.getBoolean(KEY_SEARCH_CONTACTS, false)
    fun setContactsSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SEARCH_CONTACTS, enabled).apply()

    fun isMessagesSearchEnabled(): Boolean = prefs.getBoolean(KEY_SEARCH_MESSAGES, false)
    fun setMessagesSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SEARCH_MESSAGES, enabled).apply()

    fun isCalculatorCardEnabled(): Boolean = prefs.getBoolean(KEY_CALCULATOR_CARD, true)
    fun setCalculatorCardEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_CALCULATOR_CARD, enabled).apply()

    fun isDinoWidgetEnabled(): Boolean = prefs.getBoolean(KEY_WIDGET_DINO, false)
    fun setDinoWidgetEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_WIDGET_DINO, enabled).apply()

    fun isNotesWidgetEnabled(): Boolean = prefs.getBoolean(KEY_WIDGET_NOTES, false)
    fun setNotesWidgetEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_WIDGET_NOTES, enabled).apply()

    fun isSwitchOnMusicPlayEnabled(): Boolean = prefs.getBoolean(KEY_SWITCH_ON_MUSIC_PLAY, true)
    fun setSwitchOnMusicPlayEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SWITCH_ON_MUSIC_PLAY, enabled).apply()

    fun getDefaultWidgetCardIndex(): Int = prefs.getInt(KEY_DEFAULT_WIDGET_CARD_INDEX, 0)
    fun setDefaultWidgetCardIndex(index: Int) = prefs.edit().putInt(KEY_DEFAULT_WIDGET_CARD_INDEX, index).apply()

    fun getNotesRaw(): String = prefs.getString("notes_data", "Comprar café:::false|||Planejar projeto:::true") ?: ""
    fun setNotesRaw(raw: String) = prefs.edit().putString("notes_data", raw).apply()

    fun getIconShape(): String = prefs.getString("icon_shape", "DEFAULT") ?: "DEFAULT"
    fun setIconShape(shape: String) = prefs.edit().putString("icon_shape", shape).apply()

    fun getSelectedIconPack(): String? = prefs.getString("selected_icon_pack", null)
    fun setSelectedIconPack(pkg: String?) = prefs.edit().putString("selected_icon_pack", pkg).apply()

    fun isShowStatusBarEnabled(): Boolean = prefs.getBoolean(KEY_SHOW_STATUS_BAR, true)
    fun setShowStatusBarEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SHOW_STATUS_BAR, enabled).apply()

    fun getSearchoActivationSymbol(): String = prefs.getString("searcho_symbol", "@") ?: "@"
    fun setSearchoActivationSymbol(symbol: String) = prefs.edit().putString("searcho_symbol", symbol).apply()

    fun getFoldersRaw(): String = prefs.getString("folders_data", "") ?: ""
    fun setFoldersRaw(raw: String) = prefs.edit().putString("folders_data", raw).apply()

    fun getSearchosRaw(): String = prefs.getString("searchos_data", "") ?: ""
    fun setSearchosRaw(raw: String) = prefs.edit().putString("searchos_data", raw).apply()
}
