package com.tessera.launcher.data.license

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.runtime.Immutable
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Immutable
enum class LicenseTier {
    TRIAL,
    FREE,
    PRO_GIFT,
    PRO_MONTHLY,
    PRO_SEMESTRAL,
    PRO_ANNUAL,
    PRO_LIFETIME,
    MASTER
}

enum class LicenseKeyType(val code: String, val displayLabel: String, val durationDays: Int?) {
    GIFT("GIFT", "Cortesia", null),
    MENS("MENS", "Pro Mensal", 30),
    SEMS("SEMS", "Pro Semestral", 180),
    ANUL("ANUL", "Pro Anual", 365),
    LIFE("LIFE", "Pro Vitalício", null),
    MSTR("MSTR", "Master Desenvolvedor", null);

    companion object {
        fun fromCode(code: String): LicenseKeyType? = entries.firstOrNull { it.code == code }
    }
}

@Immutable
data class LicenseStatus(
    val tier: LicenseTier = LicenseTier.TRIAL,
    val hasProAccess: Boolean = true,
    val isMasterAccess: Boolean = false,
    val daysRemaining: Int = BillingConfig.TRIAL_DAYS,
    val hoursRemaining: Int = BillingConfig.TRIAL_DAYS * 24,
    val remainingMillis: Long = BillingConfig.TRIAL_DURATION_MS,
    val isClockTampered: Boolean = false,
    val isVaultTampered: Boolean = false,
    val expiresAtMs: Long? = null,
    val activeKeyMasked: String? = null,
    val activationTypeLabel: String = "Trial Pro (3 dias)"
) {
    val isTrialActive: Boolean get() = tier == LicenseTier.TRIAL && hasProAccess
    val isTrialExpired: Boolean get() = tier == LicenseTier.FREE && !hasProAccess
    val isSubscribedPro: Boolean get() = hasProAccess && tier != LicenseTier.TRIAL && tier != LicenseTier.FREE
    val isPermanentPro: Boolean get() = isSubscribedPro && (tier == LicenseTier.PRO_LIFETIME || tier == LicenseTier.MASTER)
}

sealed interface LicenseActivationResult {
    data class Success(val status: LicenseStatus, val message: String) : LicenseActivationResult
    data class InvalidKey(val message: String) : LicenseActivationResult
    data class ExpiredKey(val message: String) : LicenseActivationResult
}

interface TimeProvider {
    fun currentTimeMillis(): Long
    fun elapsedRealtimeMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}

data class VaultSnapshot(
    val trialStartedAtMs: Long,
    val lastSeenWallMs: Long,
    val lastSeenElapsedMs: Long,
    val activeKey: String?,
    val activatedAtMs: Long?,
    val expiresAtMs: Long?,
    val usedKeyHashes: Set<String>
)

interface LicenseStorage {
    fun readSnapshot(): VaultSnapshot?
    fun writeSnapshot(snapshot: VaultSnapshot)
    fun isSealValid(): Boolean
}

class PrefsLicenseStorage(context: Context) : LicenseStorage {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val deviceBindingId: String = runCatching {
        Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }.getOrNull().orEmpty().ifBlank { "tessera-default-device" }

    override fun readSnapshot(): VaultSnapshot? {
        if (!prefs.contains(KEY_TRIAL_STARTED_AT)) return null
        val trialStarted = prefs.getLong(KEY_TRIAL_STARTED_AT, 0L)
        val lastSeenWall = prefs.getLong(KEY_LAST_SEEN_WALL, trialStarted)
        val lastSeenElapsed = prefs.getLong(KEY_LAST_SEEN_ELAPSED, 0L)
        val activeKey = prefs.getString(KEY_ACTIVE_KEY, null)
        val activatedAt = if (prefs.contains(KEY_ACTIVATED_AT)) prefs.getLong(KEY_ACTIVATED_AT, 0L) else null
        val expiresRaw = prefs.getLong(KEY_EXPIRES_AT, NO_EXPIRY)
        val expiresAt = if (expiresRaw == NO_EXPIRY) null else expiresRaw
        val usedHashes = prefs.getStringSet(KEY_USED_KEY_HASHES, emptySet())?.toSet() ?: emptySet()
        return VaultSnapshot(
            trialStartedAtMs = trialStarted,
            lastSeenWallMs = lastSeenWall,
            lastSeenElapsedMs = lastSeenElapsed,
            activeKey = activeKey,
            activatedAtMs = activatedAt,
            expiresAtMs = expiresAt,
            usedKeyHashes = usedHashes
        )
    }

    override fun writeSnapshot(snapshot: VaultSnapshot) {
        val seal = computeSeal(snapshot)
        prefs.edit().apply {
            putLong(KEY_TRIAL_STARTED_AT, snapshot.trialStartedAtMs)
            putLong(KEY_LAST_SEEN_WALL, snapshot.lastSeenWallMs)
            putLong(KEY_LAST_SEEN_ELAPSED, snapshot.lastSeenElapsedMs)
            if (snapshot.activeKey != null) putString(KEY_ACTIVE_KEY, snapshot.activeKey) else remove(KEY_ACTIVE_KEY)
            if (snapshot.activatedAtMs != null) putLong(KEY_ACTIVATED_AT, snapshot.activatedAtMs) else remove(KEY_ACTIVATED_AT)
            putLong(KEY_EXPIRES_AT, snapshot.expiresAtMs ?: NO_EXPIRY)
            putStringSet(KEY_USED_KEY_HASHES, snapshot.usedKeyHashes)
            putString(KEY_VAULT_SEAL, seal)
        }.apply()
    }

    override fun isSealValid(): Boolean {
        val snapshot = readSnapshot() ?: return true
        val storedSeal = prefs.getString(KEY_VAULT_SEAL, null) ?: return false
        val expectedSeal = computeSeal(snapshot)
        return MessageDigest.isEqual(
            storedSeal.toByteArray(Charsets.UTF_8),
            expectedSeal.toByteArray(Charsets.UTF_8)
        )
    }

    private fun computeSeal(snapshot: VaultSnapshot): String {
        val sortedHashes = snapshot.usedKeyHashes.sorted().joinToString(",")
        val payload = buildString {
            append(deviceBindingId)
            append('|').append(snapshot.trialStartedAtMs)
            append('|').append(snapshot.lastSeenWallMs)
            append('|').append(snapshot.lastSeenElapsedMs)
            append('|').append(snapshot.activeKey.orEmpty())
            append('|').append(snapshot.activatedAtMs ?: -1L)
            append('|').append(snapshot.expiresAtMs ?: NO_EXPIRY)
            append('|').append(sortedHashes)
        }
        return LicenseManager.hmacSha256Hex(VAULT_HMAC_KEY, payload)
    }

    companion object {
        private const val PREFS_NAME = "tessera_license_vault"
        private const val KEY_TRIAL_STARTED_AT = "trial_started_at"
        private const val KEY_LAST_SEEN_WALL = "last_seen_wall_ms"
        private const val KEY_LAST_SEEN_ELAPSED = "last_seen_elapsed_ms"
        private const val KEY_ACTIVE_KEY = "active_key"
        private const val KEY_ACTIVATED_AT = "activated_at_ms"
        private const val KEY_EXPIRES_AT = "expires_at_ms"
        private const val KEY_USED_KEY_HASHES = "used_key_hashes"
        private const val KEY_VAULT_SEAL = "vault_seal"
        private const val NO_EXPIRY = -1L
        private const val VAULT_HMAC_KEY = "Tessera::Vault#Integrity!2026\$Seal"
    }
}

class LicenseManager(
    private val storage: LicenseStorage,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {
    constructor(context: Context) : this(
        storage = PrefsLicenseStorage(context.applicationContext),
        timeProvider = SystemTimeProvider()
    )

    companion object {
        internal const val BASE32_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private const val KEY_PREFIX = "TESS"
        private const val SECRET_KEY = "Tessera::2026::Edt0r1al#Launch!Vault\$K3y"
        private const val DAY_MS = 24L * 60L * 60L * 1000L
        private const val HOUR_MS = 60L * 60L * 1000L
        private const val CLOCK_ROLLBACK_TOLERANCE_MS = 5L * 60L * 1000L

        private val secureRandom = SecureRandom()

        fun normalizeKeyInput(raw: String): String {
            val cleaned = raw.uppercase(Locale.US).replace(Regex("[^A-Z0-9]"), "")
            return if (cleaned.length == 16 && cleaned.startsWith(KEY_PREFIX)) {
                "${cleaned.substring(0, 4)}-${cleaned.substring(4, 8)}-${cleaned.substring(8, 12)}-${cleaned.substring(12, 16)}"
            } else {
                raw.trim().uppercase(Locale.US)
            }
        }

        internal fun hmacSha256Bytes(secret: String, message: String): ByteArray {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
            return mac.doFinal(message.toByteArray(Charsets.UTF_8))
        }

        internal fun hmacSha256Hex(secret: String, message: String): String {
            val bytes = hmacSha256Bytes(secret, message)
            val sb = StringBuilder(bytes.size * 2)
            for (b in bytes) {
                sb.append(String.format(Locale.US, "%02x", b.toInt() and 0xFF))
            }
            return sb.toString()
        }

        internal fun sha256Hex(message: String): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(message.toByteArray(Charsets.UTF_8))
            val sb = StringBuilder(digest.size * 2)
            for (b in digest) {
                sb.append(String.format(Locale.US, "%02x", b.toInt() and 0xFF))
            }
            return sb.toString()
        }

        fun computeSignature(keyType: LicenseKeyType, data4: String): String {
            val digest = hmacSha256Bytes(SECRET_KEY, "${keyType.code}-$data4")
            val top24 = ((digest[0].toInt() and 0xFF) shl 16) or
                ((digest[1].toInt() and 0xFF) shl 8) or
                (digest[2].toInt() and 0xFF)
            val c0 = BASE32_ALPHABET[(top24 ushr 19) and 0x1F]
            val c1 = BASE32_ALPHABET[(top24 ushr 14) and 0x1F]
            val c2 = BASE32_ALPHABET[(top24 ushr 9) and 0x1F]
            val c3 = BASE32_ALPHABET[(top24 ushr 4) and 0x1F]
            return "$c0$c1$c2$c3"
        }

        fun generateKey(type: LicenseKeyType, giftDays: Int = BillingConfig.GIFT_MAX_DAYS): String {
            val data4 = buildString(4) {
                if (type == LicenseKeyType.GIFT) {
                    require(giftDays in BillingConfig.GIFT_MIN_DAYS..BillingConfig.GIFT_MAX_DAYS) {
                        "Chaves cortesia devem ter entre ${BillingConfig.GIFT_MIN_DAYS} e ${BillingConfig.GIFT_MAX_DAYS} dias."
                    }
                    append(('0'.code + giftDays).toChar())
                } else {
                    append(BASE32_ALPHABET[secureRandom.nextInt(BASE32_ALPHABET.length)])
                }
                while (length < 4) {
                    append(BASE32_ALPHABET[secureRandom.nextInt(BASE32_ALPHABET.length)])
                }
            }
            val sig4 = computeSignature(type, data4)
            return "$KEY_PREFIX-${type.code}-$data4-$sig4"
        }

        fun maskKey(normalizedKey: String): String {
            val parts = normalizedKey.split("-")
            return if (parts.size == 4) {
                "${parts[0]}-${parts[1]}-••••-${parts[3]}"
            } else {
                "TESS-••••"
            }
        }
    }

    data class ParsedKey(
        val normalizedKey: String,
        val type: LicenseKeyType,
        val data4: String,
        val durationDays: Int?
    )

    fun parseAndVerifyKey(rawKey: String): ParsedKey? {
        val normalized = normalizeKeyInput(rawKey)
        val parts = normalized.split("-")
        if (parts.size != 4 || parts[0] != KEY_PREFIX) return null

        val keyType = LicenseKeyType.fromCode(parts[1]) ?: return null
        val data4 = parts[2]
        val sig4 = parts[3]
        if (data4.length != 4 || sig4.length != 4) return null
        if (data4.any { it !in BASE32_ALPHABET } || sig4.any { it !in BASE32_ALPHABET }) return null

        val expectedSig = computeSignature(keyType, data4)
        val sigMatches = MessageDigest.isEqual(
            expectedSig.toByteArray(Charsets.US_ASCII),
            sig4.toByteArray(Charsets.US_ASCII)
        )
        if (!sigMatches) return null

        val durationDays = when (keyType) {
            LicenseKeyType.GIFT -> {
                val daysChar = data4[0]
                if (daysChar !in '3'..'7') return null
                val days = daysChar.digitToInt()
                if (days !in BillingConfig.GIFT_MIN_DAYS..BillingConfig.GIFT_MAX_DAYS) return null
                days
            }
            LicenseKeyType.MENS -> keyType.durationDays
            LicenseKeyType.SEMS -> keyType.durationDays
            LicenseKeyType.ANUL -> keyType.durationDays
            LicenseKeyType.LIFE, LicenseKeyType.MSTR -> null
        }

        return ParsedKey(
            normalizedKey = normalized,
            type = keyType,
            data4 = data4,
            durationDays = durationDays
        )
    }

    fun hasExistingVault(): Boolean = storage.readSnapshot() != null

    fun getLicenseStatus(): LicenseStatus {
        val nowWall = timeProvider.currentTimeMillis()
        val nowElapsed = timeProvider.elapsedRealtimeMillis()

        if (!storage.isSealValid()) {
            // Adulteração detectada fora do app no cofre XML: revoga acesso imediatamente até revalidação legítima
            val existingHashes = storage.readSnapshot()?.usedKeyHashes ?: emptySet()
            storage.writeSnapshot(
                VaultSnapshot(
                    trialStartedAtMs = nowWall - BillingConfig.TRIAL_DURATION_MS - DAY_MS,
                    lastSeenWallMs = nowWall,
                    lastSeenElapsedMs = nowElapsed,
                    activeKey = null,
                    activatedAtMs = null,
                    expiresAtMs = nowWall - 1L,
                    usedKeyHashes = existingHashes
                )
            )
            return LicenseStatus(
                tier = LicenseTier.FREE,
                hasProAccess = false,
                isMasterAccess = false,
                daysRemaining = 0,
                hoursRemaining = 0,
                remainingMillis = 0L,
                isClockTampered = false,
                isVaultTampered = true,
                expiresAtMs = null,
                activeKeyMasked = null,
                activationTypeLabel = "Tessera Free (Verificação necessária)"
            )
        }

        val current = storage.readSnapshot()
        val snapshot = if (current == null) {
            VaultSnapshot(
                trialStartedAtMs = nowWall,
                lastSeenWallMs = nowWall,
                lastSeenElapsedMs = nowElapsed,
                activeKey = null,
                activatedAtMs = null,
                expiresAtMs = null,
                usedKeyHashes = emptySet()
            ).also { storage.writeSnapshot(it) }
        } else {
            val highWaterWall = maxOf(current.lastSeenWallMs, nowWall)
            val updated = current.copy(
                lastSeenWallMs = highWaterWall,
                lastSeenElapsedMs = maxOf(current.lastSeenElapsedMs, nowElapsed)
            )
            if (updated != current) {
                storage.writeSnapshot(updated)
            }
            current
        }

        val clockTampered = nowWall < (snapshot.lastSeenWallMs - CLOCK_ROLLBACK_TOLERANCE_MS) ||
            nowWall < (snapshot.trialStartedAtMs - CLOCK_ROLLBACK_TOLERANCE_MS)

        val activeParsed = snapshot.activeKey?.let { parseAndVerifyKey(it) }
        if (activeParsed != null) {
            return when (activeParsed.type) {
                LicenseKeyType.MSTR -> LicenseStatus(
                    tier = LicenseTier.MASTER,
                    hasProAccess = true,
                    isMasterAccess = true,
                    daysRemaining = Int.MAX_VALUE,
                    hoursRemaining = Int.MAX_VALUE,
                    remainingMillis = Long.MAX_VALUE,
                    isClockTampered = false,
                    isVaultTampered = false,
                    expiresAtMs = null,
                    activeKeyMasked = maskKey(activeParsed.normalizedKey),
                    activationTypeLabel = "Master Desenvolvedor"
                )
                LicenseKeyType.LIFE -> LicenseStatus(
                    tier = LicenseTier.PRO_LIFETIME,
                    hasProAccess = true,
                    isMasterAccess = false,
                    daysRemaining = Int.MAX_VALUE,
                    hoursRemaining = Int.MAX_VALUE,
                    remainingMillis = Long.MAX_VALUE,
                    isClockTampered = false,
                    isVaultTampered = false,
                    expiresAtMs = null,
                    activeKeyMasked = maskKey(activeParsed.normalizedKey),
                    activationTypeLabel = "Tessera Pro Vitalício"
                )
                LicenseKeyType.GIFT, LicenseKeyType.MENS, LicenseKeyType.SEMS, LicenseKeyType.ANUL -> {
                    val tier = when (activeParsed.type) {
                        LicenseKeyType.GIFT -> LicenseTier.PRO_GIFT
                        LicenseKeyType.MENS -> LicenseTier.PRO_MONTHLY
                        LicenseKeyType.SEMS -> LicenseTier.PRO_SEMESTRAL
                        LicenseKeyType.ANUL -> LicenseTier.PRO_ANNUAL
                        else -> LicenseTier.PRO_GIFT
                    }
                    val expiry = snapshot.expiresAtMs ?: ((snapshot.activatedAtMs ?: nowWall) + (activeParsed.durationDays ?: 7) * DAY_MS)
                    val remaining = if (clockTampered) 0L else (expiry - nowWall).coerceAtLeast(0L)
                    if (!clockTampered && remaining > 0L) {
                        val days = (remaining / DAY_MS).toInt()
                        val hours = ((remaining % DAY_MS) / HOUR_MS).toInt()
                        val label = if (activeParsed.type == LicenseKeyType.GIFT) {
                            "Cortesia (${activeParsed.durationDays} dias)"
                        } else {
                            activeParsed.type.displayLabel
                        }
                        LicenseStatus(
                            tier = tier,
                            hasProAccess = true,
                            isMasterAccess = false,
                            daysRemaining = days,
                            hoursRemaining = hours,
                            remainingMillis = remaining,
                            isClockTampered = false,
                            isVaultTampered = false,
                            expiresAtMs = expiry,
                            activeKeyMasked = maskKey(activeParsed.normalizedKey),
                            activationTypeLabel = label
                        )
                    } else {
                        LicenseStatus(
                            tier = LicenseTier.FREE,
                            hasProAccess = false,
                            isMasterAccess = false,
                            daysRemaining = 0,
                            hoursRemaining = 0,
                            remainingMillis = 0L,
                            isClockTampered = clockTampered,
                            isVaultTampered = false,
                            expiresAtMs = expiry,
                            activeKeyMasked = maskKey(activeParsed.normalizedKey),
                            activationTypeLabel = if (clockTampered) "Relógio adulterado" else "Tessera Free"
                        )
                    }
                }
            }
        }

        // Sem chave ativa: avalia Trial automático de 3 dias (72h)
        val trialEnd = snapshot.trialStartedAtMs + BillingConfig.TRIAL_DURATION_MS
        val trialRemaining = if (clockTampered) 0L else (trialEnd - nowWall).coerceAtLeast(0L)
        return if (!clockTampered && trialRemaining > 0L) {
            val days = (trialRemaining / DAY_MS).toInt()
            val hours = ((trialRemaining % DAY_MS) / HOUR_MS).toInt()
            LicenseStatus(
                tier = LicenseTier.TRIAL,
                hasProAccess = true,
                isMasterAccess = false,
                daysRemaining = days,
                hoursRemaining = hours,
                remainingMillis = trialRemaining,
                isClockTampered = false,
                isVaultTampered = false,
                expiresAtMs = trialEnd,
                activeKeyMasked = null,
                activationTypeLabel = "Trial Pro (3 dias)"
            )
        } else {
            LicenseStatus(
                tier = LicenseTier.FREE,
                hasProAccess = false,
                isMasterAccess = false,
                daysRemaining = 0,
                hoursRemaining = 0,
                remainingMillis = 0L,
                isClockTampered = clockTampered,
                isVaultTampered = false,
                expiresAtMs = trialEnd,
                activeKeyMasked = null,
                activationTypeLabel = if (clockTampered) "Relógio adulterado" else "Tessera Free"
            )
        }
    }

    fun activateKey(rawKey: String): LicenseActivationResult {
        val parsed = parseAndVerifyKey(rawKey)
            ?: return LicenseActivationResult.InvalidKey(
                "Código inválido. Verifique os caracteres e use o formato TESS-XXXX-XXXX-XXXX."
            )

        val nowWall = timeProvider.currentTimeMillis()
        val nowElapsed = timeProvider.elapsedRealtimeMillis()

        val sealValid = storage.isSealValid()
        val current = if (sealValid) storage.readSnapshot() else null
        val keyHash = sha256Hex(parsed.normalizedKey)

        // Impede reutilização de chave temporária já consumida neste aparelho
        val existingHashes = current?.usedKeyHashes ?: emptySet()
        val isLifetimeOrMaster = parsed.type == LicenseKeyType.LIFE || parsed.type == LicenseKeyType.MSTR
        val isSameStillActiveKey = current?.activeKey == parsed.normalizedKey &&
            (current.expiresAtMs == null || nowWall < current.expiresAtMs)

        if (!isLifetimeOrMaster && existingHashes.contains(keyHash) && !isSameStillActiveKey) {
            return LicenseActivationResult.ExpiredKey(
                "Este código temporário já foi utilizado ou expirou neste dispositivo."
            )
        }

        if (isSameStillActiveKey && current != null) {
            val status = getLicenseStatus()
            return LicenseActivationResult.Success(
                status = status,
                message = "${status.activationTypeLabel} já está ativo."
            )
        }

        val durationDays = parsed.durationDays
        val newExpiresAt = if (durationDays != null) {
            nowWall + (durationDays * DAY_MS)
        } else {
            null
        }

        val updatedHashes = if (isLifetimeOrMaster) existingHashes else (existingHashes + keyHash)
        val baseTrialStart = current?.trialStartedAtMs ?: nowWall

        storage.writeSnapshot(
            VaultSnapshot(
                trialStartedAtMs = baseTrialStart,
                lastSeenWallMs = nowWall,
                lastSeenElapsedMs = nowElapsed,
                activeKey = parsed.normalizedKey,
                activatedAtMs = nowWall,
                expiresAtMs = newExpiresAt,
                usedKeyHashes = updatedHashes
            )
        )

        val newStatus = getLicenseStatus()
        val successMsg = when (parsed.type) {
            LicenseKeyType.MSTR -> "Licença Master ativada! IA e Gerador desbloqueados."
            LicenseKeyType.LIFE -> "Tessera Pro Vitalício ativado com sucesso!"
            LicenseKeyType.GIFT -> "Cortesia de ${parsed.durationDays} dias ativada!"
            else -> "${parsed.type.displayLabel} ativado com sucesso!"
        }
        return LicenseActivationResult.Success(status = newStatus, message = successMsg)
    }
}
