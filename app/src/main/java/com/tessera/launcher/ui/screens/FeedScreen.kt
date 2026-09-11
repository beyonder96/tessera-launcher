package com.tessera.launcher.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tessera.launcher.data.model.FeedPost
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.ui.state.FeedState
import com.tessera.launcher.ui.components.FeedPostCard
import com.tessera.launcher.ui.theme.*

@Composable
fun FeedScreen(
    feedState: FeedState,
    activeSource: FeedSource?,
    enabledSources: Set<FeedSource>,
    isLightMode: Boolean,
    onSourceSelected: (FeedSource) -> Unit,
    onPostClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryTextColor = if (isLightMode) LightTextPrimary else TextPrimary
    val backgroundColor = if (isLightMode) Color(0xFFF7F7F7) else Color(0xFF000000)
    
    var localSelectedSource by remember { mutableStateOf<FeedSource?>(activeSource) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = primaryTextColor
                )
            }
            Text(
                text = "Feed",
                color = primaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Recarregar",
                    tint = primaryTextColor
                )
            }
        }

        if (enabledSources.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SourceChip(
                        name = "Tudo",
                        isSelected = localSelectedSource == null,
                        isLightMode = isLightMode,
                        onClick = { localSelectedSource = null }
                    )
                }
                items(enabledSources.toList()) { source ->
                    SourceChip(
                        name = source.displayName,
                        isSelected = localSelectedSource == source,
                        isLightMode = isLightMode,
                        onClick = { 
                            localSelectedSource = source
                            onSourceSelected(source)
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (feedState) {
                is FeedState.Loading -> FeedSkeleton(isLightMode = isLightMode)
                is FeedState.Empty -> FeedEmptyState(isLightMode = isLightMode)
                is FeedState.Error -> FeedErrorState(
                    message = feedState.message,
                    isLightMode = isLightMode,
                    onRetry = onRefresh
                )
                is FeedState.Success -> {
                    val posts = feedState.posts
                    val filteredPosts = if (localSelectedSource == null) {
                        posts
                    } else {
                        posts.filter { it.source == localSelectedSource }
                    }

                    if (filteredPosts.isEmpty()) {
                        FeedEmptyState(isLightMode = isLightMode)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPosts, key = { it.id }) { post ->
                                FeedPostCard(
                                    post = post,
                                    isLightMode = isLightMode,
                                    onPostClick = onPostClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChip(
    name: String,
    isSelected: Boolean,
    isLightMode: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        if (isLightMode) LightCardBorder else DarkSurfaceVariant
    } else {
        Color.Transparent
    }
    
    val textColor = if (isSelected) {
        if (isLightMode) LightTextPrimary else TextPrimary
    } else {
        if (isLightMode) LightTextSecondary else TextSecondary
    }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = PillShape,
        color = backgroundColor,
        border = if (!isSelected) BorderStroke(1.dp, if (isLightMode) LightCardBorder else DarkSurfaceBorder) else null
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FeedSkeleton(isLightMode: Boolean) {
    val skeletonBase = if (isLightMode) Color(0xFFE0E0E0) else SkeletonBase
    val skeletonHighlight = if (isLightMode) Color(0xFFF5F5F5) else SkeletonHighlight

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(skeletonBase, skeletonHighlight, skeletonBase),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = CardShape,
                color = if (isLightMode) LightCardBackground else DarkSurface,
                border = BorderStroke(1.dp, if (isLightMode) LightCardBorder else DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(width = 80.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(18.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        Box(modifier = Modifier.size(width = 40.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.size(width = 40.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedEmptyState(isLightMode: Boolean) {
    val primaryTextColor = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLightMode) LightTextSecondary else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.RssFeed,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = secondaryTextColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum post encontrado",
            color = primaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Verifique sua conexão ou adicione subreddits nas configurações.",
            color = secondaryTextColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeedErrorState(
    message: String?,
    isLightMode: Boolean,
    onRetry: () -> Unit
) {
    val primaryTextColor = if (isLightMode) LightTextPrimary else TextPrimary
    val secondaryTextColor = if (isLightMode) LightTextSecondary else TextSecondary
    val borderColor = if (isLightMode) LightCardBorder else DarkSurfaceBorder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = secondaryTextColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Falha ao carregar o feed",
            color = primaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message ?: "Ocorreu um erro desconhecido.",
            color = secondaryTextColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            modifier = Modifier.clickable(onClick = onRetry),
            shape = PillShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Text(
                text = "Recarregar",
                color = primaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}
