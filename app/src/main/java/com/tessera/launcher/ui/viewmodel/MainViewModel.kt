package com.tessera.launcher.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.launcher.data.helper.CalendarHelper
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.LauncherPreferences
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.repository.AppRepository
import com.tessera.launcher.data.service.TesseraMediaService
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SettingsSubScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

class MainViewModel(
    private val appRepository: AppRepository,
    private val systemInfoHelper: SystemInfoHelper,
    private val calendarHelper: CalendarHelper,
    private val preferences: LauncherPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LauncherUiState(
            enabledWidgets = preferences.getEnabledWidgets(),
            defaultMusicApp = preferences.getDefaultMusicPackage(),
            defaultCalendarApp = preferences.getDefaultCalendarPackage(),
            photoWidgetUri = preferences.getPhotoWidgetUri(),
            isPhotoWidgetEnabled = preferences.isPhotoWidgetEnabled(),
            isAmoledMode = preferences.isAmoledMode(),
            isLiquidGlassEnabled = preferences.isLiquidGlassEnabled(),
            isAutoOpenKeyboard = preferences.isAutoOpenKeyboard(),
            isAutoLaunchEnabled = preferences.isAutoLaunchEnabled(),
            isCollapseDockEnabled = preferences.isCollapseDockEnabled(),
            isExactSearchEnabled = preferences.isExactSearchEnabled(),
            isAppShortcutsEnabled = preferences.isAppShortcutsEnabled(),
            isWebSearchEnabled = preferences.isWebSearchEnabled(),
            isContactsSearchEnabled = preferences.isContactsSearchEnabled(),
            isMessagesSearchEnabled = preferences.isMessagesSearchEnabled(),
            isCalculatorCardEnabled = preferences.isCalculatorCardEnabled(),
            isDinoWidgetEnabled = preferences.isDinoWidgetEnabled(),
            isNotesWidgetEnabled = preferences.isNotesWidgetEnabled(),
            isSwitchOnMusicPlayEnabled = preferences.isSwitchOnMusicPlayEnabled(),
            defaultWidgetCardIndex = preferences.getDefaultWidgetCardIndex()
        )
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()
    private var autoLaunchJob: Job? = null

    init {
        observeInstalledApps()
        observeSystemInfo()
        observeMediaService()
        refreshCalendarAndPermissions()
    }

    private fun observeInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(appsState = AppsListState.Loading) }
            appRepository.observeApps()
                .catch { error ->
                    _uiState.update {
                        it.copy(appsState = AppsListState.Error(error.localizedMessage ?: "Erro ao carregar aplicativos"))
                    }
                }
                .collect { apps ->
                    allApps = apps
                    // Auto-seleciona Spotify como app de música se instalado e nenhum outro configurado
                    if (preferences.getDefaultMusicPackage() == null) {
                        val hasSpotify = apps.any { it.packageName == "com.spotify.music" }
                        if (hasSpotify) {
                            setDefaultMusicApp("com.spotify.music")
                        }
                    }
                    applyFilter(_uiState.value.searchQuery)
                }
        }
    }

    private fun observeSystemInfo() {
        viewModelScope.launch {
            systemInfoHelper.observeBattery().collect { battery ->
                _uiState.update {
                    it.copy(
                        batteryPercentage = battery.percentage,
                        isCharging = battery.isCharging
                    )
                }
            }
        }

        viewModelScope.launch {
            systemInfoHelper.observeTime().collect { (time, date) ->
                _uiState.update {
                    it.copy(
                        formattedTime = time,
                        formattedDate = date
                    )
                }
            }
        }
    }

    private fun observeMediaService() {
        viewModelScope.launch {
            TesseraMediaService.mediaState.collect { mediaInfo ->
                _uiState.update { it.copy(mediaPlayback = mediaInfo) }
            }
        }
    }

    fun refreshCalendarAndPermissions() {
        val hasCalendar = calendarHelper.hasCalendarPermission()
        val nextEvent = if (hasCalendar) calendarHelper.getNextUpcomingEvent() else null
        _uiState.update {
            it.copy(
                hasCalendarPermission = hasCalendar,
                nextCalendarEvent = nextEvent
            )
        }
    }

    fun checkNotificationAccess(context: Context) {
        val hasAccess = TesseraMediaService.isNotificationAccessGranted(context)
        _uiState.update { it.copy(hasNotificationAccess = hasAccess) }
    }

    fun expandSearch() {
        _uiState.update {
            it.copy(
                isSearchExpanded = true,
                isWidgetExpanded = true
            )
        }
    }

    fun collapseSearch() {
        autoLaunchJob?.cancel()
        _uiState.update {
            it.copy(
                isSearchExpanded = false,
                isWidgetExpanded = false,
                isDrawerOpen = false,
                searchQuery = ""
            )
        }
        applyFilter("")
    }

    fun onSearchQueryChange(query: String) {
        val hasQuery = query.isNotBlank()
        _uiState.update {
            it.copy(
                searchQuery = query,
                isDrawerOpen = if (hasQuery) true else it.isDrawerOpen,
                isSearchExpanded = true,
                isWidgetExpanded = !hasQuery
            )
        }
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        autoLaunchJob?.cancel()

        val isExact = _uiState.value.isExactSearchEnabled
        val filtered = if (trimmed.isEmpty()) {
            allApps
        } else {
            val normalizedQuery = AppInfo.normalize(trimmed)
            if (isExact) {
                allApps.filter { app ->
                    app.normalizedLabel.equals(normalizedQuery, ignoreCase = true) ||
                            app.label.equals(trimmed, ignoreCase = true)
                }
            } else {
                allApps.filter { app ->
                    app.normalizedLabel.contains(normalizedQuery) ||
                            app.packageName.contains(trimmed, ignoreCase = true)
                }
            }
        }

        val letterMap = mutableMapOf<Char, Int>()
        val availableLetters = mutableListOf<Char>()
        filtered.forEachIndexed { index, app ->
            val letter = app.firstLetter
            if (!letterMap.containsKey(letter)) {
                letterMap[letter] = index
                availableLetters.add(letter)
            }
        }

        val appsState = when {
            allApps.isEmpty() -> AppsListState.Loading
            filtered.isEmpty() -> AppsListState.Empty(query = query)
            else -> AppsListState.Success(filtered)
        }

        _uiState.update {
            it.copy(
                appsState = appsState,
                filteredApps = filtered,
                letterIndexMap = letterMap,
                availableLetters = availableLetters
            )
        }

        // Auto-launch se habilitado e houver exatamente 1 aplicativo correspondente ao digitar
        if (_uiState.value.isAutoLaunchEnabled && trimmed.isNotEmpty() && filtered.size == 1) {
            val singleApp = filtered.first()
            autoLaunchJob = viewModelScope.launch {
                delay(150)
                launchApp(singleApp.packageName)
            }
        }
    }

    fun openDrawer() {
        _uiState.update {
            it.copy(
                isDrawerOpen = true,
                isSearchExpanded = true,
                isWidgetExpanded = false
            )
        }
    }

    fun closeDrawer() {
        _uiState.update {
            it.copy(
                isDrawerOpen = false,
                searchQuery = ""
            )
        }
        applyFilter("")
    }

    fun toggleDrawer() {
        if (_uiState.value.isDrawerOpen) {
            closeDrawer()
        } else {
            openDrawer()
        }
    }

    fun setWidgetsExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isWidgetExpanded = expanded) }
    }

    fun toggleWidgets() {
        _uiState.update {
            val willExpand = !it.isWidgetExpanded
            it.copy(
                isWidgetExpanded = willExpand,
                isDrawerOpen = if (willExpand) false else it.isDrawerOpen,
                searchQuery = if (willExpand) "" else it.searchQuery
            )
        }
    }

    // Gestão de Widgets da Barra
    fun toggleWidget(type: WidgetType) {
        val current = _uiState.value.enabledWidgets.toMutableList()
        if (current.contains(type)) {
            current.remove(type)
        } else {
            current.add(type)
        }
        preferences.setEnabledWidgets(current)
        _uiState.update { it.copy(enabledWidgets = current) }
    }

    fun moveWidgetUp(type: WidgetType) {
        val current = _uiState.value.enabledWidgets.toMutableList()
        val index = current.indexOf(type)
        if (index > 0) {
            current.removeAt(index)
            current.add(index - 1, type)
            preferences.setEnabledWidgets(current)
            _uiState.update { it.copy(enabledWidgets = current) }
        }
    }

    fun moveWidgetDown(type: WidgetType) {
        val current = _uiState.value.enabledWidgets.toMutableList()
        val index = current.indexOf(type)
        if (index >= 0 && index < current.size - 1) {
            current.removeAt(index)
            current.add(index + 1, type)
            preferences.setEnabledWidgets(current)
            _uiState.update { it.copy(enabledWidgets = current) }
        }
    }

    // Moldura de Foto
    fun setPhotoWidgetUri(uriString: String?) {
        preferences.setPhotoWidgetUri(uriString)
        _uiState.update { it.copy(photoWidgetUri = uriString) }
    }

    fun setPhotoWidgetEnabled(enabled: Boolean) {
        preferences.setPhotoWidgetEnabled(enabled)
        _uiState.update { it.copy(isPhotoWidgetEnabled = enabled) }
    }

    // Configurações da Launcher e Subtelas
    fun openSettings() {
        _uiState.update {
            it.copy(
                isSettingsOpen = true,
                currentSettingsScreen = SettingsSubScreen.MAIN
            )
        }
    }

    fun closeSettings() {
        _uiState.update {
            it.copy(
                isSettingsOpen = false,
                currentSettingsScreen = SettingsSubScreen.MAIN
            )
        }
    }

    fun navigateToSettingsSubScreen(screen: SettingsSubScreen) {
        _uiState.update { it.copy(currentSettingsScreen = screen) }
    }

    fun navigateBackSettings() {
        _uiState.update {
            when (it.currentSettingsScreen) {
                SettingsSubScreen.WIDGETS_CENTER -> it.copy(currentSettingsScreen = SettingsSubScreen.SEARCH)
                SettingsSubScreen.SEARCH -> it.copy(currentSettingsScreen = SettingsSubScreen.MAIN)
                SettingsSubScreen.MAIN -> it.copy(isSettingsOpen = false)
            }
        }
    }

    fun setAutoOpenKeyboard(enabled: Boolean) {
        preferences.setAutoOpenKeyboard(enabled)
        _uiState.update { it.copy(isAutoOpenKeyboard = enabled) }
    }

    fun setAutoLaunchEnabled(enabled: Boolean) {
        preferences.setAutoLaunchEnabled(enabled)
        _uiState.update { it.copy(isAutoLaunchEnabled = enabled) }
    }

    fun setCollapseDockEnabled(enabled: Boolean) {
        preferences.setCollapseDockEnabled(enabled)
        _uiState.update { it.copy(isCollapseDockEnabled = enabled) }
    }

    fun setExactSearchEnabled(enabled: Boolean) {
        preferences.setExactSearchEnabled(enabled)
        _uiState.update { it.copy(isExactSearchEnabled = enabled) }
        applyFilter(_uiState.value.searchQuery)
    }

    fun setAppShortcutsEnabled(enabled: Boolean) {
        preferences.setAppShortcutsEnabled(enabled)
        _uiState.update { it.copy(isAppShortcutsEnabled = enabled) }
    }

    fun setWebSearchEnabled(enabled: Boolean) {
        preferences.setWebSearchEnabled(enabled)
        _uiState.update { it.copy(isWebSearchEnabled = enabled) }
    }

    fun setContactsSearchEnabled(enabled: Boolean) {
        preferences.setContactsSearchEnabled(enabled)
        _uiState.update { it.copy(isContactsSearchEnabled = enabled) }
    }

    fun setMessagesSearchEnabled(enabled: Boolean) {
        preferences.setMessagesSearchEnabled(enabled)
        _uiState.update { it.copy(isMessagesSearchEnabled = enabled) }
    }

    fun setCalculatorCardEnabled(enabled: Boolean) {
        preferences.setCalculatorCardEnabled(enabled)
        _uiState.update { it.copy(isCalculatorCardEnabled = enabled) }
    }

    fun setDinoWidgetEnabled(enabled: Boolean) {
        preferences.setDinoWidgetEnabled(enabled)
        _uiState.update { it.copy(isDinoWidgetEnabled = enabled) }
    }

    fun setNotesWidgetEnabled(enabled: Boolean) {
        preferences.setNotesWidgetEnabled(enabled)
        _uiState.update { it.copy(isNotesWidgetEnabled = enabled) }
    }

    fun setSwitchOnMusicPlayEnabled(enabled: Boolean) {
        preferences.setSwitchOnMusicPlayEnabled(enabled)
        _uiState.update { it.copy(isSwitchOnMusicPlayEnabled = enabled) }
    }

    fun setDefaultWidgetCardIndex(index: Int) {
        preferences.setDefaultWidgetCardIndex(index)
        _uiState.update { it.copy(defaultWidgetCardIndex = index) }
    }

    fun setDefaultMusicApp(pkg: String?) {
        preferences.setDefaultMusicPackage(pkg)
        _uiState.update { it.copy(defaultMusicApp = pkg) }
    }

    fun setDefaultCalendarApp(pkg: String?) {
        preferences.setDefaultCalendarPackage(pkg)
        _uiState.update { it.copy(defaultCalendarApp = pkg) }
    }

    fun setAmoledMode(enabled: Boolean) {
        preferences.setAmoledMode(enabled)
        _uiState.update { it.copy(isAmoledMode = enabled) }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        preferences.setLiquidGlassEnabled(enabled)
        _uiState.update { it.copy(isLiquidGlassEnabled = enabled) }
    }

    // Controles de Mídia Reais
    fun togglePlayPauseMedia() {
        TesseraMediaService.togglePlayPause()
    }

    fun skipNextMedia() {
        TesseraMediaService.skipNext()
    }

    fun skipPreviousMedia() {
        TesseraMediaService.skipPrevious()
    }

    fun launchDefaultMusicApp() {
        val targetPkg = _uiState.value.defaultMusicApp ?: "com.spotify.music"
        val result = appRepository.launchApp(targetPkg)
        if (result.isFailure) {
            // Se o app padrão não abrir, tenta qualquer app de música disponível
            val musicApp = allApps.firstOrNull {
                TesseraMediaService.KNOWN_MUSIC_PACKAGES.contains(it.packageName)
            }
            if (musicApp != null) {
                appRepository.launchApp(musicApp.packageName)
            }
        }
    }

    fun launchCalendarApp() {
        calendarHelper.openCalendar(_uiState.value.defaultCalendarApp)
    }

    fun handleBackPress(): Boolean {
        val state = _uiState.value
        return when {
            state.isSettingsOpen -> {
                navigateBackSettings()
                true
            }
            state.searchQuery.isNotEmpty() -> {
                onSearchQueryChange("")
                true
            }
            state.isDrawerOpen -> {
                closeDrawer()
                true
            }
            state.isSearchExpanded -> {
                collapseSearch()
                true
            }
            else -> false // Home limpa, consome evento
        }
    }

    fun launchApp(packageName: String): Result<Unit> {
        val result = appRepository.launchApp(packageName)
        if (result.isSuccess) {
            collapseSearch()
        }
        return result
    }

    fun reloadApps() {
        observeInstalledApps()
    }

    private fun normalizeString(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }
}
