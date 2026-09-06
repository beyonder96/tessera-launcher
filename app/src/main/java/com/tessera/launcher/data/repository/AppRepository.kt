package com.tessera.launcher.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserManager
import com.tessera.launcher.data.helper.IconPackHelper
import com.tessera.launcher.data.model.AppInfo
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

    suspend fun loadApps(iconPackPackage: String? = null): List<AppInfo> = withContext(ioDispatcher) {
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

                    val label = activity.label.toString()
                    val defaultIcon = try {
                        activity.getBadgedIcon(0)
                    } catch (_: Exception) {
                        null
                    }
                    val icon = if (!iconPackPackage.isNullOrBlank()) {
                        iconPackHelper.getIconForApp(pkgName, activity.name, iconPackPackage) ?: defaultIcon
                    } else {
                        defaultIcon
                    }

                    appsList.add(
                        AppInfo(
                            label = label,
                            packageName = pkgName,
                            activityName = activity.name,
                            icon = icon
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

                val label = resolveInfo.loadLabel(packageManager).toString()
                val defaultIcon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (_: Exception) {
                    null
                }
                val icon = if (!iconPackPackage.isNullOrBlank()) {
                    iconPackHelper.getIconForApp(pkgName, resolveInfo.activityInfo.name, iconPackPackage) ?: defaultIcon
                } else {
                    defaultIcon
                }

                appsList.add(
                    AppInfo(
                        label = label,
                        packageName = pkgName,
                        activityName = resolveInfo.activityInfo.name,
                        icon = icon
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
    fun observeApps(iconPackPackage: String? = null): Flow<List<AppInfo>> = callbackFlow {
        // Envia carga inicial
        launch(ioDispatcher) {
            trySend(loadApps(iconPackPackage))
        }

        val packageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REMOVED ||
                    action == Intent.ACTION_PACKAGE_CHANGED
                ) {
                    launch(ioDispatcher) {
                        trySend(loadApps(iconPackPackage))
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
}
