package com.tessera.launcher.data.preference

import android.content.Context
import android.content.SharedPreferences

enum class WidgetType(val displayName: String) {
    BATTERY("Bateria & Sinais"),
    CALENDAR("Data & Calendário"),
    MEDIA("Player de Mídia"),
    WEATHER("Clima & Temperatura")
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

        // Gestos
        private const val KEY_GESTURE_DOUBLE_TAP_ENABLED = "gesture_double_tap_enabled"
        private const val KEY_GESTURE_DOUBLE_TAP_ACTION = "gesture_double_tap_action"
        private const val KEY_GESTURE_HOLD_ENABLED = "gesture_hold_enabled"
        private const val KEY_GESTURE_HOLD_ACTION = "gesture_hold_action"
        private const val KEY_GESTURE_SWIPE_DOWN_ENABLED = "gesture_swipe_down_enabled"
        private const val KEY_GESTURE_SWIPE_DOWN_ACTION = "gesture_swipe_down_action"
        private const val KEY_GESTURE_SWIPE_UP_ENABLED = "gesture_swipe_up_enabled"
        private const val KEY_GESTURE_SWIPE_UP_ACTION = "gesture_swipe_up_action"
        private const val KEY_GESTURE_SWIPE_LEFT_ENABLED = "gesture_swipe_left_enabled"
        private const val KEY_GESTURE_SWIPE_LEFT_ACTION = "gesture_swipe_left_action"
        private const val KEY_GESTURE_SWIPE_RIGHT_ENABLED = "gesture_swipe_right_enabled"
        private const val KEY_GESTURE_SWIPE_RIGHT_ACTION = "gesture_swipe_right_action"

        // Busca em Apps & Arquivos
        private const val KEY_IN_APP_SEARCH_PACKAGES = "in_app_search_packages"
        private const val KEY_SEARCH_FILES = "search_files"

        // Apps Ocultos & PIN
        private const val KEY_HIDDEN_APPS_PIN = "hidden_apps_pin"
        private const val KEY_HIDDEN_APPS_PACKAGES = "hidden_apps_packages"

        // Ícones Customizados Individuais
        private const val KEY_CUSTOM_APP_ICONS = "custom_app_icons"

        // Customização Avançada
        private const val KEY_SEARCH_BAR_STYLE = "search_bar_style"
        private const val KEY_SEARCH_BAR_TEXT_TYPE = "search_bar_text_type"
        private const val KEY_SEARCH_BAR_CUSTOM_TEXT = "search_bar_custom_text"
        private const val KEY_FONT_FAMILY_TYPE = "font_family_type"
        private const val KEY_CUSTOM_FONT_PATH = "custom_font_path"
        private const val KEY_SYSTEM_WALLPAPER_ENABLED = "system_wallpaper_enabled"
        private const val KEY_SOLID_WALLPAPER_COLOR = "solid_wallpaper_color"
        private const val KEY_SOLID_WALLPAPER_TARGET = "solid_wallpaper_target"
        private const val KEY_THEMED_ICONS_ENABLED = "themed_icons_enabled"
        private const val KEY_HIDE_APP_LABELS_ENABLED = "hide_app_labels_enabled"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"

        // Configurações de Widgets
        private const val KEY_BATTERY_WIDGET_STYLE = "battery_widget_style"
        private const val KEY_MEDIA_WIDGET_STYLE = "media_widget_style"
        private const val KEY_NOTES_WIDGET_FILTER = "notes_widget_filter"
        private const val KEY_CALENDAR_HOW_FAR_AHEAD = "calendar_how_far_ahead"
        private const val KEY_CALENDAR_HIDE_FINISHED = "calendar_hide_finished"
        private const val KEY_CALENDAR_IS_24H = "calendar_is_24h"
        private const val KEY_WEATHER_IS_CELSIUS = "weather_is_celsius"
        private const val KEY_CACHED_WEATHER_JSON = "cached_weather_json"
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

    // Gestos
    fun isDoubleTapEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_DOUBLE_TAP_ENABLED, true)
    fun setDoubleTapEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_DOUBLE_TAP_ENABLED, enabled).apply()

    fun getDoubleTapAction(): String = prefs.getString(KEY_GESTURE_DOUBLE_TAP_ACTION, "lock_screen") ?: "lock_screen"
    fun setDoubleTapAction(action: String) = prefs.edit().putString(KEY_GESTURE_DOUBLE_TAP_ACTION, action).apply()

    fun isHoldEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_HOLD_ENABLED, false)
    fun setHoldEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_HOLD_ENABLED, enabled).apply()

    fun getHoldAction(): String = prefs.getString(KEY_GESTURE_HOLD_ACTION, "launcher_settings") ?: "launcher_settings"
    fun setHoldAction(action: String) = prefs.edit().putString(KEY_GESTURE_HOLD_ACTION, action).apply()

    fun isSwipeDownEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_SWIPE_DOWN_ENABLED, true)
    fun setSwipeDownEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_SWIPE_DOWN_ENABLED, enabled).apply()

    fun getSwipeDownAction(): String = prefs.getString(KEY_GESTURE_SWIPE_DOWN_ACTION, "notifications") ?: "notifications"
    fun setSwipeDownAction(action: String) = prefs.edit().putString(KEY_GESTURE_SWIPE_DOWN_ACTION, action).apply()

    fun isSwipeUpEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_SWIPE_UP_ENABLED, true)
    fun setSwipeUpEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_SWIPE_UP_ENABLED, enabled).apply()

    fun getSwipeUpAction(): String = prefs.getString(KEY_GESTURE_SWIPE_UP_ACTION, "open_keyboard") ?: "open_keyboard"
    fun setSwipeUpAction(action: String) = prefs.edit().putString(KEY_GESTURE_SWIPE_UP_ACTION, action).apply()

    fun isSwipeLeftEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_SWIPE_LEFT_ENABLED, false)
    fun setSwipeLeftEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_SWIPE_LEFT_ENABLED, enabled).apply()

    fun getSwipeLeftAction(): String = prefs.getString(KEY_GESTURE_SWIPE_LEFT_ACTION, "launcher_settings") ?: "launcher_settings"
    fun setSwipeLeftAction(action: String) = prefs.edit().putString(KEY_GESTURE_SWIPE_LEFT_ACTION, action).apply()

    fun isSwipeRightEnabled(): Boolean = prefs.getBoolean(KEY_GESTURE_SWIPE_RIGHT_ENABLED, false)
    fun setSwipeRightEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_GESTURE_SWIPE_RIGHT_ENABLED, enabled).apply()

    fun getSwipeRightAction(): String = prefs.getString(KEY_GESTURE_SWIPE_RIGHT_ACTION, "system_settings") ?: "system_settings"
    fun setSwipeRightAction(action: String) = prefs.edit().putString(KEY_GESTURE_SWIPE_RIGHT_ACTION, action).apply()

    // Busca em Apps & Arquivos
    fun getInAppSearchPackages(): Set<String> {
        val defaultSet = setOf("com.google.android.youtube", "com.android.vending", "com.spotify.music", "com.google.android.apps.maps")
        return prefs.getStringSet(KEY_IN_APP_SEARCH_PACKAGES, defaultSet) ?: defaultSet
    }
    fun setInAppSearchPackages(pkgs: Set<String>) = prefs.edit().putStringSet(KEY_IN_APP_SEARCH_PACKAGES, pkgs).apply()

    fun isFilesSearchEnabled(): Boolean = prefs.getBoolean(KEY_SEARCH_FILES, true)
    fun setFilesSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SEARCH_FILES, enabled).apply()

    // Apps Ocultos & PIN
    fun getHiddenAppsPin(): String = prefs.getString(KEY_HIDDEN_APPS_PIN, "") ?: ""
    fun setHiddenAppsPin(pin: String) = prefs.edit().putString(KEY_HIDDEN_APPS_PIN, pin).apply()

    fun getHiddenAppsPackages(): Set<String> = prefs.getStringSet(KEY_HIDDEN_APPS_PACKAGES, emptySet()) ?: emptySet()
    fun setHiddenAppsPackages(pkgs: Set<String>) = prefs.edit().putStringSet(KEY_HIDDEN_APPS_PACKAGES, pkgs).apply()

    // Ícones Customizados Individuais
    fun getCustomAppIconsRaw(): String = prefs.getString(KEY_CUSTOM_APP_ICONS, "") ?: ""
    fun setCustomAppIconsRaw(raw: String) = prefs.edit().putString(KEY_CUSTOM_APP_ICONS, raw).apply()

    // Customização Avançada
    fun getSearchBarStyle(): String = prefs.getString(KEY_SEARCH_BAR_STYLE, "split_pill") ?: "split_pill"
    fun setSearchBarStyle(style: String) = prefs.edit().putString(KEY_SEARCH_BAR_STYLE, style).apply()

    fun getSearchBarTextType(): String = prefs.getString(KEY_SEARCH_BAR_TEXT_TYPE, "app_name") ?: "app_name"
    fun setSearchBarTextType(type: String) = prefs.edit().putString(KEY_SEARCH_BAR_TEXT_TYPE, type).apply()

    fun getSearchBarCustomText(): String = prefs.getString(KEY_SEARCH_BAR_CUSTOM_TEXT, "Searcho...") ?: "Searcho..."
    fun setSearchBarCustomText(text: String) = prefs.edit().putString(KEY_SEARCH_BAR_CUSTOM_TEXT, text).apply()

    fun getFontFamilyType(): String = prefs.getString(KEY_FONT_FAMILY_TYPE, "searcho") ?: "searcho"
    fun setFontFamilyType(type: String) = prefs.edit().putString(KEY_FONT_FAMILY_TYPE, type).apply()

    fun getCustomFontPath(): String = prefs.getString(KEY_CUSTOM_FONT_PATH, "") ?: ""
    fun setCustomFontPath(path: String) = prefs.edit().putString(KEY_CUSTOM_FONT_PATH, path).apply()

    fun isSystemWallpaperEnabled(): Boolean = prefs.getBoolean(KEY_SYSTEM_WALLPAPER_ENABLED, false)
    fun setSystemWallpaperEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SYSTEM_WALLPAPER_ENABLED, enabled).apply()

    fun getSolidWallpaperColor(): String = prefs.getString(KEY_SOLID_WALLPAPER_COLOR, "#000000") ?: "#000000"
    fun setSolidWallpaperColor(hex: String) = prefs.edit().putString(KEY_SOLID_WALLPAPER_COLOR, hex).apply()

    fun getSolidWallpaperTarget(): String = prefs.getString(KEY_SOLID_WALLPAPER_TARGET, "both") ?: "both"
    fun setSolidWallpaperTarget(target: String) = prefs.edit().putString(KEY_SOLID_WALLPAPER_TARGET, target).apply()

    fun isThemedIconsEnabled(): Boolean = prefs.getBoolean(KEY_THEMED_ICONS_ENABLED, false)
    fun setThemedIconsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_THEMED_ICONS_ENABLED, enabled).apply()

    fun isHideAppLabelsEnabled(): Boolean = prefs.getBoolean(KEY_HIDE_APP_LABELS_ENABLED, false)
    fun setHideAppLabelsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HIDE_APP_LABELS_ENABLED, enabled).apply()

    fun getSelectedLanguage(): String = prefs.getString(KEY_SELECTED_LANGUAGE, "PT") ?: "PT"
    fun setSelectedLanguage(lang: String) = prefs.edit().putString(KEY_SELECTED_LANGUAGE, lang).apply()

    fun getBatteryWidgetStyle(): String = prefs.getString(KEY_BATTERY_WIDGET_STYLE, "cards_3") ?: "cards_3"
    fun setBatteryWidgetStyle(style: String) = prefs.edit().putString(KEY_BATTERY_WIDGET_STYLE, style).apply()

    fun getMediaWidgetStyle(): String = prefs.getString(KEY_MEDIA_WIDGET_STYLE, "classic") ?: "classic"
    fun setMediaWidgetStyle(style: String) = prefs.edit().putString(KEY_MEDIA_WIDGET_STYLE, style).apply()

    fun getNotesWidgetFilter(): String = prefs.getString(KEY_NOTES_WIDGET_FILTER, "all") ?: "all"
    fun setNotesWidgetFilter(filter: String) = prefs.edit().putString(KEY_NOTES_WIDGET_FILTER, filter).apply()

    fun getCalendarHowFarAhead(): Int = prefs.getInt(KEY_CALENDAR_HOW_FAR_AHEAD, 7)
    fun setCalendarHowFarAhead(days: Int) = prefs.edit().putInt(KEY_CALENDAR_HOW_FAR_AHEAD, days).apply()

    fun isCalendarHideFinished(): Boolean = prefs.getBoolean(KEY_CALENDAR_HIDE_FINISHED, true)
    fun setCalendarHideFinished(hide: Boolean) = prefs.edit().putBoolean(KEY_CALENDAR_HIDE_FINISHED, hide).apply()

    fun isCalendar24hFormat(): Boolean = prefs.getBoolean(KEY_CALENDAR_IS_24H, true)
    fun setCalendar24hFormat(is24h: Boolean) = prefs.edit().putBoolean(KEY_CALENDAR_IS_24H, is24h).apply()

    fun isWeatherCelsius(): Boolean = prefs.getBoolean(KEY_WEATHER_IS_CELSIUS, true)
    fun setWeatherCelsius(isCelsius: Boolean) = prefs.edit().putBoolean(KEY_WEATHER_IS_CELSIUS, isCelsius).apply()

    fun getCachedWeatherJson(): String? = prefs.getString(KEY_CACHED_WEATHER_JSON, null)
    fun setCachedWeatherJson(json: String?) = prefs.edit().putString(KEY_CACHED_WEATHER_JSON, json).apply()
}
