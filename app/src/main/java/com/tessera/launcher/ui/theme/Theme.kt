package com.tessera.launcher.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tessera.launcher.ui.theme.tokens.LocalTesseraMotion
import com.tessera.launcher.ui.theme.tokens.LocalTesseraPalette
import com.tessera.launcher.ui.theme.tokens.LocalTesseraShapes
import com.tessera.launcher.ui.theme.tokens.LocalTesseraSpacing
import com.tessera.launcher.ui.theme.tokens.LocalVisagismProfile
import com.tessera.launcher.ui.theme.tokens.TesseraPalette
import com.tessera.launcher.ui.theme.tokens.TesseraSpacing
import com.tessera.launcher.ui.theme.tokens.VisagismProfile
import com.tessera.launcher.ui.theme.tokens.resolveVisagismMotion
import com.tessera.launcher.ui.theme.tokens.resolveVisagismShapes

@Composable
fun TesseraTheme(
    themeMode: String = "AMOLED",
    accentColor: Color = AccentMonochrome,
    visagismProfileId: String = "SERENE",
    content: @Composable () -> Unit
) {
    val palette = when (themeMode.uppercase()) {
        "LIGHT" -> TesseraPalette.light(accentColor)
        "CHARCOAL" -> TesseraPalette.charcoal(accentColor)
        "DARK" -> TesseraPalette.dark(accentColor)
        else -> TesseraPalette.amoled(accentColor)
    }

    val profile = VisagismProfile.fromId(visagismProfileId)
    val shapes = resolveVisagismShapes(profile)
    val motion = resolveVisagismMotion(profile)
    val spacing = TesseraSpacing()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = palette.isLight
                isAppearanceLightNavigationBars = palette.isLight
            }
        }
    }

    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.background,
            secondary = palette.textSecondary,
            onSecondary = palette.background,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceSubtle,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.surfaceBorder
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.background,
            secondary = palette.textSecondary,
            onSecondary = palette.background,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceSubtle,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.surfaceBorder
        )
    }

    CompositionLocalProvider(
        LocalTesseraPalette provides palette,
        LocalTesseraShapes provides shapes,
        LocalTesseraSpacing provides spacing,
        LocalTesseraMotion provides motion,
        LocalVisagismProfile provides profile
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
