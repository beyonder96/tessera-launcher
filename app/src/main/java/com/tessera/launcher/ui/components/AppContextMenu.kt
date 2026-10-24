package com.tessera.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tessera.launcher.data.helper.IconPackInfo
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.design.components.CapsuleButton
import com.tessera.launcher.ui.design.components.GroupedRow
import com.tessera.launcher.ui.design.components.GroupedSection
import com.tessera.launcher.ui.design.components.SquircleIcon
import com.tessera.launcher.ui.design.glass.GlassStyle
import com.tessera.launcher.ui.design.glass.liquidGlass
import com.tessera.launcher.ui.design.haptics.Taptic
import com.tessera.launcher.ui.state.AppShortcutItem
import com.tessera.launcher.ui.theme.apple.AppleShapes
import com.tessera.launcher.ui.theme.apple.AppleTint
import com.tessera.launcher.ui.theme.apple.ContinuousShape
import com.tessera.launcher.ui.theme.apple.LocalAppleColors
import com.tessera.launcher.ui.theme.apple.LocalAppleTypography

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

/**
 * Menu Haptic Touch Apple (iOS 26 Liquid Glass).
 * Ao manter pressionado um app, exibe um preview elevado e atalhos em vidro.
 */
@Composable
fun AppContextMenu(
    app: AppInfo,
    shortcuts: List<AppShortcutItem> = emptyList(),
    installedIconPacks: List<IconPackInfo> = emptyList(),
    currentCustomIconPack: String? = null,
    onShortcutClick: (AppShortcutItem) -> Unit = {},
    onChangeIconPack: (String?) -> Unit = {},
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onHideApp: () -> Unit = {},
    onUninstallApp: () -> Unit,
    onRemoveShortcut: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = LocalAppleColors.current
    val typography = LocalAppleTypography.current

    val imageBitmap: ImageBitmap? = remember(app.bitmap, app.icon) {
        app.bitmap?.asImageBitmap() ?: app.icon?.let { drawableToBitmap(it).asImageBitmap() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = ContinuousShape(30.dp), style = GlassStyle.Chrome)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header elevado do aplicativo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(AppleShapes.icon)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(AppleShapes.icon)
                                .background(colors.fill),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.firstLetter.toString(),
                                style = typography.title1,
                                color = colors.secondaryLabel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = typography.title3,
                            color = colors.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = app.packageName,
                            style = typography.footnote,
                            color = colors.secondaryLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seção de Atalhos Rápidos
                if (shortcuts.isNotEmpty()) {
                    GroupedSection(header = "Atalhos") {
                        shortcuts.take(4).forEachIndexed { idx, shortcut ->
                            val shortcutBitmap = remember(shortcut.icon) {
                                shortcut.icon?.let { drawableToBitmap(it).asImageBitmap() }
                            }
                            GroupedRow(
                                title = shortcut.shortLabel,
                                leadingIcon = {
                                    if (shortcutBitmap != null) {
                                        Image(
                                            bitmap = shortcutBitmap,
                                            contentDescription = null,
                                            modifier = Modifier.size(26.dp).clip(CircleShape)
                                        )
                                    } else {
                                        SquircleIcon(
                                            imageVector = Icons.Outlined.Shortcut,
                                            backgroundColor = colors.accent
                                        )
                                    }
                                },
                                onClick = {
                                    Taptic.lightImpact(context)
                                    onDismiss()
                                    onShortcutClick(shortcut)
                                },
                                showDivider = idx < shortcuts.lastIndex
                            )
                        }
                    }
                }

                // Ações do Sistema
                GroupedSection {
                    GroupedRow(
                        title = "Informações do App",
                        leadingIcon = {
                            SquircleIcon(
                                imageVector = Icons.Outlined.Info,
                                backgroundColor = AppleTint.BLUE.resolve(colors.isDark)
                            )
                        },
                        onClick = {
                            Taptic.lightImpact(context)
                            onDismiss()
                            onOpenAppSettings()
                        }
                    )

                    GroupedRow(
                        title = "Ocultar Aplicativo",
                        leadingIcon = {
                            SquircleIcon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                backgroundColor = AppleTint.ORANGE.resolve(colors.isDark)
                            )
                        },
                        onClick = {
                            Taptic.lightImpact(context)
                            onDismiss()
                            onHideApp()
                        }
                    )

                    // Ação Destrutiva em Vermelho Apple
                    val deleteTitle = if (app.isShortcut) "Remover Atalho" else "Desinstalar"
                    GroupedRow(
                        title = deleteTitle,
                        leadingIcon = {
                            SquircleIcon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                backgroundColor = AppleTint.RED.resolve(colors.isDark)
                            )
                        },
                        onClick = {
                            Taptic.heavyImpact(context)
                            onDismiss()
                            if (app.isShortcut) onRemoveShortcut() else onUninstallApp()
                        },
                        showDivider = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                CapsuleButton(
                    text = "Fechar",
                    onClick = onDismiss,
                    backgroundColor = colors.fill,
                    contentColor = colors.label,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
