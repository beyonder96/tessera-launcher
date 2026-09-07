package com.tessera.launcher.ui.components

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.DarkSurface
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary
import com.tessera.launcher.ui.theme.TextSecondary

@Composable
fun SearchExternalActions(
    query: String,
    inAppSearchPackages: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    val context = LocalContext.current
    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurface

    // Pílula unificada conforme Screenshot media_1788774600763.png
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(52.dp)
            .border(border, PillShape)
            .clip(PillShape)
            .background(background)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado Esquerdo: Pesquisar "$query" no Google
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { launchGoogleSearch(context, query) }
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = "Google",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pesquisar \"$query\"",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Lado Direito: Ações rápidas de Apps (YouTube, Play Store, etc.)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // YouTube (se habilitado ou padrão)
                if (inAppSearchPackages.isEmpty() || inAppSearchPackages.contains("com.google.android.youtube")) {
                    AppSearchQuickIcon(
                        icon = Icons.Outlined.PlayArrow,
                        description = "YouTube",
                        onClick = { launchYouTubeSearch(context, query) }
                    )
                }

                // Play Store
                if (inAppSearchPackages.isEmpty() || inAppSearchPackages.contains("com.android.vending")) {
                    AppSearchQuickIcon(
                        icon = Icons.Outlined.ShoppingBag,
                        description = "Play Store",
                        onClick = { launchPlayStoreSearch(context, query) }
                    )
                }

                // WhatsApp
                if (inAppSearchPackages.contains("com.whatsapp")) {
                    AppSearchQuickIcon(
                        icon = Icons.Outlined.Send,
                        description = "WhatsApp",
                        onClick = { launchWhatsAppSearch(context, query) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSearchQuickIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E26))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = TextSecondary,
            modifier = Modifier.size(17.dp)
        )
    }
}

private fun launchGoogleSearch(context: Context, query: String) {
    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra(SearchManager.QUERY, query)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val browserIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(searchIntent)
    }.onFailure {
        runCatching {
            context.startActivity(browserIntent)
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
