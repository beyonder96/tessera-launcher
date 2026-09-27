package com.tessera.launcher.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import com.tessera.launcher.data.helper.PinnedShortcutManager
import com.tessera.launcher.data.model.PinnedShortcutInfo
import java.util.UUID

class InstallShortcutReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.android.launcher.action.INSTALL_SHORTCUT") return

        val name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        if (name.isNullOrBlank()) return

        val shortcutIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT)
        }

        val iconBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        }

        val intentUri = shortcutIntent?.toUri(Intent.URI_INTENT_SCHEME)
        val pkg = shortcutIntent?.`package` ?: shortcutIntent?.component?.packageName ?: "legacy"
        val isPwa = pkg in PinnedShortcutManager.BROWSER_PACKAGES || intentUri?.contains("http") == true
        val shortcutId = "legacy_${UUID.randomUUID()}"

        val manager = PinnedShortcutManager.getInstance(context)
        val iconFileName = iconBitmap?.let { manager.saveShortcutIcon(shortcutId, it) }

        val info = PinnedShortcutInfo(
            id = shortcutId,
            packageName = pkg,
            label = name,
            iconFileName = iconFileName,
            intentUri = intentUri,
            isPwa = isPwa
        )
        manager.addShortcut(info, iconBitmap)

        Toast.makeText(
            context,
            "\"$name\" adicionado ao Tessera Launcher",
            Toast.LENGTH_SHORT
        ).show()
    }
}
