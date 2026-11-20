package com.tessera.launcher.data.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

private const val PHOTO_FILE_PREFIX = "photo_widget_"
private const val PHOTO_FILE_LEGACY = "photo_widget.jpg"
private const val PHOTO_GALLERY_DIR = "photo_widget_gallery"
private const val PHOTO_THUMBS_DIR = "photo_widget_thumbs"
private const val WIDGET_THUMB_MAX_PX = 560
private const val STANDBY_FULL_MAX_PX = 1440

/**
 * Cache em memória compartilhado entre MainViewModel, PhotoWidget e PhotoStandByModal.
 * Permite renderização instantânea no 1º frame sem aguardar recomposição de `produceState`.
 */
object PhotoBitmapCache {
    private val thumbCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    private val fullCache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    fun getThumb(key: String?): Bitmap? {
        if (key.isNullOrBlank()) return null
        return thumbCache.get(key)?.takeIf { !it.isRecycled }
    }

    fun putThumb(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            thumbCache.put(key, bitmap)
        }
    }

    fun getFull(key: String?): Bitmap? {
        if (key.isNullOrBlank()) return null
        return fullCache.get(key)?.takeIf { !it.isRecycled }
    }

    fun putFull(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            fullCache.put(key, bitmap)
        }
    }

    fun evict(key: String?) {
        if (key.isNullOrBlank()) return
        thumbCache.remove(key)
        fullCache.remove(key)
    }

    fun clear() {
        thumbCache.evictAll()
        fullCache.evictAll()
    }
}

private fun getGalleryDir(context: Context): File {
    return File(context.filesDir, PHOTO_GALLERY_DIR).apply { if (!exists()) mkdirs() }
}

private fun getThumbsDir(context: Context): File {
    return File(context.filesDir, PHOTO_THUMBS_DIR).apply { if (!exists()) mkdirs() }
}

private fun getThumbFileForPath(context: Context, photoUriOrPath: String): File {
    val safeHash = photoUriOrPath.hashCode().toUInt().toString(16)
    return File(getThumbsDir(context), "thumb_${safeHash}.jpg")
}

private fun scaleDownBitmap(source: Bitmap, maxDimensionPx: Int): Bitmap {
    val largest = max(source.width, source.height)
    if (largest <= maxDimensionPx || source.width <= 0 || source.height <= 0) return source
    val ratio = maxDimensionPx.toFloat() / largest.toFloat()
    val targetW = (source.width * ratio).roundToInt().coerceAtLeast(1)
    val targetH = (source.height * ratio).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(source, targetW, targetH, true)
}

/**
 * Salva uma foto otimizada no armazenamento interno já redimensionada e com rotação EXIF aplicada,
 * gerando também uma miniatura pré-assada (~35-60 KB) para carregamento instantâneo no boot.
 */
fun savePhotoToInternalStorage(context: Context, uri: Uri): String? {
    return runCatching {
        val galleryDir = getGalleryDir(context)
        val targetFile = File(galleryDir, "pw_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")

        val decodedFull = loadSampledPhotoBitmap(context, uri.toString(), STANDBY_FULL_MAX_PX)
        if (decodedFull != null) {
            FileOutputStream(targetFile).use { out ->
                decodedFull.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val thumbBitmap = scaleDownBitmap(decodedFull, WIDGET_THUMB_MAX_PX)
            val thumbFile = getThumbFileForPath(context, targetFile.absolutePath)
            FileOutputStream(thumbFile).use { out ->
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
            }
            PhotoBitmapCache.putFull(targetFile.absolutePath, decodedFull)
            PhotoBitmapCache.putThumb(targetFile.absolutePath, thumbBitmap)
            return@runCatching targetFile.absolutePath
        }

        // Fallback: cópia direta caso o decoder não tenha processado o stream
        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
            true
        } ?: false

        if (!copied || !targetFile.exists() || targetFile.length() == 0L) {
            runCatching { targetFile.delete() }
            return@runCatching null
        }

        targetFile.absolutePath
    }.getOrNull()
}

/**
 * Salva múltiplas fotos (até [MAX_PHOTO_WIDGET_COUNT]) de forma otimizada no armazenamento interno.
 */
fun savePhotosToInternalStorage(
    context: Context,
    uris: List<Uri>,
    maxCount: Int = MAX_PHOTO_WIDGET_COUNT
): List<String> {
    if (uris.isEmpty() || maxCount <= 0) return emptyList()
    return uris
        .take(maxCount)
        .mapNotNull { uri -> savePhotoToInternalStorage(context, uri) }
}

/**
 * Remove os arquivos de uma foto específica (versão full e miniatura) e limpa seu cache.
 */
fun deleteSinglePhotoFiles(context: Context, photoPath: String?) {
    if (photoPath.isNullOrBlank()) return
    PhotoBitmapCache.evict(photoPath)
    runCatching {
        getThumbFileForPath(context, photoPath).takeIf { it.exists() }?.delete()
        val file = resolveLocalPhotoFile(photoPath)
        if (file != null && file.exists()) {
            val filesDirPath = context.filesDir.absolutePath
            if (file.absolutePath.startsWith(filesDirPath)) {
                file.delete()
            }
        }
    }
}

/**
 * Remove todas as fotos salvas do widget / modo repouso no armazenamento interno.
 */
fun deleteSavedPhotoFiles(context: Context) {
    PhotoBitmapCache.clear()
    runCatching {
        context.filesDir.listFiles()?.forEach { file ->
            if (file.name.startsWith(PHOTO_FILE_PREFIX) || file.name == PHOTO_FILE_LEGACY) {
                runCatching { file.delete() }
            }
        }
        getGalleryDir(context).listFiles()?.forEach { runCatching { it.delete() } }
        getThumbsDir(context).listFiles()?.forEach { runCatching { it.delete() } }
    }
}

/**
 * Caminho rápido para o Widget de Fotos:
 * 1) Cache em RAM (0 ms)
 * 2) Miniatura pré-assada e já rotacionada (~35-60 KB -> ~3 ms)
 * 3) Fallback + migração transparente (gera a miniatura pré-assada para o próximo boot)
 */
fun loadWidgetThumbFast(context: Context, photoUriOrPath: String?): Bitmap? {
    if (photoUriOrPath.isNullOrBlank()) return null

    PhotoBitmapCache.getThumb(photoUriOrPath)?.let { return it }

    val thumbFile = getThumbFileForPath(context, photoUriOrPath)
    if (thumbFile.exists() && thumbFile.canRead() && thumbFile.length() > 0L) {
        val fastBitmap = runCatching {
            BitmapFactory.decodeFile(
                thumbFile.absolutePath,
                BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
            )
        }.getOrNull()
        if (fastBitmap != null && !fastBitmap.isRecycled) {
            PhotoBitmapCache.putThumb(photoUriOrPath, fastBitmap)
            return fastBitmap
        }
    }

    // Fallback (ex.: foto legada antes da otimização ou URI content://) — assa a miniatura agora
    val sampled = loadSampledPhotoBitmap(context, photoUriOrPath, WIDGET_THUMB_MAX_PX) ?: return null
    runCatching {
        FileOutputStream(thumbFile).use { out ->
            sampled.compress(Bitmap.CompressFormat.JPEG, 86, out)
        }
    }
    PhotoBitmapCache.putThumb(photoUriOrPath, sampled)
    return sampled
}

/**
 * Pré-aquece o cache de miniaturas no boot priorizando a foto ativa.
 */
fun preloadWidgetThumbs(context: Context, paths: List<String>, activeIndex: Int = 0) {
    if (paths.isEmpty()) return
    val clamped = PhotoWidgetGalleryHelper.clampActiveIndex(activeIndex, paths.size)
    // Carrega a foto visível primeiro para exibição imediata
    loadWidgetThumbFast(context, paths[clamped])
    paths.forEachIndexed { idx, path ->
        if (idx != clamped) {
            loadWidgetThumbFast(context, path)
        }
    }
}

/**
 * Carrega um Bitmap em alta resolução para StandBy, aproveitando o cache em memória quando disponível.
 */
fun loadStandByPhotoFast(context: Context, photoUriOrPath: String?): Bitmap? {
    if (photoUriOrPath.isNullOrBlank()) return null
    PhotoBitmapCache.getFull(photoUriOrPath)?.let { return it }
    val loaded = loadSampledPhotoBitmap(context, photoUriOrPath, STANDBY_FULL_MAX_PX) ?: return null
    PhotoBitmapCache.putFull(photoUriOrPath, loaded)
    return loaded
}

/**
 * Carrega um Bitmap com downsampling (`inSampleSize`) e correção de orientação EXIF,
 * suportando caminhos locais (`File`), URIs `file://` e URIs `content://`.
 */
fun loadSampledPhotoBitmap(
    context: Context,
    photoUriOrPath: String?,
    maxDimensionPx: Int = 1440
): Bitmap? {
    if (photoUriOrPath.isNullOrBlank()) return null

    return runCatching {
        val localFile = resolveLocalPhotoFile(photoUriOrPath)
        if (localFile != null) {
            if (!localFile.exists() || !localFile.canRead()) return@runCatching null

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(localFile.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculatePhotoSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeFile(localFile.absolutePath, options) ?: return@runCatching null
            val orientation = runCatching {
                ExifInterface(localFile.absolutePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

            applyExifOrientation(decoded, orientation)
        } else {
            val uri = Uri.parse(photoUriOrPath)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculatePhotoSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@runCatching null

            val orientation = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

            applyExifOrientation(decoded, orientation)
        }
    }.getOrNull()
}

private fun resolveLocalPhotoFile(photoUriOrPath: String): File? {
    return when {
        photoUriOrPath.startsWith("/") -> File(photoUriOrPath)
        photoUriOrPath.startsWith("file://") -> Uri.parse(photoUriOrPath).path?.let { File(it) }
        else -> null
    }
}

private fun calculatePhotoSampleSize(width: Int, height: Int, maxDimensionPx: Int): Int {
    var inSampleSize = 1
    val largest = max(width, height)
    while (largest / inSampleSize > maxDimensionPx) {
        inSampleSize *= 2
    }
    return inSampleSize.coerceAtLeast(1)
}

private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f)
            matrix.postScale(-1f, 1f)
        }
        else -> return bitmap
    }

    val rotated = runCatching {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.getOrNull() ?: return bitmap

    if (rotated != bitmap && !bitmap.isRecycled) {
        bitmap.recycle()
    }
    return rotated
}
