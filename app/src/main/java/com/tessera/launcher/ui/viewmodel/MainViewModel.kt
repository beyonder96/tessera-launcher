package com.tessera.launcher.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.launcher.data.helper.CalendarHelper
import com.tessera.launcher.data.helper.ContactSearchHelper
import com.tessera.launcher.data.helper.QuickSettingsHelper
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.preference.LauncherPreferences
import com.tessera.launcher.data.preference.WidgetType
import com.tessera.launcher.data.repository.AppRepository
import com.tessera.launcher.data.service.TesseraMediaService
import com.tessera.launcher.data.service.TesseraAccessibilityService
import com.tessera.launcher.data.helper.IconPackInfo
import com.tessera.launcher.ui.components.MathEvaluator
import com.tessera.launcher.ui.components.NoteTask
import com.tessera.launcher.ui.state.AppFolder
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.DEFAULT_SEARCHOS_LIST
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SearchoItem
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.state.WidgetConfigType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

private fun parseNotes(raw: String): List<NoteTask> {
    if (raw.isBlank()) return emptyList()
    return raw.split("|||").mapIndexedNotNull { index, item ->
        val parts = item.split(":::")
        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
            val text = parts[0]
            val done = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: false
            NoteTask(id = index.toLong() + 1, text = text, isDone = done)
        } else null
    }
}

private fun serializeNotes(tasks: List<NoteTask>): String {
    return tasks.joinToString("|||") { "${it.text}:::${it.isDone}" }
}

private fun parseFolders(raw: String): List<AppFolder> {
    if (raw.isBlank()) return emptyList()
    return raw.split(";;;").mapIndexedNotNull { idx, item ->
        val parts = item.split(":::")
        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
            val name = parts[0]
            val pkgs = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].split(",") else emptyList()
            AppFolder(id = idx.toLong() + 1, name = name, packageNames = pkgs)
        } else null
    }
}

private fun serializeFolders(folders: List<AppFolder>): String {
    return folders.joinToString(";;;") { "${it.name}:::${it.packageNames.joinToString(",")}" }
}

private fun parseSearchos(raw: String): List<SearchoItem> {
    if (raw.isBlank()) return DEFAULT_SEARCHOS_LIST
    return raw.split(";;;").mapNotNull { item ->
        val parts = item.split(":::")
        if (parts.size >= 4) {
            SearchoItem(id = parts[0], title = parts[1], prefix = parts[2], iconType = parts[3])
        } else null
    }.ifEmpty { DEFAULT_SEARCHOS_LIST }
}

private fun serializeSearchos(items: List<SearchoItem>): String {
    return items.joinToString(";;;") { "${it.id}:::${it.title}:::${it.prefix}:::${it.iconType}" }
}

private fun parseCustomIcons(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    return raw.split("|||").mapNotNull { item ->
        val parts = item.split(":::")
        if (parts.size >= 2 && parts[0].isNotBlank()) {
            parts[0] to parts[1]
        } else null
    }.toMap()
}

private fun serializeCustomIcons(map: Map<String, String>): String {
    return map.entries.joinToString("|||") { "${it.key}:::${it.value}" }
}

class MainViewModel(
    private val appRepository: AppRepository,
    private val systemInfoHelper: SystemInfoHelper,
    private val calendarHelper: CalendarHelper,
    private val preferences: LauncherPreferences,
    private val quickSettingsHelper: QuickSettingsHelper,
    private val contactSearchHelper: ContactSearchHelper,
    private val fileSearchHelper: com.tessera.launcher.data.helper.FileSearchHelper,
    private val messageSearchHelper: com.tessera.launcher.data.helper.MessageSearchHelper,
    private val weatherHelper: com.tessera.launcher.data.helper.WeatherHelper
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
            defaultWidgetCardIndex = preferences.getDefaultWidgetCardIndex(),
            isTorchOn = quickSettingsHelper.isTorchOn.value,
            ringerMode = quickSettingsHelper.ringerMode.value,
            hasContactsPermission = contactSearchHelper.hasContactsPermission(),
            notesTasks = parseNotes(preferences.getNotesRaw()),
            iconShape = preferences.getIconShape(),
            selectedIconPack = preferences.getSelectedIconPack(),
            isShowStatusBarEnabled = preferences.isShowStatusBarEnabled(),
            searchoActivationSymbol = preferences.getSearchoActivationSymbol(),
            searchosList = parseSearchos(preferences.getSearchosRaw()),
            appFolders = parseFolders(preferences.getFoldersRaw()),

            // Gestos
            isDoubleTapEnabled = preferences.isDoubleTapEnabled(),
            doubleTapAction = preferences.getDoubleTapAction(),
            isHoldEnabled = preferences.isHoldEnabled(),
            holdAction = preferences.getHoldAction(),
            isSwipeDownEnabled = preferences.isSwipeDownEnabled(),
            swipeDownAction = preferences.getSwipeDownAction(),
            isSwipeUpEnabled = preferences.isSwipeUpEnabled(),
            swipeUpAction = preferences.getSwipeUpAction(),
            isSwipeLeftEnabled = preferences.isSwipeLeftEnabled(),
            swipeLeftAction = preferences.getSwipeLeftAction(),
            isSwipeRightEnabled = preferences.isSwipeRightEnabled(),
            swipeRightAction = preferences.getSwipeRightAction(),

            // Busca em Apps & Arquivos
            inAppSearchPackages = preferences.getInAppSearchPackages(),
            isFilesSearchEnabled = preferences.isFilesSearchEnabled(),

            // Apps Ocultos & PIN
            hiddenAppsPin = preferences.getHiddenAppsPin(),
            hiddenAppsPackages = preferences.getHiddenAppsPackages(),

            // Ícones Customizados
            customAppIcons = parseCustomIcons(preferences.getCustomAppIconsRaw()),

            // Customização Avançada
            searchBarStyle = preferences.getSearchBarStyle(),
            searchBarTextType = preferences.getSearchBarTextType(),
            searchBarCustomText = preferences.getSearchBarCustomText(),
            fontFamilyType = preferences.getFontFamilyType(),
            customFontPath = preferences.getCustomFontPath(),
            isSystemWallpaperEnabled = preferences.isSystemWallpaperEnabled(),
            solidWallpaperColor = preferences.getSolidWallpaperColor(),
            solidWallpaperTarget = preferences.getSolidWallpaperTarget(),
            isThemedIconsEnabled = preferences.isThemedIconsEnabled(),
            isHideAppLabelsEnabled = preferences.isHideAppLabelsEnabled(),
            selectedLanguage = preferences.getSelectedLanguage(),
            homeWallpaperDimming = preferences.getHomeWallpaperDimming(),
            isDrawerGlassEnabled = preferences.isDrawerGlassEnabled(),
            drawerGlassOpacity = preferences.getDrawerGlassOpacity(),
            searchBarOpacity = preferences.getSearchBarOpacity(),
            themeMode = preferences.getThemeMode(),

            // Clima & Localização
            hasLocationPermission = weatherHelper.hasLocationPermission(),
            isWeatherCelsius = preferences.isWeatherCelsius(),
            weatherInfo = weatherHelper.fromJson(preferences.getCachedWeatherJson()),

            // Configurações & Estilos dos Widgets
            batteryWidgetStyle = preferences.getBatteryWidgetStyle(),
            mediaWidgetStyle = preferences.getMediaWidgetStyle(),
            notesWidgetFilter = preferences.getNotesWidgetFilter(),
            calendarHowFarAhead = preferences.getCalendarHowFarAhead(),
            calendarHideFinished = preferences.isCalendarHideFinished(),
            calendarIs24hFormat = preferences.isCalendar24hFormat()
        )
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()
    private var autoLaunchJob: Job? = null

    init {
        observeInstalledApps()
        observeSystemInfo()
        observeMediaService()
        observeQuickSettings()
        refreshCalendarAndPermissions()
        refreshWeather()
    }

    fun observeInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(appsState = AppsListState.Loading) }
            appRepository.observeApps(
                iconPackPackage = preferences.getSelectedIconPack(),
                customIcons = _uiState.value.customAppIcons,
                hiddenPackages = emptySet()
            )
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
                    _uiState.update { it.copy(allInstalledApps = apps) }
                    applyFilter(_uiState.value.searchQuery)
                }
        }
    }

    fun reloadAppsWithIconPack(pack: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(appsState = AppsListState.Loading) }
            val apps = runCatching { appRepository.loadApps(pack) }.getOrDefault(allApps)
            allApps = apps
            applyFilter(_uiState.value.searchQuery)
            _uiState.update { it.copy(appsState = AppsListState.Success(apps)) }
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

    private fun observeQuickSettings() {
        viewModelScope.launch {
            quickSettingsHelper.isTorchOn.collect { on ->
                _uiState.update { it.copy(isTorchOn = on) }
            }
        }
        viewModelScope.launch {
            quickSettingsHelper.ringerMode.collect { mode ->
                _uiState.update { it.copy(ringerMode = mode) }
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
                searchQuery = "",
                calculatorResult = null,
                matchingContacts = emptyList(),
                matchingFiles = emptyList()
            )
        }
        applyFilter("")
    }

    fun onSearchQueryChange(query: String) {
        val hasQuery = query.isNotBlank()
        val symbol = _uiState.value.searchoActivationSymbol
        val trimmed = query.trim()

        // 1. Verificação de PIN para desbloqueio de apps ocultos
        val pin = _uiState.value.hiddenAppsPin
        if (pin.isNotEmpty() && trimmed == pin) {
            _uiState.update {
                it.copy(
                    isPinUnlocked = true,
                    searchQuery = ""
                )
            }
            applyFilter("")
            return
        }

        // 2. Calculadora: Ativada EXCLUSIVAMENTE via @calc <expressão>
        val isCalcCommand = query.startsWith("@calc", ignoreCase = true) ||
                query.startsWith("${symbol}calc", ignoreCase = true)
        val calcResult = if (isCalcCommand) {
            val expr = query.substringAfter("calc", "").trim()
            if (expr.isNotEmpty()) MathEvaluator.evaluate(expr) else null
        } else {
            null
        }

        // Se for comando @calc, isola 100% a calculadora (sem contatos, sem arquivos, sem apps)
        if (isCalcCommand) {
            autoLaunchJob?.cancel()
            _uiState.update {
                it.copy(
                    searchQuery = query,
                    calculatorResult = calcResult,
                    matchingContacts = emptyList(),
                    matchingFiles = emptyList(),
                    filteredApps = emptyList(),
                    appsState = AppsListState.Success(emptyList()),
                    isDrawerOpen = if (hasQuery) true else it.isDrawerOpen,
                    isSearchExpanded = true,
                    isWidgetExpanded = !hasQuery
                )
            }
            return
        }

        // 3. SearchOS: Contatos via @con ou busca geral se habilitado (não buscar se for outro comando @)
        val isContactsCommand = query.startsWith("@con", ignoreCase = true) ||
                query.startsWith("${symbol}con", ignoreCase = true)
        val isOtherCommand = (query.startsWith("@") || query.startsWith(symbol)) && !isContactsCommand
        val contactsQuery = if (isContactsCommand) {
            query.substringAfter("con", "").trim()
        } else {
            query
        }
        val contacts = if (!isOtherCommand && (isContactsCommand || _uiState.value.isContactsSearchEnabled) &&
            _uiState.value.hasContactsPermission && contactsQuery.isNotBlank()
        ) {
            contactSearchHelper.searchContacts(contactsQuery)
        } else {
            emptyList()
        }

        // 4. SearchOS: Arquivos via @files ou busca geral de arquivos
        val isFilesCommand = query.startsWith("@files", ignoreCase = true) ||
                query.startsWith("${symbol}files", ignoreCase = true)
        val isOtherFilesCommand = (query.startsWith("@") || query.startsWith(symbol)) && !isFilesCommand
        val filesQuery = if (isFilesCommand) {
            query.substringAfter("files", "").trim()
        } else {
            query
        }
        val files = if (!isOtherFilesCommand && (isFilesCommand || _uiState.value.isFilesSearchEnabled) && filesQuery.length >= 2) {
            fileSearchHelper.searchFiles(filesQuery)
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                searchQuery = query,
                calculatorResult = null,
                matchingContacts = contacts,
                matchingFiles = files,
                isDrawerOpen = if (hasQuery) true else it.isDrawerOpen,
                isSearchExpanded = true,
                isWidgetExpanded = !hasQuery
            )
        }

        if (isContactsCommand || isFilesCommand) {
            autoLaunchJob?.cancel()
            _uiState.update {
                it.copy(
                    filteredApps = emptyList(),
                    appsState = AppsListState.Success(emptyList())
                )
            }
            return
        }

        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        autoLaunchJob?.cancel()

        val isExact = _uiState.value.isExactSearchEnabled
        val hiddenPkgs = if (_uiState.value.isPinUnlocked) emptySet() else _uiState.value.hiddenAppsPackages
        val visibleApps = allApps.filterNot { hiddenPkgs.contains(it.packageName) }

        val filtered = if (trimmed.isEmpty()) {
            visibleApps
        } else {
            val normalizedQuery = AppInfo.normalize(trimmed)
            if (isExact) {
                visibleApps.filter { app ->
                    app.normalizedLabel.equals(normalizedQuery, ignoreCase = true) ||
                            app.label.equals(trimmed, ignoreCase = true)
                }
            } else {
                visibleApps.filter { app ->
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
                isSearchExpanded = false,
                searchQuery = "",
                calculatorResult = null,
                matchingContacts = emptyList(),
                matchingFiles = emptyList()
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
                SettingsSubScreen.FOLDERS -> it.copy(currentSettingsScreen = SettingsSubScreen.EXTRAS)
                SettingsSubScreen.SEARCHOS -> it.copy(currentSettingsScreen = SettingsSubScreen.EXTRAS)
                SettingsSubScreen.HIDDEN_APPS -> it.copy(currentSettingsScreen = SettingsSubScreen.EXTRAS)
                SettingsSubScreen.EXTRAS -> it.copy(currentSettingsScreen = SettingsSubScreen.MAIN)
                SettingsSubScreen.GESTURES -> it.copy(currentSettingsScreen = SettingsSubScreen.MAIN)
                SettingsSubScreen.CUSTOMIZATION -> it.copy(currentSettingsScreen = SettingsSubScreen.MAIN)
                SettingsSubScreen.PERMISSIONS -> it.copy(currentSettingsScreen = SettingsSubScreen.MAIN)
                SettingsSubScreen.IN_APP_SEARCH -> it.copy(currentSettingsScreen = SettingsSubScreen.SEARCH)
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
        val mode = if (enabled) "AMOLED" else "DARK"
        preferences.setThemeMode(mode)
        _uiState.update { it.copy(isAmoledMode = enabled, themeMode = mode) }
    }

    fun setThemeMode(mode: String) {
        preferences.setThemeMode(mode)
        val isAmoled = mode == "AMOLED"
        preferences.setAmoledMode(isAmoled)
        _uiState.update { it.copy(themeMode = mode, isAmoledMode = isAmoled) }
    }

    fun setHomeWallpaperDimming(percent: Int) {
        preferences.setHomeWallpaperDimming(percent)
        _uiState.update { it.copy(homeWallpaperDimming = percent) }
    }

    fun setDrawerGlassEnabled(enabled: Boolean) {
        preferences.setDrawerGlassEnabled(enabled)
        _uiState.update { it.copy(isDrawerGlassEnabled = enabled) }
    }

    fun setDrawerGlassOpacity(percent: Int) {
        preferences.setDrawerGlassOpacity(percent)
        _uiState.update { it.copy(drawerGlassOpacity = percent) }
    }

    fun setSearchBarOpacity(percent: Int) {
        preferences.setSearchBarOpacity(percent)
        _uiState.update { it.copy(searchBarOpacity = percent) }
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

    // Ações Rápidas (Quick Settings)
    fun toggleTorch() {
        quickSettingsHelper.toggleTorch()
    }

    fun openWifiSettings() {
        quickSettingsHelper.openWifiSettings()
    }

    fun openBluetoothSettings() {
        quickSettingsHelper.openBluetoothSettings()
    }

    fun cycleRingerMode() {
        quickSettingsHelper.cycleRingerMode()
    }

    // Busca e Chamada de Contatos
    fun checkContactsPermission() {
        val hasPerm = contactSearchHelper.hasContactsPermission()
        _uiState.update { it.copy(hasContactsPermission = hasPerm) }
    }

    fun setContactsPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasContactsPermission = granted) }
        if (granted && _uiState.value.searchQuery.isNotBlank()) {
            val contacts = contactSearchHelper.searchContacts(_uiState.value.searchQuery)
            _uiState.update { it.copy(matchingContacts = contacts) }
        }
    }

    fun callContact(phoneNumber: String) {
        contactSearchHelper.callContact(phoneNumber)
    }

    // Gestão de Tarefas e Notas
    fun addNoteTask(text: String) {
        val current = _uiState.value.notesTasks.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        current.add(NoteTask(id = nextId, text = text, isDone = false))
        preferences.setNotesRaw(serializeNotes(current))
        _uiState.update { it.copy(notesTasks = current) }
    }

    fun toggleNoteTask(id: Long) {
        val current = _uiState.value.notesTasks.map { task ->
            if (task.id == id) task.copy(isDone = !task.isDone) else task
        }
        preferences.setNotesRaw(serializeNotes(current))
        _uiState.update { it.copy(notesTasks = current) }
    }

    fun removeNoteTask(id: Long) {
        val current = _uiState.value.notesTasks.filterNot { it.id == id }
        preferences.setNotesRaw(serializeNotes(current))
        _uiState.update { it.copy(notesTasks = current) }
    }

    // Estilo e Pacotes de Ícones
    fun setIconShape(shape: String) {
        preferences.setIconShape(shape)
        _uiState.update { it.copy(iconShape = shape) }
    }

    fun setSelectedIconPack(packageName: String?) {
        preferences.setSelectedIconPack(packageName)
        _uiState.update { it.copy(selectedIconPack = packageName) }
        reloadAppsWithIconPack(packageName)
    }

    fun getInstalledIconPacks(): List<IconPackInfo> {
        return appRepository.iconPackHelper.getInstalledIconPacks()
    }

    // Barra de Status do Sistema
    fun setShowStatusBarEnabled(enabled: Boolean) {
        preferences.setShowStatusBarEnabled(enabled)
        _uiState.update { it.copy(isShowStatusBarEnabled = enabled) }
    }

    // Gestão de Pastas
    fun createFolder(name: String, appPackages: List<String> = emptyList()) {
        val current = _uiState.value.appFolders.toMutableList()
        val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        current.add(AppFolder(id = nextId, name = name, packageNames = appPackages))
        preferences.setFoldersRaw(serializeFolders(current))
        _uiState.update { it.copy(appFolders = current) }
    }

    fun updateFolder(id: Long, name: String, appPackages: List<String>) {
        val current = _uiState.value.appFolders.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(name = name, packageNames = appPackages)
            preferences.setFoldersRaw(serializeFolders(current))
            _uiState.update { it.copy(appFolders = current) }
        }
    }

    fun removeFolder(id: Long) {
        val current = _uiState.value.appFolders.filterNot { it.id == id }
        preferences.setFoldersRaw(serializeFolders(current))
        _uiState.update { it.copy(appFolders = current) }
    }

    fun openSms(address: String) {
        messageSearchHelper.openSmsConversation(address)
    }

    fun executeGestureAction(actionKey: String, context: Context) {
        when {
            actionKey == "launcher_settings" -> openSettings()
            actionKey == "system_settings" -> {
                runCatching {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
            actionKey == "notifications" -> {
                val opened = TesseraAccessibilityService.openNotifications()
                if (!opened) {
                    try {
                        val statusBarService = context.getSystemService("statusbar")
                        val statusBarManager = Class.forName("android.app.StatusBarManager")
                        val method = statusBarManager.getMethod("expandNotificationsPanel")
                        method.invoke(statusBarService)
                    } catch (_: Exception) {
                    }
                }
            }
            actionKey == "quick_settings" -> {
                val opened = TesseraAccessibilityService.openQuickSettings()
                if (!opened) {
                    try {
                        val statusBarService = context.getSystemService("statusbar")
                        val statusBarManager = Class.forName("android.app.StatusBarManager")
                        val method = statusBarManager.getMethod("expandSettingsPanel")
                        method.invoke(statusBarService)
                    } catch (_: Exception) {
                    }
                }
            }
            actionKey == "open_keyboard" -> {
                expandSearch()
                openDrawer()
            }
            actionKey == "lock_screen" -> {
                val locked = TesseraAccessibilityService.lockScreen()
                if (!locked) {
                    android.widget.Toast.makeText(
                        context,
                        "Ative o Serviço de Acessibilidade do Tessera para bloquear a tela",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    runCatching {
                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
            }
            actionKey.startsWith("app:") -> {
                val pkg = actionKey.removePrefix("app:")
                launchApp(pkg)
            }
            actionKey.startsWith("shortcut:") -> {
                val parts = actionKey.removePrefix("shortcut:").split(":::")
                if (parts.size >= 2) {
                    launchShortcut(parts[0], parts[1])
                }
            }
        }
    }

    // Gestão de Searchos
    fun removeSearcho(id: String) {
        val current = _uiState.value.searchosList.filterNot { it.id == id }
        preferences.setSearchosRaw(serializeSearchos(current))
        _uiState.update { it.copy(searchosList = current) }
    }

    fun addSearcho(title: String, prefix: String, iconType: String = "tasks") {
        val current = _uiState.value.searchosList.toMutableList()
        val cleanPrefix = if (prefix.startsWith("@")) prefix else "@$prefix"
        current.add(
            SearchoItem(
                id = "searcho_${System.currentTimeMillis()}",
                title = title,
                prefix = cleanPrefix,
                iconType = iconType
            )
        )
        preferences.setSearchosRaw(serializeSearchos(current))
        _uiState.update { it.copy(searchosList = current) }
    }

    fun setSearchoActivationSymbol(symbol: String) {
        preferences.setSearchoActivationSymbol(symbol)
        _uiState.update { it.copy(searchoActivationSymbol = symbol) }
    }

    // Gestão de Gestos
    fun setDoubleTapEnabled(enabled: Boolean) {
        preferences.setDoubleTapEnabled(enabled)
        _uiState.update { it.copy(isDoubleTapEnabled = enabled) }
    }
    fun setDoubleTapAction(action: String) {
        preferences.setDoubleTapAction(action)
        _uiState.update { it.copy(doubleTapAction = action) }
    }
    fun setHoldEnabled(enabled: Boolean) {
        preferences.setHoldEnabled(enabled)
        _uiState.update { it.copy(isHoldEnabled = enabled) }
    }
    fun setHoldAction(action: String) {
        preferences.setHoldAction(action)
        _uiState.update { it.copy(holdAction = action) }
    }
    fun setSwipeDownEnabled(enabled: Boolean) {
        preferences.setSwipeDownEnabled(enabled)
        _uiState.update { it.copy(isSwipeDownEnabled = enabled) }
    }
    fun setSwipeDownAction(action: String) {
        preferences.setSwipeDownAction(action)
        _uiState.update { it.copy(swipeDownAction = action) }
    }
    fun setSwipeUpEnabled(enabled: Boolean) {
        preferences.setSwipeUpEnabled(enabled)
        _uiState.update { it.copy(isSwipeUpEnabled = enabled) }
    }
    fun setSwipeUpAction(action: String) {
        preferences.setSwipeUpAction(action)
        _uiState.update { it.copy(swipeUpAction = action) }
    }
    fun setSwipeLeftEnabled(enabled: Boolean) {
        preferences.setSwipeLeftEnabled(enabled)
        _uiState.update { it.copy(isSwipeLeftEnabled = enabled) }
    }
    fun setSwipeLeftAction(action: String) {
        preferences.setSwipeLeftAction(action)
        _uiState.update { it.copy(swipeLeftAction = action) }
    }
    fun setSwipeRightEnabled(enabled: Boolean) {
        preferences.setSwipeRightEnabled(enabled)
        _uiState.update { it.copy(isSwipeRightEnabled = enabled) }
    }
    fun setSwipeRightAction(action: String) {
        preferences.setSwipeRightAction(action)
        _uiState.update { it.copy(swipeRightAction = action) }
    }

    // Busca em Apps & Arquivos
    fun toggleInAppSearchPackage(pkg: String) {
        val current = _uiState.value.inAppSearchPackages.toMutableSet()
        if (current.contains(pkg)) current.remove(pkg) else current.add(pkg)
        preferences.setInAppSearchPackages(current)
        _uiState.update { it.copy(inAppSearchPackages = current) }
    }
    fun setFilesSearchEnabled(enabled: Boolean) {
        preferences.setFilesSearchEnabled(enabled)
        _uiState.update { it.copy(isFilesSearchEnabled = enabled) }
    }
    fun openFile(uriString: String, mimeType: String?) {
        fileSearchHelper.openFile(uriString, mimeType)
    }

    // Apps Ocultos & PIN
    fun setHiddenAppsPin(pin: String) {
        preferences.setHiddenAppsPin(pin)
        _uiState.update { it.copy(hiddenAppsPin = pin) }
    }
    fun toggleHiddenApp(pkg: String) {
        val current = _uiState.value.hiddenAppsPackages.toMutableSet()
        if (current.contains(pkg)) current.remove(pkg) else current.add(pkg)
        preferences.setHiddenAppsPackages(current)
        _uiState.update { it.copy(hiddenAppsPackages = current) }
        applyFilter(_uiState.value.searchQuery)
    }
    fun unlockHiddenAppsWithPin(pin: String): Boolean {
        if (pin == _uiState.value.hiddenAppsPin || _uiState.value.hiddenAppsPin.isEmpty()) {
            _uiState.update { it.copy(isPinUnlocked = true) }
            applyFilter(_uiState.value.searchQuery)
            return true
        }
        return false
    }
    fun lockHiddenApps() {
        _uiState.update { it.copy(isPinUnlocked = false) }
        applyFilter(_uiState.value.searchQuery)
    }

    // Ícones Customizados Individuais
    fun setCustomAppIcon(packageName: String, iconPackPackage: String?) {
        val current = _uiState.value.customAppIcons.toMutableMap()
        if (iconPackPackage.isNullOrBlank()) {
            current.remove(packageName)
        } else {
            current[packageName] = iconPackPackage
        }
        preferences.setCustomAppIconsRaw(serializeCustomIcons(current))
        _uiState.update { it.copy(customAppIcons = current) }
        observeInstalledApps()
    }
    fun removeCustomAppIcon(packageName: String) {
        setCustomAppIcon(packageName, null)
    }

    // Atalhos de Aplicativos
    fun getAppShortcuts(packageName: String) = appRepository.getAppShortcuts(packageName)
    fun launchShortcut(packageName: String, shortcutId: String) = appRepository.launchShortcut(packageName, shortcutId)

    // Customização Avançada
    fun setSearchBarStyle(style: String) {
        preferences.setSearchBarStyle(style)
        _uiState.update { it.copy(searchBarStyle = style) }
    }

    fun setSearchBarTextType(type: String) {
        preferences.setSearchBarTextType(type)
        _uiState.update { it.copy(searchBarTextType = type) }
    }

    fun setSearchBarCustomText(text: String) {
        preferences.setSearchBarCustomText(text)
        _uiState.update { it.copy(searchBarCustomText = text) }
    }

    fun setFontFamilyType(type: String) {
        preferences.setFontFamilyType(type)
        _uiState.update { it.copy(fontFamilyType = type) }
    }

    fun setCustomFontPath(path: String) {
        preferences.setCustomFontPath(path)
        _uiState.update { it.copy(customFontPath = path) }
    }

    fun setSystemWallpaperEnabled(enabled: Boolean) {
        preferences.setSystemWallpaperEnabled(enabled)
        _uiState.update { it.copy(isSystemWallpaperEnabled = enabled) }
    }

    fun setSolidWallpaperColor(hex: String) {
        preferences.setSolidWallpaperColor(hex)
        _uiState.update { it.copy(solidWallpaperColor = hex) }
    }

    fun setSolidWallpaperTarget(target: String) {
        preferences.setSolidWallpaperTarget(target)
        _uiState.update { it.copy(solidWallpaperTarget = target) }
    }

    fun applySolidWallpaper(colorHex: String, target: String, context: Context) {
        setSolidWallpaperColor(colorHex)
        setSolidWallpaperTarget(target)
        try {
            val wallpaperManager = android.app.WallpaperManager.getInstance(context)
            val color = android.graphics.Color.parseColor(colorHex)
            val bitmap = android.graphics.Bitmap.createBitmap(1080, 1920, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(color)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val flag = when (target) {
                    "home" -> android.app.WallpaperManager.FLAG_SYSTEM
                    "lock" -> android.app.WallpaperManager.FLAG_LOCK
                    else -> android.app.WallpaperManager.FLAG_SYSTEM or android.app.WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
        } catch (_: Exception) {
        }
    }

    fun importCustomFont(uri: android.net.Uri, context: Context) {
        viewModelScope.launch {
            try {
                val fontsDir = java.io.File(context.filesDir, "fonts")
                if (!fontsDir.exists()) fontsDir.mkdirs()
                val destFile = java.io.File(fontsDir, "custom_font_${System.currentTimeMillis()}.ttf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (destFile.exists() && destFile.length() > 0) {
                    setCustomFontPath(destFile.absolutePath)
                    setFontFamilyType("custom")
                }
            } catch (_: Exception) {
            }
        }
    }

    fun setThemedIconsEnabled(enabled: Boolean) {
        preferences.setThemedIconsEnabled(enabled)
        _uiState.update { it.copy(isThemedIconsEnabled = enabled) }
    }

    fun setHideAppLabelsEnabled(enabled: Boolean) {
        preferences.setHideAppLabelsEnabled(enabled)
        _uiState.update { it.copy(isHideAppLabelsEnabled = enabled) }
    }

    fun setSelectedLanguage(lang: String) {
        preferences.setSelectedLanguage(lang)
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun setLanguageModalOpen(open: Boolean) {
        _uiState.update { it.copy(isLanguageModalOpen = open) }
    }

    fun refreshAllPermissions(context: Context) {
        val pm = context.packageManager
        val isDefault = try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
            val resolve = pm.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            resolve?.activityInfo?.packageName == context.packageName
        } catch (_: Exception) { false }

        val hasSms = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasMusic = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_MEDIA_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        val hasImages = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        val hasA11y = try {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            enabledServices.contains(context.packageName)
        } catch (_: Exception) { false }

        _uiState.update {
            it.copy(
                isDefaultLauncher = isDefault,
                hasSmsPermission = hasSms,
                hasMusicPermission = hasMusic,
                hasMediaImagesPermission = hasImages,
                hasAccessibilityService = hasA11y,
                hasContactsPermission = contactSearchHelper.hasContactsPermission(),
                hasNotificationAccess = TesseraMediaService.isNotificationAccessGranted(context),
                hasLocationPermission = weatherHelper.hasLocationPermission()
            )
        }
        if (weatherHelper.hasLocationPermission()) {
            refreshWeather()
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            val hasPerm = weatherHelper.hasLocationPermission()
            _uiState.update { it.copy(hasLocationPermission = hasPerm, isWeatherLoading = true, weatherError = null) }
            val weather = weatherHelper.fetchWeather(isCelsius = _uiState.value.isWeatherCelsius)
            if (weather != null) {
                preferences.setCachedWeatherJson(weatherHelper.toJson(weather))
                _uiState.update { it.copy(weatherInfo = weather, isWeatherLoading = false, weatherError = null) }
            } else {
                val cached = weatherHelper.fromJson(preferences.getCachedWeatherJson())
                if (cached != null) {
                    _uiState.update { it.copy(weatherInfo = cached, isWeatherLoading = false, weatherError = null) }
                } else {
                    _uiState.update { it.copy(isWeatherLoading = false, weatherError = "Falha ao obter previsão") }
                }
            }
        }
    }

    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }
        if (hasPermission) {
            refreshWeather()
        }
    }

    fun setWeatherCelsius(isCelsius: Boolean) {
        preferences.setWeatherCelsius(isCelsius)
        _uiState.update { state ->
            val updatedWeather = state.weatherInfo?.copy(isCelsius = isCelsius)
            state.copy(
                isWeatherCelsius = isCelsius,
                weatherInfo = updatedWeather
            )
        }
    }

    fun openWidgetConfig(type: WidgetConfigType) {
        _uiState.update { it.copy(activeWidgetConfigModal = type) }
    }

    fun closeWidgetConfig() {
        _uiState.update { it.copy(activeWidgetConfigModal = null) }
    }

    fun setBatteryWidgetStyle(style: String) {
        preferences.setBatteryWidgetStyle(style)
        _uiState.update { it.copy(batteryWidgetStyle = style) }
    }

    fun setMediaWidgetStyle(style: String) {
        preferences.setMediaWidgetStyle(style)
        _uiState.update { it.copy(mediaWidgetStyle = style) }
    }

    fun setNotesWidgetFilter(filter: String) {
        preferences.setNotesWidgetFilter(filter)
        _uiState.update { it.copy(notesWidgetFilter = filter) }
    }

    fun setCalendarHowFarAhead(days: Int) {
        preferences.setCalendarHowFarAhead(days)
        _uiState.update { it.copy(calendarHowFarAhead = days) }
    }

    fun setCalendarHideFinished(hide: Boolean) {
        preferences.setCalendarHideFinished(hide)
        _uiState.update { it.copy(calendarHideFinished = hide) }
    }

    fun setCalendarIs24hFormat(is24h: Boolean) {
        preferences.setCalendar24hFormat(is24h)
        _uiState.update { it.copy(calendarIs24hFormat = is24h) }
    }

    private fun normalizeString(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }
}
