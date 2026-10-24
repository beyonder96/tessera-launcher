package com.tessera.launcher.ui.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Widget Oficial de Tempo da Apple (iOS Weather Widget).
 * Tipografia grande de temperatura com símbolo de grau tabular, ícone semântico e máxima/mínima.
 */
@Composable
fun AppleWeatherWidget(
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val conditionText = weather?.condition ?: "Ensolarado"
    val temperature = weather?.temperature?.toInt() ?: 24
    val locationName = weather?.cityName ?: "Minha Localização"

    val (weatherIcon, iconTint) = when {
        conditionText.contains("Chuva", ignoreCase = true) -> Icons.Outlined.WaterDrop to Color(0xFF007AFF)
        conditionText.contains("Tempestade", ignoreCase = true) -> Icons.Outlined.Thunderstorm to Color(0xFF5856D6)
        conditionText.contains("Nublado", ignoreCase = true) -> Icons.Outlined.Cloud to Color(0xFF8E8E93)
        else -> Icons.Outlined.WbSunny to Color(0xFFFF9500)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(ContinuousShape(22.dp))
            .background(colors.secondaryGrouped)
            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(22.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linha superior: Cidade e Ícone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = locationName,
                        style = typography.headline,
                        color = colors.label
                    )
                    Text(
                        text = conditionText,
                        style = typography.footnote,
                        color = colors.secondaryLabel
                    )
                }

                Icon(
                    imageVector = weatherIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Linha inferior: Temperatura Hero e Mín/Máx
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$temperature°",
                    style = typography.largeTitle.copy(fontWeight = FontWeight.Medium),
                    color = colors.label
                )

                Text(
                    text = "Mín: ${temperature - 3}°  Máx: ${temperature + 4}°",
                    style = typography.caption1,
                    color = colors.secondaryLabel
                )
            }
        }
    }
}
