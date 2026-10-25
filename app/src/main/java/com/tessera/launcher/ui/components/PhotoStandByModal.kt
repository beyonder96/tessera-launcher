package com.tessera.launcher.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary
import com.tessera.launcher.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Modo Repouso (StandBy / Ambient Mode com Foto e Relógio).
 * Ao ativar, expande a foto no centro com relógio de cabeceira e
 * ativa a rotação automática livre (sensor) para suporte a modo paisagem.
 */
@Composable
fun PhotoStandByModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    photoUriString: String?,
    formattedTime: String,
    formattedDate: String,
    batteryPercentage: Int,
    isCharging: Boolean,
    weatherTemp: String? = null,
    weatherCondition: String? = null,
    accentColor: Color = Color(0xFF34C759)
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Ativa rotação automática por sensor enquanto o Modo Repouso estiver aberto
    DisposableEffect(isOpen) {
        val activity = context as? Activity
        if (isOpen && activity != null) {
            val previousOrientation = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            onDispose {
                activity.requestedOrientation = previousOrientation
            }
        } else {
            onDispose {}
        }
    }

    // Carrega o bitmap da foto em IO thread
    val bitmap by produceState<Bitmap?>(initialValue = null, photoUriString) {
        value = if (!photoUriString.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(photoUriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        } else {
            null
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(260)) + scaleIn(
            animationSpec = tween(320, easing = FastOutSlowInEasing),
            initialScale = 0.88f
        ),
        exit = fadeOut(tween(200)) + scaleOut(
            animationSpec = tween(200),
            targetScale = 0.92f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030305))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Brilho difuso ambiente suave com o tom da foto ao fundo
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                )
                // Vinheta escurecedora profunda AMOLED para contraste perfeito
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDC000000))
                )
            }

            // Botão Fechar discreto no topo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 44.dp, end = 24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Fechar modo repouso",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isLandscape) {
                // Layout Paisagem (Bedside / Nightstand Dock)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Foto ampliada
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                            .aspectRatio(1f)
                            .shadow(28.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black, spotColor = Color.Black)
                            .clip(RoundedCornerShape(32.dp))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f))
                                    )
                                ),
                                RoundedCornerShape(32.dp)
                            )
                            .background(Color(0xFF141419)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Foto em modo repouso",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp))

                    // Relógio e Informações de Cabeceira
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                            color = TextPrimary,
                            letterSpacing = (-2).sp
                        )

                        Text(
                            text = formattedDate,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status de Bateria / Carregamento
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                                contentDescription = null,
                                tint = if (isCharging) accentColor else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCharging) "Carregando • $batteryPercentage%" else "Bateria • $batteryPercentage%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCharging) accentColor else TextPrimary
                            )
                        }

                        if (!weatherTemp.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$weatherTemp${if (!weatherCondition.isNullOrBlank()) " • $weatherCondition" else ""}",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Layout Retrato (Vertical)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Relógio Superior
                    Text(
                        text = formattedTime,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        color = TextPrimary,
                        letterSpacing = (-1.5).sp
                    )

                    Text(
                        text = formattedDate,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bateria / Carregamento
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                            contentDescription = null,
                            tint = if (isCharging) accentColor else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCharging) "Carregando • $batteryPercentage%" else "$batteryPercentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCharging) accentColor else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Foto Central Ampliada
                    Box(
                        modifier = Modifier
                            .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                            .fillMaxWidth(0.82f)
                            .aspectRatio(1f)
                            .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black, spotColor = Color.Black)
                            .clip(RoundedCornerShape(32.dp))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f))
                                    )
                                ),
                                RoundedCornerShape(32.dp)
                            )
                            .background(Color(0xFF141419)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Foto em modo repouso",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Toque em qualquer lugar para sair",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}
