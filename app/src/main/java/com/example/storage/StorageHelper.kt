package com.example.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.MediaType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat

object StorageHelper {
  private const val TAG = "StorageHelper"

  /**
   * Returns available disk space in bytes on primary storage
   */
  fun getAvailableStorageBytes(context: Context): Long {
    return try {
      val path = context.getExternalFilesDir(null) ?: context.filesDir
      val stat = StatFs(path.path)
      stat.availableBlocksLong * stat.blockSizeLong
    } catch (e: Exception) {
      Log.e(TAG, "Error checking storage space", e)
      1024L * 1024L * 500L // 500 MB fallback assumption
    }
  }

  /**
   * Format bytes to readable string (e.g. 42.6 MB, 1.2 GB)
   */
  fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    val df = DecimalFormat("#,##0.#")
    return "${df.format(value)} ${units[digitGroups]}"
  }

  /**
   * Safe filename sanitization
   */
  fun sanitizeFilename(name: String, fallbackExtension: String): String {
    var clean = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    if (clean.isBlank()) {
      clean = "media_${System.currentTimeMillis()}"
    }
    if (!clean.contains(".")) {
      clean = "$clean.$fallbackExtension"
    }
    return clean
  }

  /**
   * Saves downloaded media into Android's public MediaStore collections:
   * - Videos: Movies/LinkDrop/Videos
   * - Images: Pictures/LinkDrop/Images
   * - Thumbnails: Pictures/LinkDrop/Thumbnails
   * - Audio: Music/LinkDrop/Audio
   *
   * Files appear automatically in Gallery, Google Photos, Files, and Music apps.
   */
  fun saveStreamToMediaStore(
    context: Context,
    inputStream: InputStream,
    filename: String,
    mediaType: MediaType,
    mimeType: String,
    totalBytesExpected: Long,
    onProgressUpdate: (downloadedBytes: Long) -> Unit
  ): Uri? {
    val resolver = context.contentResolver
    val cleanName = sanitizeFilename(filename, defaultExtensionFor(mediaType))

    val (contentUri, relativePath) = when (mediaType) {
      MediaType.VIDEO -> Pair(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        },
        "Movies/LinkDrop/Videos"
      )
      MediaType.IMAGE -> Pair(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        },
        "Pictures/LinkDrop/Images"
      )
      MediaType.THUMBNAIL -> Pair(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        },
        "Pictures/LinkDrop/Thumbnails"
      )
      MediaType.AUDIO -> Pair(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        },
        "Music/LinkDrop/Audio"
      )
    }

    val values = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
      put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        put(MediaStore.MediaColumns.IS_PENDING, 1)
      }
    }

    var insertedUri: Uri? = null
    var outputStream: OutputStream? = null

    try {
      insertedUri = resolver.insert(contentUri, values)
      if (insertedUri == null) {
        Log.e(TAG, "Failed to create MediaStore entry for $cleanName, using app-specific fallback")
        return saveToAppFilesFallback(context, inputStream, cleanName, relativePath, onProgressUpdate)
      }

      outputStream = resolver.openOutputStream(insertedUri)
      if (outputStream == null) {
        throw IllegalStateException("Unable to open output stream for $insertedUri")
      }

      val buffer = ByteArray(8192)
      var bytesRead: Int
      var totalDownloaded: Long = 0

      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
        totalDownloaded += bytesRead
        onProgressUpdate(totalDownloaded)
      }

      outputStream.flush()

      // Mark IS_PENDING = 0 so Gallery immediately indexes it
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(insertedUri, values, null, null)
      }

      Log.d(TAG, "Successfully saved $cleanName to MediaStore: $insertedUri")
      return insertedUri
    } catch (e: Exception) {
      Log.e(TAG, "Error saving to MediaStore", e)
      if (insertedUri != null) {
        try {
          resolver.delete(insertedUri, null, null)
        } catch (_: Exception) {}
      }
      return null
    } finally {
      try { outputStream?.close() } catch (_: Exception) {}
      try { inputStream.close() } catch (_: Exception) {}
    }
  }

  private fun saveToAppFilesFallback(
    context: Context,
    inputStream: InputStream,
    filename: String,
    subDir: String,
    onProgressUpdate: (downloadedBytes: Long) -> Unit
  ): Uri? {
    try {
      val baseDir = File(context.getExternalFilesDir(null) ?: context.filesDir, subDir)
      if (!baseDir.exists()) baseDir.mkdirs()
      val targetFile = File(baseDir, filename)
      val output = FileOutputStream(targetFile)
      val buffer = ByteArray(8192)
      var read: Int
      var total: Long = 0
      while (inputStream.read(buffer).also { read = it } != -1) {
        output.write(buffer, 0, read)
        total += read
        onProgressUpdate(total)
      }
      output.flush()
      output.close()

      return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        targetFile
      )
    } catch (e: Exception) {
      Log.e(TAG, "Fallback file save failed", e)
      return null
    }
  }

  private fun defaultExtensionFor(mediaType: MediaType): String {
    return when (mediaType) {
      MediaType.VIDEO -> "mp4"
      MediaType.IMAGE -> "jpg"
      MediaType.THUMBNAIL -> "jpg"
      MediaType.AUDIO -> "mp3"
    }
  }

  /**
   * Opens media file with Android's system viewer (Gallery, Video player, Music player)
   */
  fun openMedia(context: Context, uriString: String, mimeType: String?) {
    try {
      val uri = Uri.parse(uriString)
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Error opening media", e)
      Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Shares media file via Android Share sheet
   */
  fun shareMedia(context: Context, uriString: String, mimeType: String?, title: String) {
    try {
      val uri = Uri.parse(uriString)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType ?: "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(shareIntent, "Share Media")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Log.e(TAG, "Error sharing media", e)
      Toast.makeText(context, "Unable to share media file", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Deletes media file from device storage
   */
  fun deleteMediaFile(context: Context, uriString: String, filePath: String?): Boolean {
    var deleted = false
    try {
      val uri = Uri.parse(uriString)
      val rows = context.contentResolver.delete(uri, null, null)
      if (rows > 0) deleted = true
    } catch (e: Exception) {
      Log.w(TAG, "Could not delete via contentResolver: ${e.message}")
    }

    if (filePath != null) {
      try {
        val f = File(filePath)
        if (f.exists() && f.delete()) {
          deleted = true
        }
      } catch (e: Exception) {
        Log.w(TAG, "Could not delete file path: ${e.message}")
      }
    }
    return deleted
  }
}
