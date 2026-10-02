package com.example.download

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.example.database.AppDatabase
import com.example.database.DownloadHistoryEntity
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.notifications.DownloadNotificationHelper
import com.example.settings.SettingsManager
import com.example.storage.StorageHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class DownloadStatus {
  QUEUED,
  DOWNLOADING,
  PAUSED,
  COMPLETED,
  FAILED,
  CANCELLED
}

data class DownloadJob(
  val id: String,
  val title: String,
  val sourceUrl: String,
  val platform: PlatformType,
  val mediaType: MediaType,
  val mimeType: String,
  val progress: Float = 0f,
  val downloadedBytes: Long = 0L,
  val totalBytes: Long = 0L,
  val speedText: String = "0 KB/s",
  val remainingTimeText: String = "",
  val status: DownloadStatus = DownloadStatus.QUEUED,
  val errorMessage: String? = null,
  val localUriString: String? = null,
  val thumbnailUrl: String = ""
)

class DownloadEngine(
  private val context: Context,
  private val database: AppDatabase,
  private val settingsManager: SettingsManager
) {
  private val scope = CoroutineScope(Dispatchers.IO + Job())
  private val activeJobs = ConcurrentHashMap<String, Job>()

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  private val _jobs = MutableStateFlow<List<DownloadJob>>(emptyList())
  val jobs: StateFlow<List<DownloadJob>> = _jobs.asStateFlow()

  fun startDownload(
    title: String,
    sourceUrl: String,
    platform: PlatformType,
    mediaType: MediaType,
    mimeType: String = defaultMime(mediaType),
    thumbnailUrl: String = ""
  ): String {
    val id = "dl_${System.currentTimeMillis()}_${(100..999).random()}"
    val job = DownloadJob(
      id = id,
      title = title,
      sourceUrl = sourceUrl,
      platform = platform,
      mediaType = mediaType,
      mimeType = mimeType,
      thumbnailUrl = thumbnailUrl,
      status = DownloadStatus.QUEUED
    )

    updateJob(job)
    launchDownload(job)
    return id
  }

  fun pauseDownload(id: String) {
    activeJobs[id]?.cancel()
    activeJobs.remove(id)
    val current = _jobs.value.firstOrNull { it.id == id } ?: return
    updateJob(current.copy(status = DownloadStatus.PAUSED, speedText = "Paused"))
    DownloadNotificationHelper.cancelNotification(context, id.hashCode())
  }

  fun resumeDownload(id: String) {
    val current = _jobs.value.firstOrNull { it.id == id } ?: return
    updateJob(current.copy(status = DownloadStatus.QUEUED, errorMessage = null))
    launchDownload(current)
  }

  fun cancelDownload(id: String) {
    activeJobs[id]?.cancel()
    activeJobs.remove(id)
    _jobs.value = _jobs.value.filterNot { it.id == id }
    DownloadNotificationHelper.cancelNotification(context, id.hashCode())
  }

  fun retryDownload(id: String) {
    val current = _jobs.value.firstOrNull { it.id == id } ?: return
    updateJob(current.copy(status = DownloadStatus.QUEUED, errorMessage = null, progress = 0f))
    launchDownload(current)
  }

  private fun launchDownload(job: DownloadJob) {
    val coroutineJob = scope.launch {
      runDownloadTask(job)
    }
    activeJobs[job.id] = coroutineJob
  }

  private suspend fun runDownloadTask(job: DownloadJob) {
    updateJob(job.copy(status = DownloadStatus.DOWNLOADING))

    // 1. Check Wi-Fi settings
    if (settingsManager.settings.value.wifiOnly && !isWifiConnected()) {
      failJob(job, "Wi-Fi is required by settings but not connected.")
      return
    }

    // 2. Check Storage Space
    val availableSpace = StorageHelper.getAvailableStorageBytes(context)
    if (availableSpace < 20L * 1024L * 1024L) { // minimum 20MB safety margin
      failJob(job, "Not enough storage space.")
      return
    }

    try {
      // Connect to URL
      val request = Request.Builder()
        .url(job.sourceUrl)
        .header("User-Agent", "Mozilla/5.0 LinkDrop/2.4 Android")
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        failJob(job, "Download server returned HTTP ${response.code}")
        return
      }

      val body = response.body ?: run {
        failJob(job, "Empty response body from media server")
        return
      }

      val contentLength = body.contentLength()
      val stream: InputStream = body.byteStream()

      var lastTime = System.currentTimeMillis()
      var lastDownloaded: Long = 0
      var smoothedSpeed: Double = 0.0

      val savedUri: Uri? = StorageHelper.saveStreamToMediaStore(
        context = context,
        inputStream = stream,
        filename = job.title,
        mediaType = job.mediaType,
        mimeType = job.mimeType,
        totalBytesExpected = contentLength,
        onProgressUpdate = { downloaded ->
          val now = System.currentTimeMillis()
          val elapsed = (now - lastTime)
          if (elapsed >= 300) {
            val bytesDiff = downloaded - lastDownloaded
            val currentSpeed = (bytesDiff.toDouble() / (elapsed / 1000.0))
            smoothedSpeed = if (smoothedSpeed == 0.0) currentSpeed else (smoothedSpeed * 0.7 + currentSpeed * 0.3)
            val speedStr = "${StorageHelper.formatBytes(smoothedSpeed.toLong())}/s"

            val progress = if (contentLength > 0) (downloaded.toFloat() / contentLength).coerceIn(0f, 1f) else 0.5f

            val remainingTime = if (contentLength > 0 && smoothedSpeed > 100) {
              val remainingBytes = contentLength - downloaded
              val sec = (remainingBytes / smoothedSpeed).toInt()
              if (sec < 60) "${sec}s left" else "${sec / 60}m left"
            } else ""

            updateJob(
              job.copy(
                status = DownloadStatus.DOWNLOADING,
                progress = progress,
                downloadedBytes = downloaded,
                totalBytes = contentLength,
                speedText = speedStr,
                remainingTimeText = remainingTime
              )
            )

            DownloadNotificationHelper.showProgressNotification(
              context = context,
              notificationId = job.id.hashCode(),
              filename = job.title,
              progressPercent = (progress * 100).toInt(),
              speed = speedStr,
              downloadedFormatted = "${StorageHelper.formatBytes(downloaded)} / ${if (contentLength > 0) StorageHelper.formatBytes(contentLength) else "..."}"
            )

            lastTime = now
            lastDownloaded = downloaded
          }
        }
      )

      if (savedUri != null) {
        val finalSize = if (contentLength > 0) contentLength else lastDownloaded
        val formattedFinal = StorageHelper.formatBytes(finalSize)

        // Record to Room Database
        database.downloadHistoryDao().insert(
          DownloadHistoryEntity(
            id = job.id,
            filename = job.title,
            sourceUrl = job.sourceUrl,
            platform = job.platform.displayName,
            mediaType = job.mediaType.name,
            quality = "Original",
            fileSize = finalSize,
            formattedSize = formattedFinal,
            downloadDate = System.currentTimeMillis(),
            localUriString = savedUri.toString(),
            thumbnailUri = job.thumbnailUrl
          )
        )

        updateJob(
          job.copy(
            status = DownloadStatus.COMPLETED,
            progress = 1.0f,
            downloadedBytes = finalSize,
            totalBytes = finalSize,
            speedText = "Completed",
            remainingTimeText = "",
            localUriString = savedUri.toString()
          )
        )

        // Show completed notification
        if (settingsManager.settings.value.notifyOnComplete) {
          DownloadNotificationHelper.showCompletedNotification(
            context = context,
            notificationId = job.id.hashCode(),
            filename = job.title,
            fileUri = savedUri,
            mimeType = job.mimeType
          )
        }
      } else {
        failJob(job, "Failed to write media file to storage.")
      }
    } catch (e: CancellationException) {
      Log.d("DownloadEngine", "Job ${job.id} cancelled or paused.")
    } catch (e: Exception) {
      Log.e("DownloadEngine", "Download failed", e)
      failJob(job, e.localizedMessage ?: "Network connection failed.")
    } finally {
      activeJobs.remove(job.id)
    }
  }

  private fun failJob(job: DownloadJob, reason: String) {
    updateJob(
      job.copy(
        status = DownloadStatus.FAILED,
        errorMessage = reason,
        speedText = "Failed"
      )
    )
    DownloadNotificationHelper.cancelNotification(context, job.id.hashCode())
  }

  private fun updateJob(updated: DownloadJob) {
    val list = _jobs.value.toMutableList()
    val index = list.indexOfFirst { it.id == updated.id }
    if (index >= 0) {
      list[index] = updated
    } else {
      list.add(0, updated)
    }
    _jobs.value = list
  }

  private fun isWifiConnected(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  }

  private fun defaultMime(type: MediaType): String {
    return when (type) {
      MediaType.VIDEO -> "video/mp4"
      MediaType.IMAGE -> "image/jpeg"
      MediaType.THUMBNAIL -> "image/jpeg"
      MediaType.AUDIO -> "audio/mpeg"
    }
  }
}
