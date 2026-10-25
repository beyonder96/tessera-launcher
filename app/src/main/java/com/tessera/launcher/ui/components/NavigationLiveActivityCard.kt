package com.tessera.launcher.ui.components

import android.app.PendingIntent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.ManeuverType
import com.tessera.launcher.data.model.NavigationActivityDetails
import com.tessera.launcher.ui.theme.tokens.Tessera
import com.tessera.launcher.ui.theme.tokens.tesseraPressFeedback

/**
 * Card Expandido Especializado em Navegação GPS Curva a Curva (Google Maps e Waze).
 * Apresenta:
 * - Ícone direcional de manobra em destaque (curva à esquerda/direita, seguir em frente, etc.)
 * - Distância imediata até a manobra e nome da próxima via em alta legibilidade
 * - Métricas de viagem consolidadas: tempo restante, distância total e previsão de chegada
 * - Barra de progresso de trajeto
 * - Ações rápidas (Encerrar navegação, Mutar áudio)
 */
@Composable
fun NavigationLiveActivityCard(
    navigationDetails: NavigationActivityDetails,
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

    val navAccent = when {
        accentColor != Color.White && accentColor != Color.Black -> accentColor
        navigationDetails.appName.contains("waze", ignoreCase = true) -> Color(0xFF33CCFF)
        else -> Color(0xFF10B981) // Verde esmeralda GPS Google Maps
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
            // 1. CABEÇALHO: App Name & Indicador
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = navigationDetails.appName,
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
                            .background(navAccent)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(navAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Navigation,
                            contentDescription = null,
                            tint = navAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

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
            // 2. CORPO PRINCIPAL: Ícone de Manobra e Próxima Via
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone de Manobra em destaque
                val (maneuverIcon, iconDesc) = getManeuverIcon(navigationDetails.maneuver)

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(navAccent.copy(alpha = 0.20f))
                        .border(1.dp, navAccent.copy(alpha = 0.40f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = maneuverIcon,
                        contentDescription = iconDesc,
                        tint = navAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Distância da manobra imediata (ex: "em 200 m")
                    val distText = navigationDetails.distanceToManeuver ?: "Próxima ação"
                    Text(
                        text = distText,
                        color = navAccent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Nome da Próxima Via (ex: "Av. Brigadeiro Faria Lima")
                    val streetName = navigationDetails.nextStreetName
                        ?: navigationDetails.maneuverText
                        ?: "Siga a rota indicada"

                    Text(
                        text = streetName,
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ==========================================
            // 3. MÉTRICAS CONSOLIDADAS: ETA • Distância • Chegada
            // ==========================================
            val metrics = buildList {
                navigationDetails.etaRemaining?.let { add(it) }
                navigationDetails.distanceRemaining?.let { add(it) }
                navigationDetails.estimatedArrival?.let { add("Chegada às $it") }
            }

            if (metrics.isNotEmpty()) {
                Text(
                    text = metrics.joinToString(" • "),
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ==========================================
            // 4. BARRA DE TRAJETO DA ROTA
            // ==========================================
            RideRouteProgressBar(
                progressPercent = navigationDetails.progressPercent,
                accentColor = navAccent,
                trackColor = trackColor,
                isArrivedOrNear = navigationDetails.maneuver == ManeuverType.DESTINATION,
                modifier = Modifier.padding(top = 2.dp)
            )

            // ==========================================
            // 5. AÇÕES RÁPIDAS DA NAVEGAÇÃO
            // ==========================================
            if (navigationDetails.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navigationDetails.actions.forEach { action ->
                        val isExit = action.title.contains("sair", ignoreCase = true) ||
                                action.title.contains("fechar", ignoreCase = true) ||
                                action.title.contains("cancelar", ignoreCase = true) ||
                                action.title.contains("exit", ignoreCase = true)
                        val isMute = action.title.contains("som", ignoreCase = true) ||
                                action.title.contains("mutar", ignoreCase = true) ||
                                action.title.contains("áudio", ignoreCase = true)

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
                                    isExit -> Icons.Outlined.Close
                                    isMute -> Icons.AutoMirrored.Outlined.VolumeOff
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

private fun getManeuverIcon(maneuver: ManeuverType): Pair<ImageVector, String> {
    return when (maneuver) {
        ManeuverType.TURN_LEFT, ManeuverType.TURN_SLIGHT_LEFT, ManeuverType.TURN_SHARP_LEFT ->
            Pair(Icons.AutoMirrored.Outlined.ArrowBack, "Virar à esquerda")
        ManeuverType.TURN_RIGHT, ManeuverType.TURN_SLIGHT_RIGHT, ManeuverType.TURN_SHARP_RIGHT ->
            Pair(Icons.AutoMirrored.Outlined.ArrowForward, "Virar à direita")
        ManeuverType.ROUNDABOUT ->
            Pair(Icons.AutoMirrored.Outlined.RotateRight, "Rotatória")
        ManeuverType.DESTINATION ->
            Pair(Icons.Outlined.Flag, "Destino final")
        else ->
            Pair(Icons.Outlined.ArrowUpward, "Siga em frente")
    }
}
