package com.tessera.launcher.data.bridge

import android.net.Uri

/**
 * Contrato unificado de integração entre Tessera Core (com.aistudio.tessera.xtrkna)
 * e Tessera Launcher (com.tessera.launcher) — Tessera Ecosystem Bridge.
 */
object TesseraBridgeContract {
    const val CORE_PACKAGE = "com.aistudio.tessera.xtrkna"
    const val AUTHORITY = "com.aistudio.tessera.xtrkna.bridge"
    const val PERMISSION = "com.tessera.permission.ECOSYSTEM_BRIDGE"

    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    val SNAPSHOT_URI: Uri = Uri.parse("content://$AUTHORITY/snapshot")
    val GLANCE_URI: Uri = Uri.parse("content://$AUTHORITY/glance_briefing")
    val DAILY_VERSE_URI: Uri = Uri.parse("content://$AUTHORITY/daily_verse")
    val TIME_CLOCK_URI: Uri = Uri.parse("content://$AUTHORITY/time_clock")
    val MARKET_SUMMARY_URI: Uri = Uri.parse("content://$AUTHORITY/market_summary")
    val FINANCE_PULSE_URI: Uri = Uri.parse("content://$AUTHORITY/finance_pulse")

    // Broadcast Actions
    const val ACTION_LAUNCHER_ZEN_CHANGED = "com.tessera.action.LAUNCHER_ZEN_CHANGED"
    const val ACTION_CORE_FOCUS_CHANGED = "com.tessera.action.CORE_FOCUS_CHANGED"
    const val ACTION_ECOSYSTEM_THEME_SYNC = "com.tessera.action.ECOSYSTEM_THEME_SYNC"
    const val ACTION_ECOSYSTEM_DATA_CHANGED = "com.tessera.action.ECOSYSTEM_DATA_CHANGED"

    // Call Methods (ContentProvider call)
    const val METHOD_QUICK_TRANSACTION = "quick_transaction"
    const val METHOD_ADD_MARKET_ITEMS = "add_market_items"
    const val METHOD_PUNCH_TIME_CLOCK = "punch_time_clock"
    const val METHOD_LOG_HEALTH = "log_health"
    const val METHOD_SET_FOCUS_SESSION = "set_focus_session"
    const val METHOD_TOGGLE_FINANCE_MASK = "toggle_finance_mask"

    // Extras / Bundle Keys
    const val EXTRA_SUCCESS = "success"
    const val EXTRA_MESSAGE = "message"
    const val EXTRA_AMOUNT = "amount"
    const val EXTRA_TYPE = "type" // "EXPENSE" or "INCOME"
    const val EXTRA_DESCRIPTION = "description"
    const val EXTRA_CATEGORY = "category"
    const val EXTRA_ACCOUNT = "account"
    const val EXTRA_DRY_RUN = "dry_run"
    const val EXTRA_RAW_ITEMS = "raw_items"
    const val EXTRA_PUNCH_ACTION = "punch_action" // "IN", "OUT", "TOGGLE"
    const val EXTRA_WATER_ML = "water_ml"
    const val EXTRA_WEIGHT_KG = "weight_kg"
    const val EXTRA_MARK_MEDICATION = "mark_medication"
    const val EXTRA_FOCUS_START = "focus_start"
    const val EXTRA_FOCUS_MINUTES = "focus_minutes"
    const val EXTRA_FOCUS_NAME = "focus_name"
    const val EXTRA_THEME_MODE = "theme_mode" // "JOURNAL", "AMOLED", "SYSTEM"
}

/**
 * Snapshot reativo consolidado do ecossistema Tessera.
 * Transmitido via ContentProvider/ContentObserver para latência ultra-baixa (< 15ms).
 */
data class TesseraEcosystemSnapshot(
    val isInstalled: Boolean = false,
    val hasSyncedData: Boolean = false,
    val dailyVerseText: String? = null,
    val dailyVerseRef: String? = null,
    val verseStreakDays: Int = 0,
    val marketPendingCount: Int = 0,
    val marketEstimatedTotal: Double = 0.0,
    val marketTopItems: List<String> = emptyList(),
    val timeClockStatus: String? = null, // "Trabalhando", "Pausa", "Fora"
    val timeClockWorkedMinutes: Int = 0,
    val timeClockTargetMinutes: Int = 480,
    val timeClockNextPunchHint: String? = null,
    val financeBalance: Double = 0.0,
    val financeVrBalance: Double = 0.0,
    val isFinanceMasked: Boolean = false,
    val nextMedicationName: String? = null,
    val nextMedicationTime: String? = null,
    val waterIntakeMl: Int = 0,
    val waterTargetMl: Int = 2000,
    val weightKg: Double? = null,
    val transitAnomalyAlert: String? = null,
    val activeFocusName: String? = null,
    val focusRemainingSeconds: Int = 0,
    val themeMode: String? = null
)
