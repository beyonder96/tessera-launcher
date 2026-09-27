package com.tessera.launcher.data.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tessera.launcher.data.model.PinnedShortcutInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class PinnedShortcutManager private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("tessera_pinned_shortcuts", Context.MODE_PRIVATE)

    companion object {
        const val ACTION_SHORTCUTS_CHANGED = "com.tessera.launcher.action.SHORTCUTS_CHANGED"
        private const val KEY_SHORTCUTS_DATA = "shortcuts_json_data"

        val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.fenix",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.sec.android.app.sbrowser",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.duckduckgo.mobile.android",
            "com.kiwibrowser.browser",
            "com.vivaldi.browser"
        )

        @Volatile
        private var instance: PinnedShortcutManager? = null

        fun getInstance(context: Context): PinnedShortcutManager {
            return instance ?: synchronized(this) {
                instance ?: PinnedShortcutManager(context.applicationContext).also { instance = it }
            }
        }
    }

    @Synchronized
    fun getPinnedShortcuts(): List<PinnedShortcutInfo> {
        val raw = prefs.getString(KEY_SHORTCUTS_DATA, null) ?: return emptyList()
        return fromJson(raw)
    }

    @Synchronized
    fun addShortcut(info: PinnedShortcutInfo, bitmap: Bitmap?) {
        val list = getPinnedShortcuts().toMutableList()
        var iconFile = info.iconFileName
        if (bitmap != null && iconFile == null) {
            iconFile = saveShortcutIcon(info.id, bitmap)
        }

        val updatedInfo = if (iconFile != info.iconFileName) {
            info.copy(iconFileName = iconFile)
        } else {
            info
        }

        // Remove duplicatas se houver
        list.removeAll { it.id == updatedInfo.id && it.packageName == updatedInfo.packageName }
        list.add(updatedInfo)

        prefs.edit().putString(KEY_SHORTCUTS_DATA, toJson(list)).apply()
        notifyShortcutsChanged()
    }

    @Synchronized
    fun saveShortcutMetadataOnly(info: PinnedShortcutInfo) {
        val list = getPinnedShortcuts().toMutableList()
        if (list.none { it.id == info.id && it.packageName == info.packageName }) {
            list.add(info)
            prefs.edit().putString(KEY_SHORTCUTS_DATA, toJson(list)).apply()
        }
    }

    @Synchronized
    fun removeShortcut(packageName: String, shortcutId: String?) {
        val list = getPinnedShortcuts().toMutableList()
        val toRemove = list.filter {
            it.packageName == packageName && (shortcutId == null || it.id == shortcutId)
        }
        for (item in toRemove) {
            item.iconFileName?.let { fname ->
                try {
                    val file = File(getIconsDir(), fname)
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
        }
        list.removeAll(toRemove)
        prefs.edit().putString(KEY_SHORTCUTS_DATA, toJson(list)).apply()
        notifyShortcutsChanged()
    }

    fun saveShortcutIcon(id: String, bitmap: Bitmap): String {
        return try {
            val dir = getIconsDir()
            val safeId = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val file = File(dir, "pwa_${safeId}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.name
        } catch (_: Exception) {
            ""
        }
    }

    fun getShortcutIcon(iconFileName: String?): Bitmap? {
        if (iconFileName.isNullOrBlank()) return null
        return try {
            val file = File(getIconsDir(), iconFileName)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getIconsDir(): File {
        return File(context.filesDir, "pwa_shortcuts").apply {
            if (!exists()) mkdirs()
        }
    }

    fun notifyShortcutsChanged() {
        val intent = Intent(ACTION_SHORTCUTS_CHANGED).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    private fun toJson(shortcuts: List<PinnedShortcutInfo>): String {
        val array = JSONArray()
        shortcuts.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("packageName", it.packageName)
                put("label", it.label)
                put("iconFileName", it.iconFileName ?: "")
                put("intentUri", it.intentUri ?: "")
                put("isPwa", it.isPwa)
                put("createdAt", it.createdAt)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun fromJson(raw: String): List<PinnedShortcutInfo> {
        if (raw.isBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<PinnedShortcutInfo>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PinnedShortcutInfo(
                        id = obj.getString("id"),
                        packageName = obj.getString("packageName"),
                        label = obj.getString("label"),
                        iconFileName = obj.optString("iconFileName").takeIf { it.isNotBlank() },
                        intentUri = obj.optString("intentUri").takeIf { it.isNotBlank() },
                        isPwa = obj.optBoolean("isPwa", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
