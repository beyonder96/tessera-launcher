package com.tessera.launcher.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun FeedSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LateralPanelSettingsScreen(
        viewModel = viewModel,
        uiState = uiState,
        onBack = onBack,
        initialTab = 1,
        modifier = modifier
    )
}
