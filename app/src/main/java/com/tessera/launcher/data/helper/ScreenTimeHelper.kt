package com.tessera.launcher.data.helper

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val timeMillis: Long,
    val formattedTime: String,
    val percent: Float
)

data class ScreenTimeInfo(
    val totalTimeMillis: Long = 0L,
    val formattedTotalTime: String = "0m",
    val topApps: List<AppUsageStat> = emptyList(),
    val hasPermission: Boolean = false
)

class ScreenTimeHelper(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    fun getUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getDailyScreenTime(): ScreenTimeInfo {
        if (!hasUsagePermission()) {
            return ScreenTimeInfo(hasPermission = false)
        }

        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return ScreenTimeInfo(hasPermission = true)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ) ?: emptyList()

            val pm = context.packageManager
            val ownPackage = context.packageName

            // Agrupa tempo por pacote (alguns aparelhos retornam múltiplos registros fragmentados)
            val aggregated = mutableMapOf<String, Long>()
            for (stat in stats) {
                val pkg = stat.packageName
                // Ignora o launcher atual, serviços de sistema puros e pacotes sem tempo
                if (pkg == ownPackage || pkg.startsWith("android") || pkg.startsWith("com.android.systemui")) {
                    continue
                }
                val current = aggregated.getOrDefault(pkg, 0L)
                aggregated[pkg] = current + stat.totalTimeInForeground
            }

            // Filtra apps com mais de 30 segundos de uso
            val filtered = aggregated.filter { it.value > 30_000L }
            val totalTime = filtered.values.sum()

            val topStats = filtered.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { (pkg, time) ->
                    val appLabel = runCatching {
                        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getApplicationInfo(pkg, 0)
                        }
                        pm.getApplicationLabel(appInfo).toString()
                    }.getOrDefault(pkg.substringAfterLast("."))

                    val percent = if (totalTime > 0) (time.toFloat() / totalTime).coerceIn(0f, 1f) else 0f
                    AppUsageStat(
                        packageName = pkg,
                        appName = appLabel,
                        timeMillis = time,
                        formattedTime = formatDuration(time),
                        percent = percent
                    )
                }

            ScreenTimeInfo(
                totalTimeMillis = totalTime,
                formattedTotalTime = formatDuration(totalTime),
                topApps = topStats,
                hasPermission = true
            )
        } catch (_: Exception) {
            ScreenTimeInfo(hasPermission = true)
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
