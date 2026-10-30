package com.tessera.launcher.ui.components

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.GeminiCyan
import com.tessera.launcher.ui.theme.GeminiPurple
import java.util.Locale

@Composable
fun SearchExternalActions(
    query: String,
    inAppSearchPackages: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false,
    provider: String = "GROQ",
    onAskAi: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    if (query.isBlank()) return

    val cardBg = when {
        isLightMode -> Color(0xFFEFECE2)
        isAmoledMode -> Color(0xFF10121A)
        else -> Color(0xFF161822)
    }
    val borderColor = when {
        isLightMode -> Color(0xFFDDD8CB)
        else -> Color(0x25FFFFFF)
    }
    val textPrimary = if (isLightMode) Color(0xFF1C1917) else Color(0xFFF7F4EB)
    val textSecondary = if (isLightMode) Color(0xFF78716C) else Color(0xFFA8A29E)

    // Animação contínua da aura de IA para o Hero Card
    val infiniteTransition = rememberInfiniteTransition(label = "ai_hero_glow")
    val glowAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_glow_angle"
    )

    val rad = Math.toRadians(glowAngle.toDouble())
    val cosVal = Math.cos(rad).toFloat()
    val sinVal = Math.sin(rad).toFloat()

    val aiBorderBrush = remember(glowAngle, isLightMode) {
        Brush.linearGradient(
            colors = if (isLightMode) {
                listOf(
                    Color(0xFF3B82F6).copy(alpha = 0.55f),
                    Color(0xFF8B5CF6).copy(alpha = 0.55f),
                    Color(0xFF06B6D4).copy(alpha = 0.65f),
                    Color(0xFF3B82F6).copy(alpha = 0.55f)
                )
            } else {
                listOf(
                    Color(0xFF60A5FA).copy(alpha = 0.65f),
                    Color(0xFFA78BFA).copy(alpha = 0.65f),
                    Color(0xFF22D3EE).copy(alpha = 0.75f),
                    Color(0xFF60A5FA).copy(alpha = 0.65f)
                )
            },
            start = androidx.compose.ui.geometry.Offset(100f * (1f - cosVal), 100f * (1f - sinVal)),
            end = androidx.compose.ui.geometry.Offset(600f * (1f + cosVal), 600f * (1f + sinVal))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Hero Card de IA com Glow Especular
        if (onAskAi != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.2.dp, aiBorderBrush, RoundedCornerShape(20.dp))
                    .tesseraPressable(pressedScale = 0.98f, onClick = { onAskAi(query) })
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                GeminiCyan.copy(alpha = if (isLightMode) 0.25f else 0.35f),
                                                GeminiPurple.copy(alpha = if (isLightMode) 0.25f else 0.35f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isLightMode) Color(0xFF4338CA) else GeminiCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = if (provider.equals("GEMINI", true)) "RESPOSTA RÁPIDA • GEMINI FLASH" else "RESPOSTA RÁPIDA • GROQ IA",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = if (isLightMode) Color(0xFF4338CA) else GeminiCyan
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLightMode) Color(0x181C1917) else Color(0x20FFFFFF))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Perguntar ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                    }

                    Text(
                        text = "Consultar IA sobre \"$query\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Respostas instantâneas, sínteses e ideias sem sair da launcher.",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        // 2. Barra de Ações Rápidas em Chips Bento Refinados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip Google Busca Web
            SearchBentoChip(
                icon = Icons.Outlined.Language,
                iconTint = if (isLightMode) Color(0xFF2563EB) else Color(0xFF60A5FA),
                title = "Google",
                subtitle = "Web",
                cardBg = cardBg,
                borderColor = borderColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f),
                onClick = { launchGoogleSearch(context, query) }
            )

            // Chip YouTube
            if (inAppSearchPackages.isEmpty() || inAppSearchPackages.contains("com.google.android.youtube")) {
                SearchBentoChip(
                    icon = Icons.Outlined.PlayArrow,
                    iconTint = Color(0xFFFF3333),
                    title = "YouTube",
                    subtitle = "Vídeo",
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { launchYouTubeSearch(context, query) }
                )
            }

            // Chip Play Store
            if (inAppSearchPackages.isEmpty() || inAppSearchPackages.contains("com.android.vending")) {
                SearchBentoChip(
                    icon = Icons.Outlined.ShoppingBag,
                    iconTint = if (isLightMode) Color(0xFF059669) else Color(0xFF34D399),
                    title = "Play Store",
                    subtitle = "Apps",
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { launchPlayStoreSearch(context, query) }
                )
            }

            // Chip Maps (se ativo)
            if (inAppSearchPackages.contains("com.google.android.apps.maps")) {
                SearchBentoChip(
                    icon = Icons.Outlined.Place,
                    iconTint = Color(0xFFEA4335),
                    title = "Maps",
                    subtitle = "Locais",
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { launchMapsSearch(context, query) }
                )
            }
        }
    }
}

@Composable
private fun SearchBentoChip(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    cardBg: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
            .tesseraPressable(pressedScale = 0.93f, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(17.dp)
            )

            Column {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun launchGoogleSearch(context: Context, query: String) {
    val browserIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra(SearchManager.QUERY, query)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(browserIntent)
    }.onFailure {
        runCatching {
            context.startActivity(searchIntent)
        }
    }
}

private fun launchMapsSearch(context: Context, query: String) {
    val mapsIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    ).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(mapsIntent)
    }.onFailure {
        runCatching {
            context.startActivity(webIntent)
        }
    }
}

private fun launchPlayStoreSearch(context: Context, query: String) {
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://search?q=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/search?q=${Uri.encode(query)}&c=apps")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(marketIntent)
    }.onFailure {
        runCatching {
            context.startActivity(webIntent)
        }
    }
}

private fun launchYouTubeSearch(context: Context, query: String) {
    val ytIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("vnd.youtube://results?search_query=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(ytIntent)
    }.onFailure {
        runCatching {
            context.startActivity(webIntent)
        }
    }
}

private fun launchWhatsAppSearch(context: Context, query: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, query)
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(sendIntent)
    }
}
