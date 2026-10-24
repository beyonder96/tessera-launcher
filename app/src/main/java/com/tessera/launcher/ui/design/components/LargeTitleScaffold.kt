package com.tessera.launcher.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

/**
 * Scaffold padrão Apple HIG com título grande (Large Title 34sp) que colapsa
 * suavemente para título compacto centralizado (17sp) na barra de navegação com Liquid Glass.
 */
@Composable
fun LargeTitleScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    searchBar: (@Composable () -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = statusBarPadding + 54.dp,
                bottom = 36.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // Large Title Row
            item(key = "large_title_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = title,
                        style = typography.largeTitle,
                        color = colors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (searchBar != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        searchBar()
                    }
                }
            }

            content()
        }

        // Barra Superior de Vidro Compacta (Aparece ao Rolar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarPadding + 52.dp)
                .then(
                    if (isScrolled) {
                        Modifier.liquidGlass(shape = AppleShapes.small, style = GlassStyle.Thin)
                    } else {
                        Modifier
                    }
                )
                .padding(top = statusBarPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(0.2f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    navigationIcon?.invoke()
                }

                Box(
                    modifier = Modifier.weight(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isScrolled,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = title,
                            style = typography.headline,
                            color = colors.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(0.2f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    actions?.invoke()
                }
            }
        }
    }
}
