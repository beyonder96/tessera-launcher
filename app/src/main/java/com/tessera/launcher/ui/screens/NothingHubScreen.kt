package com.tessera.launcher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.components.NothingAgendaCard
import com.tessera.launcher.ui.components.NothingBatteryCard
import com.tessera.launcher.ui.components.NothingClockCard
import com.tessera.launcher.ui.components.NothingDarkBackground
import com.tessera.launcher.ui.components.NothingMediaTapeCard
import com.tessera.launcher.ui.components.NothingQuickSettingsCard
import com.tessera.launcher.ui.components.NothingRed
import com.tessera.launcher.ui.components.NothingTileBackground
import com.tessera.launcher.ui.components.NothingWeatherCard
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.state.SettingsSubScreen
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun NothingHubScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    BackHandler {
        onClose()
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().coerceAtLeast(36.dp) + 8.dp
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NothingDarkBackground)
            .padding(top = topInset)
    ) {
        // Top Bar Estilo Nothing OS: Botão Voltar + "CENTRAL •" + Atalho Configurações
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão Voltar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NothingTileBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar para Home",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Título Central com Ponto Vermelho Nothing
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CENTRAL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NothingRed)
                )
            }

            // Botão Configurações da Central
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NothingTileBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                            viewModel.openSettings()
                            viewModel.navigateToSettingsSubScreen(SettingsSubScreen.FEED)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Configurações da Central",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conteúdo Rolável dos Widgets Nothing OS
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Relógio Dot-Matrix & Data
            NothingClockCard(uiState = uiState)

            // 2. Controles Rápidos (Wi-Fi, Bluetooth, Lanterna, Som, Calc, Alarme)
            NothingQuickSettingsCard(
                viewModel = viewModel,
                uiState = uiState
            )

            // 3. Bateria & Sinais
            NothingBatteryCard(uiState = uiState)

            // 4. Player de Mídia Tape Recorder
            NothingMediaTapeCard(uiState = uiState)

            // 5. Clima & Previsão
            NothingWeatherCard(
                uiState = uiState,
                onRefresh = { viewModel.refreshWeather() }
            )

            // 6. Agenda & Próximos Eventos
            NothingAgendaCard(uiState = uiState)

            Spacer(modifier = Modifier.height(bottomInset))
        }
    }
}
