package com.tessera.launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.SmartGlanceActionType
import com.tessera.launcher.data.helper.SmartGlanceBriefing
import com.tessera.launcher.ui.theme.AmoledCardBackground
import com.tessera.launcher.ui.theme.AmoledCardBorder
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LightCardBackground
import com.tessera.launcher.ui.theme.LightCardBorder
import com.tessera.launcher.ui.theme.LightTextPrimary
import com.tessera.launcher.ui.theme.LightTextSecondary
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

/**
 * Smart Glance Header: Resumo contextual inteligente no topo da Tela Inicial.
 * Sintetiza eventos de agenda, clima e tarefas diárias em tempo real.
 */
@Composable
fun SmartGlanceHeader(
    formattedDate: String,
    briefing: SmartGlanceBriefing?,
    onCalendarClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onNotesClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false
) {
    val primaryColor = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryColor = if (isLightMode) LightTextSecondary else TextSecondary

    val surfaceColor = when {
        isLightMode -> LightCardBackground.copy(alpha = 0.85f)
        isAmoledMode -> AmoledCardBackground.copy(alpha = 0.70f)
        else -> DarkSurface.copy(alpha = 0.65f)
    }

    val borderColor = when {
        isLightMode -> LightCardBorder.copy(alpha = 0.60f)
        isAmoledMode -> AmoledCardBorder.copy(alpha = 0.80f)
        else -> DarkSurfaceBorder.copy(alpha = 0.70f)
    }

    val activeBriefing = briefing ?: SmartGlanceBriefing(
        primaryText = "Tudo tranquilo hoje",
        secondaryText = null,
        iconType = "INFO",
        actionType = SmartGlanceActionType.CALENDAR,
        isUrgent = false
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .wrapContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = {
                        when (activeBriefing.actionType) {
                            SmartGlanceActionType.CALENDAR -> onCalendarClick()
                            SmartGlanceActionType.WEATHER -> onWeatherClick()
                            SmartGlanceActionType.NOTES -> onNotesClick()
                            SmartGlanceActionType.NONE -> onCalendarClick()
                        }
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Linha 1: Data de hoje
            Text(
                text = formattedDate.ifEmpty { "Hoje" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Linha 2: Briefing contextual com animação de transição suave
            AnimatedContent(
                targetState = activeBriefing,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "SmartGlanceTransition"
            ) { targetBriefing ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon: ImageVector = when (targetBriefing.iconType) {
                        "ALERT" -> Icons.Outlined.Warning
                        "CALENDAR" -> Icons.AutoMirrored.Outlined.EventNote
                        "RAIN" -> Icons.Outlined.Cloud
                        "SUN" -> Icons.Outlined.WbSunny
                        "CHECK" -> Icons.Outlined.CheckCircle
                        else -> Icons.Outlined.Info
                    }

                    val iconTint = if (targetBriefing.isUrgent) {
                        Color(0xFFEF4444)
                    } else {
                        primaryColor
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = targetBriefing.iconType,
                        modifier = Modifier.size(16.dp),
                        tint = iconTint
                    )

                    Text(
                        text = targetBriefing.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!targetBriefing.secondaryText.isNullOrBlank()) {
                        Text(
                            text = "• ${targetBriefing.secondaryText}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
