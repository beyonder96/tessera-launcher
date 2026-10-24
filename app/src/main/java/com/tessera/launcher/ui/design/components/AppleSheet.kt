package com.tessera.launcher.ui.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.appleSpring

@Composable
fun AppleSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val motion = LocalAppleMotion.current
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(visible) {
        if (visible) {
            Taptic.snap(context)
        }
    }

    if (visible) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Scrim escurecido de fundo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = appleSpring(response = 0.44f, bounce = 0.08f)
                ) + fadeIn(animationSpec = motion.fadeQuick),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = appleSpring(response = 0.38f, bounce = 0.0f)
                ) + fadeOut(animationSpec = motion.fadeQuick)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(ContinuousShape(38.dp))
                        .background(colors.secondaryGrouped)
                        .padding(bottom = navBarsPadding + 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Grabber clássico iOS
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(5.dp)
                            .clip(AppleShapes.capsule)
                            .background(colors.quaternaryLabel)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    content()
                }
            }
        }
    }
}
