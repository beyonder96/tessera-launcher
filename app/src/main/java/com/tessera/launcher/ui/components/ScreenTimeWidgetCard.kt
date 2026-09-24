package com.tessera.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.ScreenTimeInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenTimeWidgetCard(
    screenTimeInfo: ScreenTimeInfo?,
    accentColor: Color,
    isAmoledMode: Boolean = false,
    isLightMode: Boolean = false,
    isLiquidGlass: Boolean = false,
    onCardClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasPermission = screenTimeInfo?.hasPermission == true

    val boxBg = when {
        isLightMode -> Color(0x14000000)
        isLiquidGlass -> Color.White.copy(alpha = 0.08f)
        isAmoledMode -> Color.White.copy(alpha = 0.05f)
        else -> Color(0xFF1E1E26)
    }

    val boxBorder = when {
        isLightMode -> Color(0x20000000)
        isLiquidGlass -> Color.White.copy(alpha = 0.12f)
        isAmoledMode -> Color.White.copy(alpha = 0.10f)
        else -> Color(0xFF2C2C38)
    }

    val contentColor = if (isLightMode) Color(0xFF0F172A) else Color.White
    val secondaryColor = if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8)

    val effectiveAccent = if (accentColor != Color.White) {
        accentColor
    } else {
        if (isLightMode) Color(0xFF0F172A) else Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!hasPermission) {
                        val packageUriIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(packageUriIntent)
                        } catch (_: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (_: Exception) {}
                        }
                    } else {
                        onCardClick()
                        openDigitalWellbeing(context)
                    }
                },
                onLongClick = onLongClick
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone indicador à esquerda
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(boxBg)
                .border(BorderStroke(1.dp, boxBorder), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasPermission) Icons.Outlined.HourglassEmpty else Icons.Outlined.LockClock,
                contentDescription = "Tempo de Tela",
                tint = effectiveAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (hasPermission) {
            // Estado Autorizado: Tempo do dia e top aplicativos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tempo de tela",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "• ${screenTimeInfo.formattedTotalTime}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = effectiveAccent
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                val topApps = screenTimeInfo.topApps
                if (topApps.isNotEmpty()) {
                    val summary = topApps.take(2).joinToString("  •  ") { app ->
                        "${app.appName} ${app.formattedTime}"
                    }
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = secondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Nenhum aplicativo ativo hoje",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = secondaryColor
                    )
                }
            }
        } else {
            // Estado Não Autorizado: Convite minimalista
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tempo de tela",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Toque para habilitar acesso",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = effectiveAccent
                )
            }
        }
    }
}

/**
 * Tenta abrir o aplicativo de Bem-Estar Digital do sistema ou o painel de uso.
 */
private fun openDigitalWellbeing(context: Context) {
    val wellbeingIntents = listOf(
        Intent().setClassName("com.google.android.apps.wellbeing", "com.google.android.apps.wellbeing.home.TopLevelSettingsActivity"),
        Intent("com.google.android.apps.wellbeing.action.SETTINGS"),
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    )

    for (intent in wellbeingIntents) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val success = runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
        if (success) return
    }
}
