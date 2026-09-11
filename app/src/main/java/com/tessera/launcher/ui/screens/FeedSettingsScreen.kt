package com.tessera.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                text = "Feed Social",
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
            
            // Ativar Feed Social
            SettingsSwitchRow(
                title = "Ativar Feed Social",
                checked = uiState.isFeedEnabled,
                onCheckedChange = { viewModel.setFeedEnabled(it) },
                textColor = textColor,
                surfaceColor = surfaceColor
            )

            if (uiState.isFeedEnabled) {
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
