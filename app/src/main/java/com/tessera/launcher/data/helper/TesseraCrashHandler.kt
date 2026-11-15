package com.tessera.launcher.data.helper

import android.content.Context

object TesseraCrashHandler {
    private const val PREFS_NAME = "tessera_crash_guard"
    private const val KEY_CRASH_TIMESTAMPS = "crash_timestamps"
    private const val KEY_SAFE_MODE_PENDING = "safe_mode_pending"
    private const val KEY_LAST_CRASH_SUMMARY = "last_crash_summary"
    private const val CRASH_WINDOW_MS = 30_000L
    private const val CRASH_THRESHOLD = 2

    @Volatile
    private var isInstalled = false

    fun install(context: Context) {
        if (isInstalled) return
        isInstalled = true
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                recordCrash(appContext, throwable)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun recordCrash(context: Context, throwable: Throwable) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val raw = prefs.getString(KEY_CRASH_TIMESTAMPS, "").orEmpty()
        val recent = raw.split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it in 0..CRASH_WINDOW_MS } + now

        val summary = "${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}".take(220)
        prefs.edit()
            .putString(KEY_CRASH_TIMESTAMPS, recent.joinToString(","))
            .putString(KEY_LAST_CRASH_SUMMARY, summary)
            .putBoolean(
                KEY_SAFE_MODE_PENDING,
                prefs.getBoolean(KEY_SAFE_MODE_PENDING, false) || recent.size >= CRASH_THRESHOLD
            )
            .commit()
    }

    fun isSafeModePending(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SAFE_MODE_PENDING, false)
    }

    fun getLastCrashSummary(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_CRASH_SUMMARY, null)?.takeIf { it.isNotBlank() }
    }

    fun clearSafeModePending(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_SAFE_MODE_PENDING, false)
            .remove(KEY_CRASH_TIMESTAMPS)
            .apply()
    }
}
