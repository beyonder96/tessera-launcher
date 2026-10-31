package com.tessera.launcher.ui.components

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.RamInfo
import com.tessera.launcher.data.helper.ScreenTimeInfo
import com.tessera.launcher.data.helper.WeatherInfo
import java.util.Locale

/**
 * Grade Bento 2x2 com cápsulas táteis lapidadas para Wi-Fi, Bluetooth, Lanterna e Som.
 */
@Composable
fun QuickControlBentoGrid(
    isWifiActive: Boolean,
    isBluetoothActive: Boolean,
    isTorchOn: Boolean,
    ringerMode: Int,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onToggleTorch: () -> Unit,
    onCycleRingerMode: () -> Unit,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Wi-Fi
            BentoControlTile(
                title = "Wi-Fi",
                subtitle = if (isWifiActive) "Conectado" else "Desativado",
                icon = Icons.Outlined.Wifi,
                isActive = isWifiActive,
                activeColor = Color(0xFF3B82F6),
                isLightMode = isLightMode,
                onClick = onOpenWifi,
                modifier = Modifier.weight(1f)
            )

            // Bluetooth
            BentoControlTile(
                title = "Bluetooth",
                subtitle = if (isBluetoothActive) "Ativo" else "Inativo",
                icon = Icons.Outlined.Bluetooth,
                isActive = isBluetoothActive,
                activeColor = Color(0xFF6366F1),
                isLightMode = isLightMode,
                onClick = onOpenBluetooth,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lanterna
            BentoControlTile(
                title = "Lanterna",
                subtitle = if (isTorchOn) "Ligada" else "Desligada",
                icon = Icons.Outlined.FlashlightOn,
                isActive = isTorchOn,
                activeColor = Color(0xFFF59E0B),
                isLightMode = isLightMode,
                onClick = onToggleTorch,
                modifier = Modifier.weight(1f)
            )

            // Modo de Som (Ringer Mode: 2 = Normal, 1 = Vibrar, 0 = Silencioso)
            val ringerIcon = when (ringerMode) {
                1 -> Icons.Outlined.Vibration
                0 -> Icons.Outlined.NotificationsOff
                else -> Icons.Outlined.VolumeUp
            }
            val ringerSubtitle = when (ringerMode) {
                1 -> "Vibrar"
                0 -> "Silêncio"
                else -> "Som Ativo"
            }
            BentoControlTile(
                title = "Modo de Som",
                subtitle = ringerSubtitle,
                icon = ringerIcon,
                isActive = ringerMode == 2,
                activeColor = Color(0xFF10B981),
                isLightMode = isLightMode,
                onClick = onCycleRingerMode,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BentoControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    isLightMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF161820)
    val borderColor = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(0.5.dp, borderColor, RoundedCornerShape(18.dp))
            .tesseraPressable(pressedScale = 0.94f, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        JournalMedallion(
            icon = icon,
            contentDescription = title,
            size = 38.dp,
            iconSize = 18.dp,
            isLightMode = isLightMode,
            isActive = isActive,
            onClick = null // O clique é tratado pelo container
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isActive) activeColor else textSecondary,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

/**
 * Trio de Anéis de Telemetria (Bateria, Memória RAM e Tempo de Tela) com Canvas zero-allocation.
 */
@Composable
fun JournalTelemetryRingsRow(
    batteryPercentage: Int,
    isCharging: Boolean,
    ramInfo: RamInfo = RamInfo(),
    weatherInfo: WeatherInfo? = null,
    screenTimeInfo: ScreenTimeInfo? = null,
    isLightMode: Boolean = false,
    onWeatherRefresh: (() -> Unit)? = null,
    onScreenTimeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardBg = if (isLightMode) Color(0xFFEFECE2) else Color(0xFF161820)
    val borderColor = if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF)
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(0.5.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Anel Bateria
            TelemetryRingItem(
                valueText = "$batteryPercentage%",
                labelText = if (isCharging) "CARGA" else "BATERIA",
                progress = (batteryPercentage / 100f).coerceIn(0f, 1f),
                ringColor = if (batteryPercentage <= 20) Color(0xFFFF5252) else if (isCharging) Color(0xFF10B981) else Color(0xFF3B82F6),
                isLightMode = isLightMode,
                centerIcon = if (isCharging) Icons.Outlined.Bolt else null
            )

            // Divisor vertical sutil
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 48.dp)
                    .background(if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF))
            )

            // 2. Anel Memória RAM Real
            val ramPercent = ramInfo.usedPercentage
            val ramUsedGbStr = String.format(Locale.getDefault(), "%.1f", ramInfo.usedGb)
            val ramTotalGbStr = String.format(Locale.getDefault(), "%.1f", ramInfo.totalGb)
            val ramRingColor = when {
                ramPercent >= 85 -> Color(0xFFFF5252)
                ramPercent >= 70 -> Color(0xFFF59E0B)
                else -> Color(0xFF10B981)
            }
            TelemetryRingItem(
                valueText = if (ramInfo.totalBytes > 0) "$ramPercent%" else "--%",
                labelText = "RAM",
                progress = (ramPercent / 100f).coerceIn(0f, 1f),
                ringColor = ramRingColor,
                isLightMode = isLightMode,
                centerIcon = Icons.Outlined.Memory,
                onClick = {
                    if (ramInfo.totalBytes > 0) {
                        Toast.makeText(
                            context,
                            "RAM: $ramUsedGbStr GB usados de $ramTotalGbStr GB ($ramPercent%)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            // Divisor vertical sutil
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 48.dp)
                    .background(if (isLightMode) Color(0xFFDDD8CB) else Color(0x25FFFFFF))
            )

            // 3. Anel Tempo de Tela (Meta base diária de 4 horas = 240 minutos)
            val totalMins = if (screenTimeInfo != null && screenTimeInfo.hasPermission) {
                screenTimeInfo.totalTimeMillis / (1000 * 60)
            } else 0L
            val hours = totalMins / 60
            val remMins = totalMins % 60
            val timeText = if (hours > 0) "${hours}h${remMins}m" else "${remMins}m"
            val screenProgress = (totalMins / 240f).coerceIn(0f, 1f)

            TelemetryRingItem(
                valueText = if (totalMins > 0) timeText else "--",
                labelText = "FOCO",
                progress = screenProgress,
                ringColor = Color(0xFF8B5CF6),
                isLightMode = isLightMode,
                onClick = onScreenTimeClick,
                centerIcon = Icons.Outlined.HourglassEmpty
            )
        }
    }
}

@Composable
private fun TelemetryRingItem(
    valueText: String,
    labelText: String,
    progress: Float,
    ringColor: Color,
    isLightMode: Boolean,
    centerIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)
    val trackColor = if (isLightMode) Color(0x201C1917) else Color(0x25FFFFFF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.tesseraPressable(pressedScale = 0.94f, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(54.dp)) {
                val strokeWidth = 3.5.dp.toPx()
                // Track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                // Progresso
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = valueText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (centerIcon != null) {
                Icon(
                    imageVector = centerIcon,
                    contentDescription = null,
                    tint = ringColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = labelText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = textSecondary
            )
        }
    }
}
