package com.tessera.launcher.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.ui.state.LauncherUiState
import com.tessera.launcher.ui.theme.*
import com.tessera.launcher.ui.viewmodel.MainViewModel

@Composable
fun FeedSettingsScreen(
    viewModel: MainViewModel,
    uiState: LauncherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightMode = uiState.isLightMode
    val surfaceColor = if (isLightMode) LightCardBackground else DarkSurfaceVariant
    val backgroundColor = if (isLightMode) Color(0xFFF7F7F7) else DarkSurface
    val textColor = if (isLightMode) LightTextPrimary else TextPrimary
    val textSecondary = if (isLightMode) LightTextSecondary else TextSecondary
    val borderColor = if (isLightMode) LightCardBorder else DarkSurfaceBorder

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = textColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tela Lateral (−1)",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MODO DA TELA LATERAL",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            // Opção 1: Central Nothing OS
            LateralModeCard(
                title = "Central Nothing OS (Recomendado)",
                description = "Dashboard com Relógio Dot-Matrix, Quick Settings reais (Lanterna, Som, Wi-Fi, Bluetooth), Bateria, Player de Mídia e Clima.",
                isSelected = uiState.leftScreenMode == "nothing_hub",
                onClick = { viewModel.setLeftScreenMode("nothing_hub") },
                textColor = textColor,
                textSecondary = textSecondary,
                surfaceColor = surfaceColor,
                borderColor = borderColor,
                hasAccentDot = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Opção 2: Feed Social
            LateralModeCard(
                title = "Feed Social (Notícias)",
                description = "Linha do tempo clássica de notícias (G1, TecMundo, Reddit) com feeds RSS e resumos por IA.",
                isSelected = uiState.leftScreenMode == "feed",
                onClick = { viewModel.setLeftScreenMode("feed") },
                textColor = textColor,
                textSecondary = textSecondary,
                surfaceColor = surfaceColor,
                borderColor = borderColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Opção 3: Desativada
            LateralModeCard(
                title = "Desativada",
                description = "Nenhuma tela lateral ao deslizar para a direita na tela inicial.",
                isSelected = uiState.leftScreenMode == "disabled",
                onClick = { viewModel.setLeftScreenMode("disabled") },
                textColor = textColor,
                textSecondary = textSecondary,
                surfaceColor = surfaceColor,
                borderColor = borderColor
            )

            if (uiState.leftScreenMode == "nothing_hub") {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "RECURSOS DA CENTRAL NOTHING OS",
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
                )

                Surface(
                    shape = CardShape,
                    color = surfaceColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "A Central Nothing OS é ativada ao deslizar para a direita na Home. Ela opera de forma 100% offline-first com controles diretos de hardware (lanterna, modos de som), status do sistema e player de mídia sem distrações.",
                            color = textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.navigateBackSettings()
                                viewModel.openFeed()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD71921)),
                            shape = PillShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Testar e Abrir Central Agora",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else if (uiState.leftScreenMode == "feed") {
                Spacer(modifier = Modifier.height(20.dp))
            
                // Ativar Feed Social
                SettingsSwitchRow(
                    title = "Ativar Feed Social",
                    checked = uiState.isFeedEnabled,
                    onCheckedChange = { viewModel.setFeedEnabled(it) },
                    textColor = textColor,
                    surfaceColor = surfaceColor
                )

                if (uiState.isFeedEnabled) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "PRESETS REGIONAIS",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = PillShape,
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setFeedEnabledSources(setOf("NEWS", "REDDIT", "BLUESKY"))
                                viewModel.setFeedSubreddits(listOf("tecnologia", "brasil", "gamesEcultura"))
                                viewModel.setFeedBlueskyHandles(listOf("g1.globo.com", "tecmundo.com.br", "canaltech.com.br"))
                                viewModel.refreshFeed()
                            }
                    ) {
                        Text(
                            text = "🇧🇷 Brasil (PT-BR)",
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Surface(
                        shape = PillShape,
                        color = surfaceColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setFeedEnabledSources(setOf("REDDIT", "BLUESKY"))
                                viewModel.setFeedSubreddits(listOf("technology", "androiddev", "worldnews"))
                                viewModel.setFeedBlueskyHandles(listOf("theverge.com", "techcrunch.com", "bsky.app"))
                                viewModel.refreshFeed()
                            }
                    ) {
                        Text(
                            text = "🌐 Global (EN)",
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "FONTES DE CONTEÚDO",
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                Surface(
                    shape = CardShape,
                    color = surfaceColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        FeedSource.values().forEachIndexed { index, source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleFeedSource(source) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = source.displayName,
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W400
                                )
                                Switch(
                                    checked = uiState.feedEnabledSources.contains(source),
                                    onCheckedChange = { viewModel.toggleFeedSource(source) }
                                )
                            }
                            if (index < FeedSource.values().size - 1) {
                                HorizontalDivider(color = borderColor, thickness = 1.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "IA",
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                SettingsSwitchRow(
                    title = "Resumos com IA",
                    checked = uiState.isFeedAiSummariesEnabled,
                    onCheckedChange = { viewModel.setFeedAiSummariesEnabled(it) },
                    textColor = textColor,
                    surfaceColor = surfaceColor
                )

                val isRedditEnabled = uiState.feedEnabledSources.any { it.name == "REDDIT" }
                if (isRedditEnabled) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "SUBREDDITS (separados por vírgula)",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Surface(
                        shape = CardShape,
                        color = surfaceColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var textValue by remember(uiState.feedSubreddits) { 
                            mutableStateOf(uiState.feedSubreddits.joinToString(", ")) 
                        }
                        BasicTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                textValue = newValue
                                val list = newValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.setFeedSubreddits(list)
                            },
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400
                            ),
                            cursorBrush = SolidColor(textColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                val isBlueskyEnabled = uiState.feedEnabledSources.any { it.name == "BLUESKY" }
                if (isBlueskyEnabled) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "BLUESKY HANDLES (separados por vírgula)",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    Surface(
                        shape = CardShape,
                        color = surfaceColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var textValue by remember(uiState.feedBlueskyHandles) { 
                            mutableStateOf(uiState.feedBlueskyHandles.joinToString(", ")) 
                        }
                        BasicTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                textValue = newValue
                                val list = newValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.setFeedBlueskyHandles(list)
                            },
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W400
                            ),
                            cursorBrush = SolidColor(textColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    surfaceColor: Color
) {
    Surface(
        shape = CardShape,
        color = surfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun LateralModeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    textSecondary: Color,
    surfaceColor: Color,
    borderColor: Color,
    hasAccentDot: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) surfaceColor else surfaceColor.copy(alpha = 0.5f),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasAccentDot) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD71921))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = textSecondary
                )
            )
        }
    }
}
