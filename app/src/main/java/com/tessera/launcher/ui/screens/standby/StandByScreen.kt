package com.tessera.launcher.ui.screens.standby

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.helper.CalendarEventInfo
import com.tessera.launcher.data.helper.WeatherInfo
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import com.tessera.launcher.ui.theme.apple.interFont
import java.util.Calendar

/**
 * Modo Em Espera Oficial da Apple (iOS StandBy Mode).
 * Ativa quando o smartphone está em carregamento horizontal (paisagem).
 * Apresenta relógio de mesa de cabeceira em escala monumento e Modo Noite em luz vermelha.
 */
@Composable
fun StandByScreen(
    isCharging: Boolean,
    formattedTime: String,
    formattedDate: String,
    batteryPercentage: Int,
    weather: WeatherInfo?,
    calendarEvent: CalendarEventInfo?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // StandBy só é exibido ativamente quando carregando e em paisagem
    val isStandByActive = isCharging && isLandscape

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNightMode = currentHour >= 22 || currentHour < 6

    val activeColor = if (isNightMode) Color(0xFFFF3B30) else Color.White
    val secondaryColor = if (isNightMode) Color(0xFFFF3B30).copy(alpha = 0.5f) else Color(0x99FFFFFF)
    val cardBackground = if (isNightMode) Color(0x1A000000) else Color(0x221C1C1E)

    AnimatedVisibility(
        visible = isStandByActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metade Esquerda: Relógio Monumental de Cabeceira
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 94.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interFont(500, 32f),
                        color = activeColor,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = formattedDate.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFont(700, 16f),
                        color = secondaryColor,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Metade Direita: Cartões Duplos em Vidro (Clima + Bateria/Agenda)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Cartão de Clima em StandBy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(ContinuousShape(24.dp))
                            .background(cardBackground)
                            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = weather?.cityName ?: "Tempo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor
                                )
                                Text(
                                    text = "${weather?.temperature?.toInt() ?: 24}°",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Light,
                                    color = activeColor
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cartão de Bateria / Status em StandBy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(ContinuousShape(24.dp))
                            .background(cardBackground)
                            .liquidGlass(style = GlassStyle.Regular, shape = ContinuousShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Carregando",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor
                                )
                                Text(
                                    text = "$batteryPercentage%",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Light,
                                    color = activeColor
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.BatteryChargingFull,
                                contentDescription = null,
                                tint = if (isNightMode) Color(0xFFFF3B30) else Color(0xFF34C759),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
