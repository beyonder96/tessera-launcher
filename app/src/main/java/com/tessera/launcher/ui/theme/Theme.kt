package com.tessera.launcher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tessera.launcher.ui.design.glass.LocalHazeState
import com.tessera.launcher.ui.design.glass.LocalReduceTransparency
import com.tessera.launcher.ui.theme.apple.AppleColors
import com.tessera.launcher.ui.theme.apple.AppleMotion
import com.tessera.launcher.ui.theme.apple.AppleTypography
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleMotion
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography
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
import dev.chrisbanes.haze.HazeState

@Composable
fun TesseraTheme(
    themeMode: String = "DARK",
    accentColor: Color = Color(0xFF0A84FF),
    isReduceTransparency: Boolean = false,
    isReduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "AUTO" -> systemInDark
        else -> true // DARK, AMOLED, CHARCOAL
    }

    val appleColors = if (isDark) AppleColors.dark(accentColor) else AppleColors.light(accentColor)
    val appleTypography = remember { AppleTypography() }
    val appleMotion = remember(isReduceMotion) { AppleMotion(isReducedMotion = isReduceMotion) }
    val hazeState = remember { HazeState() }

    // Compatibilidade com componentes legados em transição
    val legacyPalette = when (themeMode.uppercase()) {
        "LIGHT" -> TesseraPalette.light(accentColor)
        "CHARCOAL" -> TesseraPalette.charcoal(accentColor)
        "AMOLED" -> TesseraPalette.amoled(accentColor)
        else -> TesseraPalette.dark(accentColor)
    }
    val profile = VisagismProfile.SERENE
    val legacyShapes = resolveVisagismShapes(profile)
    val legacyMotion = resolveVisagismMotion(profile)
    val legacySpacing = TesseraSpacing()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val colorScheme = if (!isDark) {
        lightColorScheme(
            primary = appleColors.accent,
            onPrimary = Color.White,
            secondary = appleColors.secondaryLabel,
            onSecondary = Color.White,
            background = appleColors.systemBackground,
            onBackground = appleColors.label,
            surface = appleColors.secondaryGrouped,
            onSurface = appleColors.label,
            surfaceVariant = appleColors.tertiaryGrouped,
            onSurfaceVariant = appleColors.secondaryLabel,
            outline = appleColors.separator
        )
    } else {
        darkColorScheme(
            primary = appleColors.accent,
            onPrimary = Color.White,
            secondary = appleColors.secondaryLabel,
            onSecondary = Color.White,
            background = appleColors.systemBackground,
            onBackground = appleColors.label,
            surface = appleColors.secondaryGrouped,
            onSurface = appleColors.label,
            surfaceVariant = appleColors.tertiaryGrouped,
            onSurfaceVariant = appleColors.secondaryLabel,
            outline = appleColors.separator
        )
    }

    CompositionLocalProvider(
        LocalAppleColors provides appleColors,
        LocalAppleTypography provides appleTypography,
        LocalAppleMotion provides appleMotion,
        LocalHazeState provides hazeState,
        LocalReduceTransparency provides isReduceTransparency,
        // Provedores legados
        LocalTesseraPalette provides legacyPalette,
        LocalTesseraShapes provides legacyShapes,
        LocalTesseraSpacing provides legacySpacing,
        LocalTesseraMotion provides legacyMotion,
        LocalVisagismProfile provides profile
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
