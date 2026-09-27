package com.tessera.launcher.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Shortcut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tessera.launcher.data.helper.PinnedShortcutManager
import com.tessera.launcher.data.model.PinnedShortcutInfo
import java.util.UUID

class AddPinShortcutActivity : ComponentActivity() {

    private fun rasterizeDrawable(drawable: Drawable?, targetSize: Int = 144): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            return if (bmp.width <= targetSize && bmp.height <= targetSize) {
                bmp
            } else {
                Bitmap.createScaledBitmap(bmp, targetSize, targetSize, true)
            }
        }
        return try {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(targetSize) else targetSize
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(targetSize) else targetSize
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Throwable) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shortcutId = UUID.randomUUID().toString()
        var shortcutLabel = "Atalho"
        var shortcutPackage = "unknown"
        var shortcutBitmap: Bitmap? = null
        var isPwa = false
        var pinItemRequest: LauncherApps.PinItemRequest? = null
        var shortcutIntentUri: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            pinItemRequest = launcherApps?.getPinItemRequest(intent)

            if (pinItemRequest != null && pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                val shortcutInfo: ShortcutInfo? = pinItemRequest.shortcutInfo
                if (shortcutInfo != null) {
                    shortcutId = shortcutInfo.id
                    shortcutLabel = shortcutInfo.shortLabel?.toString()
                        ?: shortcutInfo.longLabel?.toString()
                        ?: "Atalho"
                    shortcutPackage = shortcutInfo.activity?.packageName ?: shortcutInfo.`package`
                    isPwa = shortcutPackage in PinnedShortcutManager.BROWSER_PACKAGES ||
                            shortcutId.startsWith("http", ignoreCase = true) ||
                            shortcutId.contains("pwa", ignoreCase = true)

                    val density = resources.displayMetrics.densityDpi
                    val drawable = try {
                        launcherApps?.getShortcutBadgedIconDrawable(shortcutInfo, density)
                            ?: launcherApps?.getShortcutIconDrawable(shortcutInfo, density)
                    } catch (_: Exception) {
                        null
                    }
                    shortcutBitmap = rasterizeDrawable(drawable)
                }
            }
        }

        // Suporte a intent extras legado / fallback
        if (pinItemRequest == null) {
            val nameExtra = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            val intentExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT)
            }
            val iconExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
            }

            if (!nameExtra.isNullOrBlank()) {
                shortcutLabel = nameExtra
                shortcutBitmap = iconExtra
                shortcutIntentUri = intentExtra?.toUri(Intent.URI_INTENT_SCHEME)
                shortcutPackage = intentExtra?.`package` ?: intentExtra?.component?.packageName ?: "legacy"
                isPwa = shortcutPackage in PinnedShortcutManager.BROWSER_PACKAGES ||
                        shortcutIntentUri?.contains("http") == true
            } else {
                // Nenhum dado de atalho encontrado
                finish()
                return
            }
        }

        val sourceAppName = try {
            val appInfo = packageManager.getApplicationInfo(shortcutPackage, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            shortcutPackage
        }

        val finalPinRequest = pinItemRequest
        val finalShortcutId = shortcutId
        val finalLabel = shortcutLabel
        val finalPackage = shortcutPackage
        val finalBitmap = shortcutBitmap
        val finalIsPwa = isPwa
        val finalIntentUri = shortcutIntentUri

        setContent {
            AddShortcutDialog(
                title = finalLabel,
                sourceApp = sourceAppName,
                iconBitmap = finalBitmap,
                isPwa = finalIsPwa,
                onConfirm = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && finalPinRequest != null) {
                        try {
                            if (finalPinRequest.isValid) {
                                finalPinRequest.accept()
                            }
                        } catch (_: Exception) {}
                    }

                    val manager = PinnedShortcutManager.getInstance(applicationContext)
                    val iconFile = finalBitmap?.let { bmp ->
                        manager.saveShortcutIcon(finalShortcutId, bmp)
                    }

                    val info = PinnedShortcutInfo(
                        id = finalShortcutId,
                        packageName = finalPackage,
                        label = finalLabel,
                        iconFileName = iconFile,
                        intentUri = finalIntentUri,
                        isPwa = finalIsPwa
                    )
                    manager.addShortcut(info, finalBitmap)

                    Toast.makeText(
                        this@AddPinShortcutActivity,
                        "\"$finalLabel\" adicionado ao Tessera Launcher",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onDismiss = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}

@Composable
private fun AddShortcutDialog(
    title: String,
    sourceApp: String,
    iconBitmap: Bitmap?,
    isPwa: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF141419),
                border = BorderStroke(1.dp, Color(0xFF282834)),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge superior
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPwa) Color(0x1F6C63FF) else Color(0x1FFFFFFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPwa) Icons.Outlined.Language else Icons.Outlined.Shortcut,
                            contentDescription = null,
                            tint = if (isPwa) Color(0xFF8C82FF) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPwa) "Aplicativo Web (PWA)" else "Atalho",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isPwa) Color(0xFF8C82FF) else Color(0xFFCCCCCC)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Ícone do Atalho / PWA
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF20202A))
                            .border(1.dp, Color(0xFF333344), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconBitmap != null) {
                            Image(
                                bitmap = iconBitmap.asImageBitmap(),
                                contentDescription = title,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Icon(
                                imageVector = if (isPwa) Icons.Outlined.Language else Icons.Outlined.Shortcut,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nome do PWA / Atalho
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Origem
                    Text(
                        text = "Adicionar via $sourceApp",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF9090A0),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botões de Ação
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF333342))
                        ) {
                            Text(
                                text = "Cancelar",
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Adicionar",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
