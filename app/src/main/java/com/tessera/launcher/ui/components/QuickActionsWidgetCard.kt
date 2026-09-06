package com.tessera.launcher.ui.components

import android.media.AudioManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.CardShape
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

@Composable
fun QuickActionsWidgetCard(
    isTorchOn: Boolean,
    ringerMode: Int,
    onToggleTorch: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onCycleRingerMode: () -> Unit,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(border, CardShape)
            .clip(CardShape)
            .background(background)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wi-Fi
            QuickActionItem(
                icon = Icons.Outlined.Wifi,
                label = "Wi-Fi",
                isActive = false,
                onClick = onOpenWifi
            )

            // Lanterna (Real)
            QuickActionItem(
                icon = Icons.Outlined.FlashlightOn,
                label = "Lanterna",
                isActive = isTorchOn,
                onClick = onToggleTorch
            )

            // Bluetooth
            QuickActionItem(
                icon = Icons.Outlined.Bluetooth,
                label = "Bluetooth",
                isActive = false,
                onClick = onOpenBluetooth
            )

            // Som / Vibrar / Silencioso
            val (soundIcon, soundLabel) = when (ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> Pair(Icons.Outlined.NotificationsOff, "Mudo")
                AudioManager.RINGER_MODE_VIBRATE -> Pair(Icons.Outlined.Vibration, "Vibrar")
                else -> Pair(Icons.Outlined.Notifications, "Som")
            }

            QuickActionItem(
                icon = soundIcon,
                label = soundLabel,
                isActive = ringerMode != AudioManager.RINGER_MODE_SILENT,
                onClick = onCycleRingerMode
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFF1B1E28)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Color.White else TextSecondary
        )
    }
}
