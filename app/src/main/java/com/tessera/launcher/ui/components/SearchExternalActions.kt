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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayArrow
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
import com.tessera.launcher.ui.theme.DarkSurfaceBorder
import com.tessera.launcher.ui.theme.DarkSurfaceVariant
import com.tessera.launcher.ui.theme.LiquidGlassBackground
import com.tessera.launcher.ui.theme.LiquidGlassBorderBrush
import com.tessera.launcher.ui.theme.PillShape
import com.tessera.launcher.ui.theme.TextPrimary

@Composable
fun SearchExternalActions(
    query: String,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExternalSearchChip(
            label = "Google",
            icon = Icons.Outlined.Language,
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier.weight(1f),
            onClick = { launchGoogleSearch(context, query) }
        )

        ExternalSearchChip(
            label = "Play Store",
            icon = Icons.Outlined.ShoppingBag,
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier.weight(1.1f),
            onClick = { launchPlayStoreSearch(context, query) }
        )

        ExternalSearchChip(
            label = "YouTube",
            icon = Icons.Outlined.PlayArrow,
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier.weight(1f),
            onClick = { launchYouTubeSearch(context, query) }
        )
    }
}

@Composable
private fun ExternalSearchChip(
    label: String,
    icon: ImageVector,
    isLiquidGlass: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val border = if (isLiquidGlass) {
        BorderStroke(1.dp, LiquidGlassBorderBrush)
    } else {
        BorderStroke(1.dp, DarkSurfaceBorder)
    }
    val background = if (isLiquidGlass) LiquidGlassBackground else DarkSurfaceVariant

    Box(
        modifier = modifier
            .height(38.dp)
            .border(border, PillShape)
            .clip(PillShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
