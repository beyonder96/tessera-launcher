package com.tessera.launcher.ui.state

import com.tessera.launcher.data.model.AppInfo

sealed interface AppsListState {
    data object Loading : AppsListState
    data class Success(val apps: List<AppInfo>) : AppsListState
    data class Empty(val query: String) : AppsListState
    data class Error(val message: String) : AppsListState
}

data class MediaState(
    val title: String = "Ambient Echoes",
    val artist: String = "Searcho Sessions",
    val isPlaying: Boolean = false,
    val progress: Float = 0.42f
)

data class LauncherUiState(
    val appsState: AppsListState = AppsListState.Loading,
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val letterIndexMap: Map<Char, Int> = emptyMap(),
    val availableLetters: List<Char> = emptyList(),
    val isDrawerOpen: Boolean = false,
    val isWidgetExpanded: Boolean = false,
    val batteryPercentage: Int = 100,
    val isCharging: Boolean = false,
    val formattedTime: String = "--:--",
    val formattedDate: String = "---",
    val mediaState: MediaState = MediaState()
)
