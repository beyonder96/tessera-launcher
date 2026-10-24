package com.tessera.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.data.model.FeedPost
import com.tessera.launcher.data.model.FeedSource
import com.tessera.launcher.ui.components.FeedPostCard
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.state.FeedState
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feed Editorial Oficial do Tessera 3.0 — Padrão Apple News Today.
 * Data do dia monumental, Large Title "Hoje", chips translúcidos de tópicos
 * e cartões com tipografia editorial refinada.
 */
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
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    var localSelectedSource by remember { mutableStateOf<FeedSource?>(activeSource) }

    val todayFormatted = remember {
        val now = Date()
        val format = SimpleDateFormat("EEEE, d 'DE' MMMM", Locale.forLanguageTag("pt-BR"))
        format.format(now).uppercase()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Barra Superior com Navegação e Ações
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    Taptic.lightImpact(context)
                    onClose()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Voltar",
                    tint = colors.label
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    Taptic.lightImpact(context)
                    onRefresh()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Recarregar",
                    tint = colors.label
                )
            }
        }

        // Cabeçalho Editorial Estilo Apple News Today
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = todayFormatted,
                style = typography.caption1.copy(fontWeight = FontWeight.Bold),
                color = colors.secondaryLabel
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hoje",
                style = typography.largeTitle.copy(fontWeight = FontWeight.Bold),
                color = colors.label
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chips de Categorias e Fontes em Liquid Glass
        if (enabledSources.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AppleSourceChip(
                        name = "Tudo",
                        isSelected = localSelectedSource == null,
                        onClick = {
                            Taptic.selection(context)
                            localSelectedSource = null
                        }
                    )
                }
                items(enabledSources.toList()) { source ->
                    AppleSourceChip(
                        name = source.chipName,
                        isSelected = localSelectedSource == source,
                        onClick = {
                            Taptic.selection(context)
                            localSelectedSource = source
                            onSourceSelected(source)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Conteúdo Principal do Feed
        Box(modifier = Modifier.weight(1f)) {
            when (feedState) {
                is FeedState.Loading -> AppleFeedSkeleton()
                is FeedState.Empty -> AppleFeedEmptyState()
                is FeedState.Error -> AppleFeedErrorState(
                    message = feedState.message,
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
                        AppleFeedEmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredPosts, key = { it.id }) { post ->
                                FeedPostCard(
                                    post = post,
                                    isLightMode = !colors.isDark,
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
private fun AppleSourceChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) colors.label else colors.secondaryFill)
            .liquidGlass(style = GlassStyle.Thin, shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = typography.subheadline.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) colors.systemBackground else colors.label
        )
    }
}

@Composable
private fun AppleFeedSkeleton() {
    val colors = LocalAppleColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(ContinuousShape(18.dp))
                    .background(colors.secondaryFill)
            )
        }
    }
}

@Composable
private fun AppleFeedEmptyState() {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.RssFeed,
                contentDescription = null,
                tint = colors.tertiaryLabel,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nenhuma notícia no momento",
                style = typography.subheadline,
                color = colors.secondaryLabel
            )
        }
    }
}

@Composable
private fun AppleFeedErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFFF3B30),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Falha ao carregar notícias",
                style = typography.headline,
                color = colors.label
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = typography.footnote,
                color = colors.secondaryLabel,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            CapsuleButton(
                text = "Tentar Novamente",
                onClick = onRetry
            )
        }
    }
}
