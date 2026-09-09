package com.tessera.launcher.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserManager
import com.tessera.launcher.data.helper.IconPackHelper
import com.tessera.launcher.data.model.AppInfo
import com.tessera.launcher.ui.state.AppShortcutItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

class AppRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val iconPackHelper: IconPackHelper = IconPackHelper(context)
) {
    private fun rasterizeDrawable(drawable: Drawable?, targetSize: Int = 120): Bitmap? {
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

    suspend fun loadApps(
        iconPackPackage: String? = null,
        customIcons: Map<String, String> = emptyMap(),
        hiddenPackages: Set<String> = emptySet()
    ): List<AppInfo> = withContext(ioDispatcher) {
        val appsList = mutableListOf<AppInfo>()
        val packageManager = context.packageManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
            val profiles = userManager?.userProfiles ?: listOf(Process.myUserHandle())

            for (profile in profiles) {
                val activityList = launcherApps?.getActivityList(null, profile) ?: emptyList()
                for (activity in activityList) {
                    val pkgName = activity.applicationInfo.packageName
                    if (pkgName == context.packageName) continue // Não listar o próprio launcher
                    if (hiddenPackages.contains(pkgName)) continue // Ocultar apps protegidos

                    val label = activity.label.toString()
                    val defaultIcon = try {
                        activity.getBadgedIcon(0)
                    } catch (_: Exception) {
                        null
                    }

                    // Prioridade do ícone: 1. Ícone customizado individual, 2. Icon Pack global, 3. Padrão
                    val customPack = customIcons[pkgName]?.substringBefore(":::")
                    val customIcon = if (!customPack.isNullOrBlank()) iconPackHelper.getIconForApp(pkgName, activity.name, customPack) else null
                    val packIcon = if (!iconPackPackage.isNullOrBlank()) iconPackHelper.getIconForApp(pkgName, activity.name, iconPackPackage) else null
                    val icon = customIcon ?: packIcon ?: defaultIcon
                    val iconBitmap = rasterizeDrawable(icon)

                    appsList.add(
                        AppInfo(
                            label = label,
                            packageName = pkgName,
                            activityName = activity.name,
                            icon = icon,
                            bitmap = iconBitmap
                        )
                    )
                }
            }
        } else {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == context.packageName) continue
                if (hiddenPackages.contains(pkgName)) continue

                val label = resolveInfo.loadLabel(packageManager).toString()
                val defaultIcon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (_: Exception) {
                    null
                }
                val customPack = customIcons[pkgName]?.substringBefore(":::")
                val customIcon = if (!customPack.isNullOrBlank()) iconPackHelper.getIconForApp(pkgName, resolveInfo.activityInfo.name, customPack) else null
                val packIcon = if (!iconPackPackage.isNullOrBlank()) iconPackHelper.getIconForApp(pkgName, resolveInfo.activityInfo.name, iconPackPackage) else null
                val icon = customIcon ?: packIcon ?: defaultIcon
                val iconBitmap = rasterizeDrawable(icon)

                appsList.add(
                    AppInfo(
                        label = label,
                        packageName = pkgName,
                        activityName = resolveInfo.activityInfo.name,
                        icon = icon,
                        bitmap = iconBitmap
                    )
                )
            }
        }

        // Ordenação alfabética natural respeitando acentos e locale
        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }
        appsList.distinctBy { it.packageName }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    /**
     * Observa mudanças nos aplicativos instalados/desinstalados/atualizados
     * emitindo uma nova lista automaticamente via BroadcastReceiver.
     */
    fun observeApps(
        iconPackPackage: String? = null,
        customIcons: Map<String, String> = emptyMap(),
        hiddenPackages: Set<String> = emptySet()
    ): Flow<List<AppInfo>> = callbackFlow {
        // Envia carga inicial
        launch(ioDispatcher) {
            trySend(loadApps(iconPackPackage, customIcons, hiddenPackages))
        }

        val packageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REMOVED ||
                    action == Intent.ACTION_PACKAGE_CHANGED
                ) {
                    launch(ioDispatcher) {
                        trySend(loadApps(iconPackPackage, customIcons, hiddenPackages))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        androidx.core.content.ContextCompat.registerReceiver(
            context,
            packageChangeReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose {
            try {
                context.unregisterReceiver(packageChangeReceiver)
            } catch (_: IllegalArgumentException) {}
        }
    }.flowOn(ioDispatcher)

    fun launchApp(packageName: String): Result<Unit> = runCatching {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        } ?: throw IllegalStateException("Não foi possível inicializar o aplicativo: $packageName")

        context.startActivity(launchIntent)
    }

    /**
     * Consulta atalhos de apps do Android (Dynamic, Pinned e Manifest)
     */
    fun getAppShortcuts(packageName: String): List<AppShortcutItem> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return emptyList()
        if (!launcherApps.hasShortcutHostPermission()) return emptyList()

        return try {
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                )
                setPackage(packageName)
            }
            val shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle()) ?: emptyList()
            shortcuts.map { shortcut ->
                val iconDrawable = try {
                    launcherApps.getShortcutIconDrawable(shortcut, context.resources.displayMetrics.densityDpi)
                } catch (_: Exception) {
                    null
                }
                AppShortcutItem(
                    id = shortcut.id,
                    packageName = shortcut.activity?.packageName ?: packageName,
                    shortLabel = shortcut.shortLabel?.toString() ?: "",
                    longLabel = shortcut.longLabel?.toString(),
                    icon = iconDrawable
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun launchShortcut(packageName: String, shortcutId: String): Result<Unit> = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                ?: throw IllegalStateException("LauncherApps não disponível")
            launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle())
        } else {
            throw UnsupportedOperationException("Atalhos requerem Android 7.1+")
        }
    }
}
