package com.tessera.launcher.ui.motion

/**
 * Funções matemáticas puras para física de animações do núcleo do Tessera Launcher.
 * Sem alocações por frame e desacopladas do ciclo de vida do Compose para testes unitários diretos.
 */
object MotionMath {

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        val f = fraction.coerceIn(0f, 1f)
        return start + (stop - start) * f
    }

    /**
     * Resistência elástica (rubber-band) para arrastes que ultrapassam o limiar [limit].
     * Monotônica e assintoticamente limitada para evitar deslocamentos excessivos.
     */
    fun rubberBand(offset: Float, limit: Float, factor: Float = 0.55f): Float {
        if (offset <= 0f) return 0f
        if (limit <= 0f) return 0f
        if (offset <= limit) return offset
        val extra = offset - limit
        val maxOvershoot = limit * 0.85f
        val damped = (extra * factor) / (1f + (extra * factor) / maxOvershoot)
        return limit + damped
    }

    /**
     * Progresso escalonado (cascata) para os primeiros [staggerMaxItems] itens visíveis da gaveta.
     * Itens além de [staggerMaxItems] (ou quando staggerStepMillis == 0) acompanham diretamente [drawerProgress].
     */
    fun drawerCascadeProgress(
        drawerProgress: Float,
        index: Int,
        staggerStepMillis: Int,
        staggerMaxItems: Int,
        totalDurationMillis: Float = 260f
    ): Float {
        val p = drawerProgress.coerceIn(0f, 1f)
        if (p <= 0f) return 0f
        if (p >= 1f) return 1f
        if (staggerStepMillis <= 0 || staggerMaxItems <= 0 || index < 0 || index >= staggerMaxItems) {
            return p
        }
        val start = ((index * staggerStepMillis) / totalDurationMillis).coerceIn(0f, 0.55f)
        val span = (1f - start).coerceAtLeast(0.15f)
        val t = ((p - start) / span).coerceIn(0f, 1f)
        val inv = 1f - t
        return 1f - inv * inv * inv
    }

    /**
     * Escala da prévia de Predictive Back dado o progresso do gesto (0f..1f).
     */
    fun backProgressToScale(backProgress: Float, minScale: Float): Float {
        return lerp(1f, minScale, backProgress)
    }
}
