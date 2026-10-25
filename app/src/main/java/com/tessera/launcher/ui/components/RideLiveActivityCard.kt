package com.tessera.launcher.ui.components

import android.app.PendingIntent
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.RideActivityDetails
import com.tessera.launcher.data.model.RideStage
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Card Expandido Especializado em Corridas e Mobilidade Urbana (Uber, 99, etc.).
 * Baseado no padrão de excelência visual At-a-Glance / Live Activity:
 * - Cabeçalho com logo do aplicativo e fotos do motorista e veículo
 * - Informações de ETA em destaque tipográfico e status descritivo
 * - Placa marcante em alta visibilidade e modelo do veículo
 * - Barra de trajeto com percurso percorrido, restante e posição do carro
 * - Botões de ações rápidas se fornecidas pelo app (Ligar, Mensagem, etc.)
 */
@Composable
fun RideLiveActivityCard(
    rideDetails: RideActivityDetails,
    isLiquidGlass: Boolean,
    isAmoledMode: Boolean,
    isLightMode: Boolean,
    accentColor: Color,
    onCardClick: () -> Unit,
    onActionClick: (PendingIntent?) -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground = when {
        isAmoledMode -> Color(0xFF000000)
        isLiquidGlass -> Tessera.palette.surface.copy(alpha = 0.90f)
        isLightMode -> Color(0xFFFFFFFF)
        else -> Tessera.palette.surface
    }

    val cardBorder = when {
        isAmoledMode -> Color(0xFF27272A)
        isLightMode -> Color(0xFFE2E8F0)
        else -> Tessera.palette.surfaceBorder
    }

    val primaryTextColor = when {
        isLightMode -> Color(0xFF1C1917)
        else -> Color(0xFFF8FAFC)
    }

    val secondaryTextColor = when {
        isLightMode -> Color(0xFF57534E)
        else -> Color(0xFF94A3B8)
    }

    val trackColor = when {
        isLightMode -> Color(0xFFDDD8CB)
        else -> Color(0xFF334155)
    }

    val dynamicAccent = when {
        accentColor != Color.White && accentColor != Color.Black -> accentColor
        rideDetails.appName.equals("99", ignoreCase = true) -> Color(0xFFFFB300)
        else -> Color(0xFF2563EB) // Azul moderno Uber / Mobility
    }

    val cardShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .widthIn(min = 320.dp, max = 360.dp)
            .clip(cardShape)
            .background(cardBackground)
            .border(width = 0.8.dp, color = cardBorder, shape = cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCardClick
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // 1. CABEÇALHO: App Name & Fotos (Motorista / Veículo)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Nome do App (Uber, 99)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rideDetails.appName,
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Indicador de status pulsante
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dynamicAccent)
                    )
                }

                // Canto Superior Direito: Avatar do motorista e/ou imagem do carro
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val driverAvatar = rideDetails.driverAvatar
                    val vehicleImage = rideDetails.vehicleImage

                    if (driverAvatar != null && vehicleImage != null) {
                        // Stack sobreposta de avatar + carro
                        Box(contentAlignment = Alignment.CenterEnd) {
                            Image(
                                bitmap = vehicleImage.asImageBitmap(),
                                contentDescription = "Veículo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(width = 50.dp, height = 32.dp)
                                    .padding(end = 12.dp)
                            )
                            Image(
                                bitmap = driverAvatar.asImageBitmap(),
                                contentDescription = "Motorista",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, cardBackground, CircleShape)
                            )
                        }
                    } else if (driverAvatar != null) {
                        Image(
                            bitmap = driverAvatar.asImageBitmap(),
                            contentDescription = "Motorista",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, dynamicAccent.copy(alpha = 0.5f), CircleShape)
                        )
                    } else {
                        // Ícone elegante de carro de transporte como fallback
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(dynamicAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsCar,
                                contentDescription = "Corrida",
                                tint = dynamicAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botão de recolher card
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isLightMode) Color(0xFFE8E4D8) else Color(0xFF1E293B))
                            .tesseraPressFeedback()
                            .clickable { onCollapseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "Recolher cápsula",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 2. CORPO PRINCIPAL: ETA e Placa do Carro
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Coluna Esquerda: ETA e Mensagem de Status
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (rideDetails.etaMinutes != null) {
                        val etaAnnotated = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = dynamicAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            ) {
                                append("${rideDetails.etaMinutes} min ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = primaryTextColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            ) {
                                val label = when (rideDetails.stage) {
                                    RideStage.IN_TRIP -> "até o destino"
                                    RideStage.ARRIVED -> "no local"
                                    else -> "para o embarque"
                                }
                                append(label)
                            }
                        }
                        Text(
                            text = etaAnnotated,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = rideDetails.etaText ?: "A caminho",
                            color = primaryTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Mensagem de Status (ex: "Anderson is arriving soon")
                    Text(
                        text = rideDetails.statusMessage ?: "${rideDetails.driverName ?: "Motorista"} está a caminho",
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Coluna Direita: Placa e Modelo do Carro (se disponíveis)
                if (!rideDetails.licensePlate.isNullOrBlank() || !rideDetails.vehicleModel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (!rideDetails.licensePlate.isNullOrBlank()) {
                            Text(
                                text = rideDetails.licensePlate,
                                color = primaryTextColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.End
                            )
                        }

                        if (!rideDetails.vehicleModel.isNullOrBlank()) {
                            Text(
                                text = rideDetails.vehicleModel,
                                color = secondaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. BARRA DE TRAJETO E PROGRESSO
            // ==========================================
            RideRouteProgressBar(
                progressPercent = rideDetails.progressPercent,
                accentColor = dynamicAccent,
                trackColor = trackColor,
                isArrivedOrNear = rideDetails.stage == RideStage.ARRIVED || (rideDetails.etaMinutes != null && rideDetails.etaMinutes <= 1),
                modifier = Modifier.padding(top = 2.dp)
            )

            // ==========================================
            // 4. AÇÕES RÁPIDAS DA NOTIFICAÇÃO (se houver)
            // ==========================================
            if (rideDetails.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rideDetails.actions.forEach { action ->
                        val isContactAction = action.title.contains("ligar", ignoreCase = true) ||
                                action.title.contains("chamar", ignoreCase = true) ||
                                action.title.contains("call", ignoreCase = true)
                        val isChatAction = action.title.contains("mensagem", ignoreCase = true) ||
                                action.title.contains("chat", ignoreCase = true) ||
                                action.title.contains("msg", ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isLightMode) Color(0xFFE8E4D8) else Color(0xFF1E293B))
                                .border(
                                    width = 0.6.dp,
                                    color = if (isLightMode) Color(0xFFE2E8F0) else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .tesseraPressFeedback()
                                .clickable {
                                    onActionClick(action.intent)
                                }
                                .padding(vertical = 7.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val actionIcon = when {
                                    isContactAction -> Icons.Outlined.Call
                                    isChatAction -> Icons.Outlined.ChatBubbleOutline
                                    else -> Icons.AutoMirrored.Outlined.OpenInNew
                                }

                                Icon(
                                    imageVector = actionIcon,
                                    contentDescription = null,
                                    tint = primaryTextColor,
                                    modifier = Modifier.size(13.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = action.title,
                                    color = primaryTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
