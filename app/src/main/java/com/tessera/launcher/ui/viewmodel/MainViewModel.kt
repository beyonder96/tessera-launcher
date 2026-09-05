package com.tessera.launcher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tessera.launcher.data.helper.SystemInfoHelper
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.data.repository.AppRepository
import com.tessera.launcher.ui.state.AppsListState
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.MediaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

class MainViewModel(
    private val appRepository: AppRepository,
    private val systemInfoHelper: SystemInfoHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    init {
        observeInstalledApps()
        observeSystemInfo()
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

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                isDrawerOpen = if (query.isNotBlank()) true else it.isDrawerOpen
            )
        }
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allApps
        } else {
            val normalizedQuery = normalizeString(trimmed)
            allApps.filter { app ->
                normalizeString(app.label).contains(normalizedQuery) ||
                        app.packageName.contains(trimmed, ignoreCase = true)
            }
        }

        // Pré-computa o mapa O(1) de Letra -> Índice inicial na LazyColumn
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
    }

    fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true) }
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
        _uiState.update { it.copy(isWidgetExpanded = !it.isWidgetExpanded) }
    }

    fun handleBackPress(): Boolean {
        val state = _uiState.value
        return when {
            state.searchQuery.isNotEmpty() -> {
                onSearchQueryChange("")
                true
            }
            state.isWidgetExpanded -> {
                setWidgetsExpanded(false)
                true
            }
            state.isDrawerOpen -> {
                closeDrawer()
                true
            }
            else -> false // Já está na Home limpa, BackHandler intercepta sem fechar launcher
        }
    }

    fun launchApp(packageName: String): Result<Unit> {
        return appRepository.launchApp(packageName)
    }

    fun reloadApps() {
        observeInstalledApps()
    }

    // Ações de Mídia Mock v1
    fun togglePlayPause() {
        _uiState.update {
            it.copy(mediaState = it.mediaState.copy(isPlaying = !it.mediaState.isPlaying))
        }
    }

    fun skipNext() {
        val tracks = listOf(
            MediaState("Ambient Echoes", "Searcho Sessions", true, 0.15f),
            MediaState("Subtle Drift", "Mono Architecture", true, 0.45f),
            MediaState("Minimal Focus", "Komorebi Pulse", true, 0.80f)
        )
        val next = tracks.random()
        _uiState.update { it.copy(mediaState = next) }
    }

    fun skipPrevious() {
        _uiState.update {
            it.copy(mediaState = it.mediaState.copy(progress = 0f))
        }
    }

    private fun normalizeString(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }
}
