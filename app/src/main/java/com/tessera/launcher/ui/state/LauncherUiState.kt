package com.tessera.launcher.ui.state

import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.ContactInfo
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.service.MediaPlaybackInfo
import com.tessera.launcher.ui.components.NoteTask

sealed interface AppsListState {
    data object Loading : AppsListState
    data class Success(val apps: List<AppInfo>) : AppsListState
    data class Empty(val query: String) : AppsListState
    data class Error(val message: String) : AppsListState
}

enum class SettingsSubScreen {
    MAIN,
    SEARCH,
    WIDGETS_CENTER,
    EXTRAS,
    FOLDERS,
    SEARCHOS
}

data class AppFolder(
    val id: Long,
    val name: String,
    val packageNames: List<String> = emptyList()
)

data class SearchoItem(
    val id: String,
    val title: String,
    val prefix: String,
    val iconType: String
)

val DEFAULT_SEARCHOS_LIST = listOf(
    SearchoItem("tasks", "Tarefas", "@t", "tasks"),
    SearchoItem("notes", "Notas", "@n", "notes"),
    SearchoItem("quick_actions", "Ações rápidas", "@s", "quick_actions"),
    SearchoItem("send_message", "Enviar mensagem", "@msg", "send_message"),
    SearchoItem("keep", "Google Keep", "@kp", "keep"),
    SearchoItem("messages", "Mensagens", "@sms", "messages"),
    SearchoItem("whatsapp", "Mensagens do WhatsApp", "@wa", "whatsapp"),
    SearchoItem("telegram", "Mensagens do Telegram", "@tg", "telegram"),
    SearchoItem("notifications", "Notificações", "@ntf", "notifications"),
    SearchoItem("contacts", "Contatos", "@con", "contacts"),
    SearchoItem("activity", "Sua atividade", "@fd", "activity"),
    SearchoItem("obsidian", "Obsidian", "@ob", "obsidian")
)

data class LauncherUiState(
    val appsState: AppsListState = AppsListState.Loading,
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val letterIndexMap: Map<Char, Int> = emptyMap(),
    val availableLetters: List<Char> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val isWidgetExpanded: Boolean = false,
    val isSearchExpanded: Boolean = false,
    val batteryPercentage: Int = 100,
    val isCharging: Boolean = false,
    val formattedTime: String = "--:--",
    val formattedDate: String = "---",
    val mediaPlayback: MediaPlaybackInfo = MediaPlaybackInfo(),
    val hasNotificationAccess: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val nextCalendarEvent: CalendarEventInfo? = null,
    val enabledWidgets: List<WidgetType> = listOf(WidgetType.CALENDAR, WidgetType.BATTERY, WidgetType.MEDIA),
    val defaultMusicApp: String? = null,
    val defaultCalendarApp: String? = null,
    val photoWidgetUri: String? = null,
    val isPhotoWidgetEnabled: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val isAmoledMode: Boolean = true,
    val isLiquidGlassEnabled: Boolean = true,
    val currentSettingsScreen: SettingsSubScreen = SettingsSubScreen.MAIN,
    val isAutoOpenKeyboard: Boolean = true,
    val isAutoLaunchEnabled: Boolean = true,
    val isCollapseDockEnabled: Boolean = true,
    val isExactSearchEnabled: Boolean = false,
    val isAppShortcutsEnabled: Boolean = true,
    val isWebSearchEnabled: Boolean = true,
    val isContactsSearchEnabled: Boolean = false,
    val isMessagesSearchEnabled: Boolean = false,
    val isCalculatorCardEnabled: Boolean = true,
    val isDinoWidgetEnabled: Boolean = false,
    val isNotesWidgetEnabled: Boolean = false,
    val isSwitchOnMusicPlayEnabled: Boolean = true,
    val defaultWidgetCardIndex: Int = 0,
    val isTorchOn: Boolean = false,
    val ringerMode: Int = 2,
    val notesTasks: List<NoteTask> = emptyList(),
    val hasContactsPermission: Boolean = false,
    val matchingContacts: List<ContactInfo> = emptyList(),
    val calculatorResult: String? = null,
    val iconShape: String = "DEFAULT",
    val selectedIconPack: String? = null,
    val isShowStatusBarEnabled: Boolean = true,
    val searchoActivationSymbol: String = "@",
    val searchosList: List<SearchoItem> = DEFAULT_SEARCHOS_LIST,
    val appFolders: List<AppFolder> = emptyList()
)
