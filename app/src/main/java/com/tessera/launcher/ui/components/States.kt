package com.tessera.launcher.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.ui.theme.tokens.Tessera

@Composable
fun AppListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 8
) {
    val palette = Tessera.palette
    val shapes = Tessera.shapes
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(palette.surface, palette.surfaceSubtle, palette.surface),
        start = androidx.compose.ui.geometry.Offset(translateAnim - 500f, translateAnim - 500f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(shapes.small)
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(14.dp)
                            .clip(shapes.small)
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(10.dp)
                            .clip(shapes.small)
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
fun AppListEmptyState(
    query: String,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    isAmoledMode: Boolean = false,
    provider: String = "GROQ",
    isAiUnlocked: Boolean = false,
    onAskAi: ((String) -> Unit)? = null
) {
    val palette = Tessera.palette
    val shapes = Tessera.shapes

    val isAiMode = isAiUnlocked && (
        query.startsWith("@ai", ignoreCase = true) ||
            query.startsWith("@gemini", ignoreCase = true) ||
            query.startsWith("@groq", ignoreCase = true)
    )

    if (isAiMode) {
        SearchExternalActions(
            query = query,
            isLiquidGlass = false,
            isLightMode = isLightMode,
            isAmoledMode = isAmoledMode,
            provider = provider,
            isAiUnlocked = isAiUnlocked,
            onAskAi = onAskAi,
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        color = palette.surface,
        shape = shapes.large,
        border = BorderStroke(0.5.dp, palette.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nenhum aplicativo encontrado",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (query.isNotBlank()) "Sem correspondência para \"$query\"" else "Nenhum aplicativo inicializável",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )

            if (query.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                SearchExternalActions(
                    query = query,
                    isLiquidGlass = false,
                    isLightMode = isLightMode,
                    isAmoledMode = isAmoledMode,
                    provider = provider,
                    isAiUnlocked = isAiUnlocked,
                    onAskAi = onAskAi,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AppListErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = Tessera.palette
    val shapes = Tessera.shapes
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        color = palette.surface,
        shape = shapes.card,
        border = BorderStroke(1.dp, palette.surfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Falha ao listar aplicativos",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = shapes.pill,
                color = Color.Transparent,
                border = BorderStroke(1.dp, palette.surfaceBorder),
                modifier = Modifier.clickable(onClick = onRetry)
            ) {
                Text(
                    text = "Recarregar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}
