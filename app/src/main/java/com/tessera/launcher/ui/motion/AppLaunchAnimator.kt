package com.tessera.launcher.ui.motion

import android.app.ActivityOptions
import android.graphics.Rect
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.theme.tokens.rememberTesseraHaptics
import com.tessera.launcher.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

/**
 * Holder mutável não-observável para coordenadas do ícone, evitando recomposições ao posicionar.
 */
class IconBoundsHolder {
    var rootRect: Rect? = null
}

/**
 * Captura os bounds do ícone no espaço da View raiz para transições `makeClipRevealAnimation`.
 */
fun Modifier.captureIconBounds(holder: IconBoundsHolder): Modifier = this.onGloballyPositioned { coords ->
    if (!coords.isAttached) return@onGloballyPositioned
    val b = coords.boundsInRoot()
    if (b.width > 0f && b.height > 0f) {
        val left = b.left.roundToInt()
        val top = b.top.roundToInt()
        val right = b.right.roundToInt()
        val bottom = b.bottom.roundToInt()
        val existing = holder.rootRect
        if (existing == null) {
            holder.rootRect = Rect(left, top, right, bottom)
        } else {
            existing.set(left, top, right, bottom)
        }
    }
}

private fun buildLaunchOptionsAndScreenBounds(
    view: View,
    rootBounds: Rect?
): Pair<android.os.Bundle?, Rect?> {
    if (rootBounds == null || rootBounds.width() <= 0 || rootBounds.height() <= 0) {
        return null to null
    }
    val optionsBundle = runCatching {
        ActivityOptions.makeClipRevealAnimation(
            view,
            rootBounds.left,
            rootBounds.top,
            rootBounds.width(),
            rootBounds.height()
        ).toBundle()
    }.getOrElse {
        runCatching {
            ActivityOptions.makeScaleUpAnimation(
                view,
                rootBounds.left,
                rootBounds.top,
                rootBounds.width(),
                rootBounds.height()
            ).toBundle()
        }.getOrNull()
    }

    val screenLoc = IntArray(2)
    view.getLocationOnScreen(screenLoc)
    val screenRect = Rect(
        rootBounds.left + screenLoc[0],
        rootBounds.top + screenLoc[1],
        rootBounds.right + screenLoc[0],
        rootBounds.bottom + screenLoc[1]
    )
    return optionsBundle to screenRect
}

/**
 * Cria um lançador de aplicativos que dispara transição nativa a partir da posição do ícone
 * com feedback háptico de confirmação.
 */
@Composable
fun rememberAppLauncher(
    viewModel: MainViewModel,
    onLaunchSuccess: () -> Unit = {}
): (AppInfo, Rect?) -> Result<Unit> {
    val view = LocalView.current
    val haptics = rememberTesseraHaptics()
    return remember(viewModel, view, haptics, onLaunchSuccess) {
        { app, rootBounds ->
            haptics.confirm()
            val (optionsBundle, screenBounds) = buildLaunchOptionsAndScreenBounds(view, rootBounds)
            val result = viewModel.launchApp(
                app = app,
                launchOptions = optionsBundle,
                sourceBounds = screenBounds
            )
            if (result.isSuccess) {
                onLaunchSuccess()
            }
            result
        }
    }
}
