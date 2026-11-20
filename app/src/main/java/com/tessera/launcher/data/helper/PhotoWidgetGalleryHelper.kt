package com.tessera.launcher.data.helper

const val MAX_PHOTO_WIDGET_COUNT = 6

/**
 * Funções puras para gerenciar a galeria de até 6 fotos do Widget de Fotos.
 * Desacopladas do Android SDK para testes unitários determinísticos.
 */
object PhotoWidgetGalleryHelper {

    fun normalizeGallery(paths: List<String>): List<String> {
        if (paths.isEmpty()) return emptyList()
        return paths
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_PHOTO_WIDGET_COUNT)
            .toList()
    }

    fun mergePhotos(existing: List<String>, additions: List<String>): List<String> {
        return normalizeGallery(existing + additions)
    }

    fun remainingSlots(existingCount: Int): Int {
        return (MAX_PHOTO_WIDGET_COUNT - existingCount.coerceAtLeast(0)).coerceIn(0, MAX_PHOTO_WIDGET_COUNT)
    }

    fun clampActiveIndex(index: Int, size: Int): Int {
        if (size <= 0) return 0
        return index.coerceIn(0, size - 1)
    }

    fun removePhotoAt(existing: List<String>, index: Int, currentActiveIndex: Int): Pair<List<String>, Int> {
        val normalized = normalizeGallery(existing)
        if (index !in normalized.indices) {
            return normalized to clampActiveIndex(currentActiveIndex, normalized.size)
        }
        val updated = normalized.toMutableList().apply { removeAt(index) }.toList()
        val nextActive = when {
            updated.isEmpty() -> 0
            currentActiveIndex > index -> currentActiveIndex - 1
            else -> currentActiveIndex
        }
        return updated to clampActiveIndex(nextActive, updated.size)
    }
}
