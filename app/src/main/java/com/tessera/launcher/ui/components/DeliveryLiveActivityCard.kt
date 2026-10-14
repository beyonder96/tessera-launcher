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
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Restaurant
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
import com.tessera.launcher.data.model.DeliveryActivityDetails
import com.tessera.launcher.data.model.DeliveryStage
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Card Expandido Especializado em Pedidos e Delivery (iFood, Rappi, Zé Delivery, Uber Eats).
 * Mantém perfeita consistência visual com o padrão Live Activity:
 * - Cabeçalho com nome do app e logotipo da loja/restaurante
 * - Informações de ETA em destaque e status descritivo
 * - Código PIN de entrega em destaque monoespaçado (estilo placa de veículo)
 * - Linha de progresso de 4 etapas (Confirmado, Preparando, Em Rota, Entregue)
 * - Botões de ações rápidas se fornecidas pelo app
 */
@Composable
fun DeliveryLiveActivityCard(
    deliveryDetails: DeliveryActivityDetails,
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
        isLightMode -> Color(0xFF0F172A)
        else -> Color(0xFFF8FAFC)
    }

    val secondaryTextColor = when {
        isLightMode -> Color(0xFF64748B)
        else -> Color(0xFF94A3B8)
    }

    val trackColor = when {
        isLightMode -> Color(0xFFCBD5E1)
        else -> Color(0xFF334155)
    }

    val dynamicAccent = when {
        accentColor != Color.White && accentColor != Color.Black -> accentColor
        deliveryDetails.appName.contains("ifood", ignoreCase = true) -> Color(0xFFEA1D2C)
        deliveryDetails.appName.contains("rappi", ignoreCase = true) -> Color(0xFFFF441F)
        deliveryDetails.appName.contains("ze", ignoreCase = true) -> Color(0xFFFFB300)
        else -> Color(0xFFEA1D2C)
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
            // 1. CABEÇALHO: App Name & Logotipo da Loja
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = deliveryDetails.appName,
                        color = primaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dynamicAccent)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val storeLogo = deliveryDetails.storeLogo
                    if (storeLogo != null) {
                        Image(
                            bitmap = storeLogo.asImageBitmap(),
                            contentDescription = "Logotipo da Loja",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, dynamicAccent.copy(alpha = 0.4f), CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(dynamicAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restaurant,
                                contentDescription = null,
                                tint = dynamicAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1E293B))
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
            // 2. CORPO PRINCIPAL: ETA e Código PIN / Loja
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Coluna Esquerda: Tempo de Entrega e Status
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (deliveryDetails.etaMinutes != null) {
                        val etaAnnotated = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = dynamicAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            ) {
                                append("${deliveryDetails.etaMinutes} min ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = primaryTextColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            ) {
                                append("até a entrega")
                            }
                        }
                        Text(
                            text = etaAnnotated,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = deliveryDetails.etaText ?: "A caminho",
                            color = primaryTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = deliveryDetails.statusMessage ?: "${deliveryDetails.storeName} está preparando",
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Coluna Direita: Código PIN em destaque ou Nome da Loja
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (!deliveryDetails.confirmationPin.isNullOrBlank()) {
                        Text(
                            text = "PIN: ${deliveryDetails.confirmationPin}",
                            color = primaryTextColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = deliveryDetails.storeName,
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = deliveryDetails.storeName,
                            color = primaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Restaurante",
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // ==========================================
            // 3. BARRA DE 4 ETAPAS (Confirmado, Preparando, Em rota, Entregue)
            // ==========================================
            DeliveryProgressBar(
                stage = deliveryDetails.stage,
                accentColor = dynamicAccent,
                trackColor = trackColor,
                textColor = secondaryTextColor,
                modifier = Modifier.padding(top = 2.dp)
            )

            // ==========================================
            // 4. AÇÕES RÁPIDAS DA NOTIFICAÇÃO (se houver)
            // ==========================================
            if (deliveryDetails.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    deliveryDetails.actions.forEach { action ->
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
                                .background(if (isLightMode) Color(0xFFF1F5F9) else Color(0xFF1E293B))
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
