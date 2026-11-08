package com.tessera.launcher.data.bridge

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Cliente IPC reativo para conectar o Tessera Launcher ao Tessera Core.
 * Fornece snapshot em tempo real via ContentObserver e ações headless em baixa latência (< 15ms).
 */
object TesseraCoreBridgeClient {
    private const val TAG = "TesseraCoreBridgeClient"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var appContext: Context? = null
    private var contentObserver: ContentObserver? = null

    private val _snapshot = MutableStateFlow<TesseraEcosystemSnapshot?>(null)
    val snapshot: StateFlow<TesseraEcosystemSnapshot?> = _snapshot.asStateFlow()

    @Volatile
    var isInitialized: Boolean = false
        private set

    fun init(context: Context) {
        if (isInitialized) return
        val app = context.applicationContext
        appContext = app
        isInitialized = true

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                refreshSnapshot()
            }
        }
        contentObserver = observer

        try {
            app.contentResolver.registerContentObserver(
                TesseraBridgeContract.SNAPSHOT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível registrar ContentObserver (Core pode não estar instalado): ${e.message}")
        }

        refreshSnapshot()
    }

    fun isCoreInstalled(): Boolean {
        val ctx = appContext ?: return false
        return try {
            val pm = ctx.packageManager
            pm.getPackageInfo(TesseraBridgeContract.CORE_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun refreshSnapshot() {
        scope.launch {
            val ctx = appContext ?: return@launch
            if (!isCoreInstalled()) {
                _snapshot.value = null
                return@launch
            }

            try {
                val cursor = ctx.contentResolver.query(
                    TesseraBridgeContract.SNAPSHOT_URI,
                    null,
                    null,
                    null,
                    null
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        fun getString(col: String): String? =
                            try { c.getColumnIndex(col).takeIf { it >= 0 }?.let { c.getString(it) } } catch (_: Exception) { null }
                        fun getInt(col: String, default: Int = 0): Int =
                            try { c.getColumnIndex(col).takeIf { it >= 0 }?.let { c.getInt(it) } ?: default } catch (_: Exception) { default }
                        fun getDouble(col: String, default: Double = 0.0): Double =
                            try { c.getColumnIndex(col).takeIf { it >= 0 }?.let { c.getDouble(it) } ?: default } catch (_: Exception) { default }
                        fun getBoolean(col: String, default: Boolean = false): Boolean =
                            try { c.getColumnIndex(col).takeIf { it >= 0 }?.let { c.getInt(it) == 1 } ?: default } catch (_: Exception) { default }

                        val topItemsRaw = getString("market_top_items") ?: ""
                        val topItemsList = if (topItemsRaw.isNotBlank()) {
                            topItemsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        } else emptyList()

                        val newSnapshot = TesseraEcosystemSnapshot(
                            isInstalled = true,
                            dailyVerseText = getString("daily_verse_text"),
                            dailyVerseRef = getString("daily_verse_ref"),
                            verseStreakDays = getInt("verse_streak_days", 0),
                            marketPendingCount = getInt("market_pending_count", 0),
                            marketEstimatedTotal = getDouble("market_estimated_total", 0.0),
                            marketTopItems = topItemsList,
                            timeClockStatus = getString("time_clock_status"),
                            timeClockWorkedMinutes = getInt("time_clock_worked_minutes", 0),
                            timeClockTargetMinutes = getInt("time_clock_target_minutes", 480),
                            timeClockNextPunchHint = getString("time_clock_next_punch_hint"),
                            financeBalance = getDouble("finance_balance", 0.0),
                            financeVrBalance = getDouble("finance_vr_balance", 0.0),
                            isFinanceMasked = getBoolean("is_finance_masked", false),
                            nextMedicationName = getString("next_medication_name"),
                            nextMedicationTime = getString("next_medication_time"),
                            waterIntakeMl = getInt("water_intake_ml", 0),
                            waterTargetMl = getInt("water_target_ml", 2000),
                            weightKg = getString("weight_kg")?.toDoubleOrNull(),
                            transitAnomalyAlert = getString("transit_anomaly_alert"),
                            activeFocusName = getString("active_focus_name"),
                            focusRemainingSeconds = getInt("focus_remaining_seconds", 0),
                            themeMode = getString("theme_mode")
                        )
                        _snapshot.value = newSnapshot
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Sem permissão para consultar ContentProvider do Tessera Core: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao atualizar snapshot do Tessera Core: ${e.message}")
            }
        }
    }

    suspend fun quickTransaction(
        amount: Double,
        type: String,
        description: String,
        category: String = "",
        account: String = "",
        dryRun: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("Bridge não inicializado"))
        try {
            val extras = Bundle().apply {
                putDouble(TesseraBridgeContract.EXTRA_AMOUNT, amount)
                putString(TesseraBridgeContract.EXTRA_TYPE, type)
                putString(TesseraBridgeContract.EXTRA_DESCRIPTION, description)
                putString(TesseraBridgeContract.EXTRA_CATEGORY, category)
                putString(TesseraBridgeContract.EXTRA_ACCOUNT, account)
                putBoolean(TesseraBridgeContract.EXTRA_DRY_RUN, dryRun)
            }
            val res = ctx.contentResolver.call(
                TesseraBridgeContract.BASE_CONTENT_URI,
                TesseraBridgeContract.METHOD_QUICK_TRANSACTION,
                null,
                extras
            )
            val success = res?.getBoolean(TesseraBridgeContract.EXTRA_SUCCESS, false) ?: false
            val message = res?.getString(TesseraBridgeContract.EXTRA_MESSAGE) ?: if (success) "Lançamento efetuado!" else "Falha ao registrar"
            if (success) {
                if (!dryRun) refreshSnapshot()
                Result.success(message)
            } else {
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMarketItems(itemsRaw: String): Result<String> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("Bridge não inicializado"))
        try {
            val extras = Bundle().apply {
                putString(TesseraBridgeContract.EXTRA_RAW_ITEMS, itemsRaw)
            }
            val res = ctx.contentResolver.call(
                TesseraBridgeContract.BASE_CONTENT_URI,
                TesseraBridgeContract.METHOD_ADD_MARKET_ITEMS,
                null,
                extras
            )
            val success = res?.getBoolean(TesseraBridgeContract.EXTRA_SUCCESS, false) ?: false
            val message = res?.getString(TesseraBridgeContract.EXTRA_MESSAGE) ?: if (success) "Itens adicionados!" else "Falha ao adicionar itens"
            if (success) {
                refreshSnapshot()
                Result.success(message)
            } else {
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun punchTimeClock(action: String = "TOGGLE"): Result<String> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("Bridge não inicializado"))
        try {
            val extras = Bundle().apply {
                putString(TesseraBridgeContract.EXTRA_PUNCH_ACTION, action)
            }
            val res = ctx.contentResolver.call(
                TesseraBridgeContract.BASE_CONTENT_URI,
                TesseraBridgeContract.METHOD_PUNCH_TIME_CLOCK,
                null,
                extras
            )
            val success = res?.getBoolean(TesseraBridgeContract.EXTRA_SUCCESS, false) ?: false
            val message = res?.getString(TesseraBridgeContract.EXTRA_MESSAGE) ?: if (success) "Ponto batido com sucesso!" else "Falha ao bater ponto"
            if (success) {
                refreshSnapshot()
                Result.success(message)
            } else {
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logHealth(waterMl: Int? = null, weightKg: Double? = null, markMedication: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("Bridge não inicializado"))
        try {
            val extras = Bundle().apply {
                waterMl?.let { putInt(TesseraBridgeContract.EXTRA_WATER_ML, it) }
                weightKg?.let { putDouble(TesseraBridgeContract.EXTRA_WEIGHT_KG, it) }
                putBoolean(TesseraBridgeContract.EXTRA_MARK_MEDICATION, markMedication)
            }
            val res = ctx.contentResolver.call(
                TesseraBridgeContract.BASE_CONTENT_URI,
                TesseraBridgeContract.METHOD_LOG_HEALTH,
                null,
                extras
            )
            val success = res?.getBoolean(TesseraBridgeContract.EXTRA_SUCCESS, false) ?: false
            val message = res?.getString(TesseraBridgeContract.EXTRA_MESSAGE) ?: if (success) "Registro de saúde atualizado" else "Falha ao registrar saúde"
            if (success) {
                refreshSnapshot()
                Result.success(message)
            } else {
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFinanceMask(): Result<Boolean> = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext Result.failure(IllegalStateException("Bridge não inicializado"))
        try {
            val res = ctx.contentResolver.call(
                TesseraBridgeContract.BASE_CONTENT_URI,
                TesseraBridgeContract.METHOD_TOGGLE_FINANCE_MASK,
                null,
                null
            )
            val success = res?.getBoolean(TesseraBridgeContract.EXTRA_SUCCESS, false) ?: false
            val isMasked = res?.getBoolean("is_finance_masked", false) ?: false
            if (success) {
                val current = _snapshot.value
                if (current != null) {
                    _snapshot.value = current.copy(isFinanceMasked = isMasked)
                }
                Result.success(isMasked)
            } else {
                Result.failure(Exception("Não foi possível alternar visibilidade financeira"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun notifyLauncherZenChanged(isActive: Boolean, durationMinutes: Int = 25) {
        val ctx = appContext ?: return
        try {
            val intent = Intent(TesseraBridgeContract.ACTION_LAUNCHER_ZEN_CHANGED).apply {
                setPackage(TesseraBridgeContract.CORE_PACKAGE)
                putExtra(TesseraBridgeContract.EXTRA_FOCUS_START, isActive)
                putExtra(TesseraBridgeContract.EXTRA_FOCUS_MINUTES, durationMinutes)
                putExtra(TesseraBridgeContract.EXTRA_FOCUS_NAME, "Modo Foco Zen (Launcher)")
            }
            ctx.sendBroadcast(intent, TesseraBridgeContract.PERMISSION)
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao emitir ACTION_LAUNCHER_ZEN_CHANGED: ${e.message}")
        }
    }
}
