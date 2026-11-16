package com.tessera.launcher.data.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

private const val PHOTO_FILE_PREFIX = "photo_widget_"
private const val PHOTO_FILE_LEGACY = "photo_widget.jpg"

/**
 * Copia a imagem selecionada no Photo Picker para o armazenamento interno do app,
 * evitando perda de permissão temporária de URIs `content://` após reinicializações.
 */
fun savePhotoToInternalStorage(context: Context, uri: Uri): String? {
    return runCatching {
        val targetFile = File(context.filesDir, "${PHOTO_FILE_PREFIX}${System.currentTimeMillis()}.jpg")
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

        // Remove arquivos antigos após copiar o novo com sucesso
        context.filesDir.listFiles()?.forEach { file ->
            if (file.absolutePath != targetFile.absolutePath &&
                (file.name.startsWith(PHOTO_FILE_PREFIX) || file.name == PHOTO_FILE_LEGACY)
            ) {
                runCatching { file.delete() }
            }
        }

        targetFile.absolutePath
    }.getOrNull()
}

/**
 * Remove as fotos salvas do widget / modo repouso no armazenamento interno.
 */
fun deleteSavedPhotoFiles(context: Context) {
    runCatching {
        context.filesDir.listFiles()?.forEach { file ->
            if (file.name.startsWith(PHOTO_FILE_PREFIX) || file.name == PHOTO_FILE_LEGACY) {
                runCatching { file.delete() }
            }
        }
    }
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
