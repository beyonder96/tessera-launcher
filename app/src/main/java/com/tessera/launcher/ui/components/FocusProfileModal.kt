package com.tessera.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FocusProfile
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
import com.tessera.launcher.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusProfileModal(
    currentProfile: FocusProfile,
    isScheduleEnabled: Boolean,
    onSelectProfile: (FocusProfile) -> Unit,
    onToggleSchedule: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    isAmoledMode: Boolean = true,
    isLightMode: Boolean = false,
    accentColor: Color = Color.White
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val sheetBg = when {
        isLightMode -> LightCardBackground
        isAmoledMode -> AmoledCardBackground
        else -> DarkSurface
    }
    val primaryText = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryText = if (isLightMode) LightTextSecondary else TextSecondary
    val tertiaryText = TextTertiary
    val dividerColor = if (isLightMode) LightCardBorder else Color(0xFF1E1E26)
    val effectiveAccent = if (accentColor != Color.White && accentColor != Color(0xFF111827)) accentColor else Color.White

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 20.dp
                )
        ) {
            // Drag Indicator
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(effectiveAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SelfImprovement,
                        contentDescription = null,
                        tint = effectiveAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "PERFIS DE FOCO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Adapte a gaveta de aplicativos ao seu momento do dia",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Opções de Perfis
            FocusProfile.entries.forEach { profile ->
                val isSelected = profile == currentProfile
                val icon = when (profile) {
                    FocusProfile.OFF -> Icons.Outlined.GridView
                    FocusProfile.WORK -> Icons.Outlined.WorkOutline
                    FocusProfile.MINDFUL -> Icons.Outlined.SelfImprovement
                }

                FocusProfileOptionCard(
                    profile = profile,
                    icon = icon,
                    isSelected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(profile)
                        onDismiss()
                    },
                    isAmoledMode = isAmoledMode,
                    isLightMode = isLightMode,
                    accentColor = effectiveAccent,
                    primaryTextColor = primaryText,
                    secondaryTextColor = secondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Seção de Programação Automática
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isLightMode) Color(0x14000000) else Color(0x14FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Agendamento Automático",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ativa 'Trabalho' em dias úteis das 09h às 18h",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = secondaryText
                    )
                }

                Switch(
                    checked = isScheduleEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSchedule(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = effectiveAccent,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun FocusProfileOptionCard(
    profile: FocusProfile,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isAmoledMode: Boolean,
    isLightMode: Boolean,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "profile_option_scale"
    )

    val cardBg by animateColorAsState(
        targetValue = when {
            isSelected && isLightMode -> Color(0x14000000)
            isSelected -> accentColor.copy(alpha = 0.12f)
            isLightMode -> Color(0xFFF3F4F6)
            isAmoledMode -> Color(0xFF0C0C0F)
            else -> Color(0xFF141419)
        },
        animationSpec = tween(180),
        label = "profile_card_bg"
    )

    val cardBorder by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.60f)
            isLightMode -> LightCardBorder
            isAmoledMode -> AmoledCardBorder
            else -> DarkSurfaceBorder
        },
        animationSpec = tween(180),
        label = "profile_card_border"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.22f)
                        else if (isLightMode) Color(0x14000000) else Color(0x14FFFFFF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else secondaryTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) accentColor else primaryTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = secondaryTextColor
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = if (accentColor == Color.White) Color.Black else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
