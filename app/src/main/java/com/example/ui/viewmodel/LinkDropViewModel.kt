package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.database.AppDatabase
import com.example.database.DownloadHistoryEntity
import com.example.download.DownloadEngine
import com.example.download.DownloadJob
import com.example.imageprocessing.EnhanceQualityEngine
import com.example.model.AudioQualityOption
import com.example.model.BatchItem
import com.example.model.ImageQualityOption
import com.example.model.MediaAnalyzeResult
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.model.VideoQualityOption
import com.example.notifications.DownloadNotificationHelper
import com.example.providers.ProviderRegistry
import com.example.settings.AppSettings
import com.example.settings.SettingsManager
import com.example.storage.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
  object Idle : AnalysisUiState()
  data class Loading(val url: String) : AnalysisUiState()
  data class Success(val media: MediaAnalyzeResult) : AnalysisUiState()
  data class Error(val message: String) : AnalysisUiState()
}

class LinkDropViewModel(application: Application) : AndroidViewModel(application) {

  private val database = Room.databaseBuilder(
    application.applicationContext,
    AppDatabase::class.java,
    "linkdrop_app.db"
  ).fallbackToDestructiveMigration().build()

  val settingsManager = SettingsManager(application.applicationContext)
  val downloadEngine = DownloadEngine(application.applicationContext, database, settingsManager)

  val historyList: StateFlow<List<DownloadHistoryEntity>> = database.downloadHistoryDao()
    .getAllHistory()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentHistoryList: StateFlow<List<DownloadHistoryEntity>> = database.downloadHistoryDao()
    .getRecentHistory(5)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeJobs: StateFlow<List<DownloadJob>> = downloadEngine.jobs

  val appSettings: StateFlow<AppSettings> = settingsManager.settings

  private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
  val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

  init {
    DownloadNotificationHelper.initChannel(application.applicationContext)
    seedInitialHistoryIfEmpty()
  }

  private fun seedInitialHistoryIfEmpty() {
    viewModelScope.launch(Dispatchers.IO) {
      val existing = database.downloadHistoryDao().getAllHistory().first()
      if (existing.isEmpty()) {
        // Pre-seed with the reference screenshot items
        com.example.data.MockDataProvider.initialRecentDownloads.forEach { item ->
          database.downloadHistoryDao().insert(
            DownloadHistoryEntity(
              id = item.id,
              filename = item.title,
              sourceUrl = "https://example.com/${item.title}",
              platform = item.platform.displayName,
              mediaType = item.mediaType.name,
              quality = "1080p",
              fileSize = 44670000L,
              formattedSize = item.fileSize,
              downloadDate = System.currentTimeMillis() - 600000L,
              localUriString = item.thumbnailUrl,
              thumbnailUri = item.thumbnailUrl,
              duration = item.duration
            )
          )
        }
      }
    }
  }

  fun analyzeUrl(url: String) {
    if (url.isBlank()) {
      _analysisState.value = AnalysisUiState.Error("Please enter a valid link.")
      return
    }
    _analysisState.value = AnalysisUiState.Loading(url)
    viewModelScope.launch {
      val result = ProviderRegistry.analyzeUrl(url)
      if (result.isSuccess && result.media != null) {
        _analysisState.value = AnalysisUiState.Success(result.media)
      } else {
        _analysisState.value = AnalysisUiState.Error(result.errorMessage ?: "Unable to analyze this link.")
      }
    }
  }

  fun clearAnalysis() {
    _analysisState.value = AnalysisUiState.Idle
  }

  fun startVideoDownload(media: MediaAnalyzeResult, quality: VideoQualityOption, format: String) {
    val cleanName = "${media.title.take(30).trim().replace(" ", "_")}_${quality.resolution}.${format.lowercase()}"
    val mime = if (format.equals("webm", ignoreCase = true)) "video/webm" else "video/mp4"

    downloadEngine.startDownload(
      title = cleanName,
      sourceUrl = media.originalUrl,
      platform = media.platform,
      mediaType = MediaType.VIDEO,
      mimeType = mime,
      thumbnailUrl = media.thumbnailUrl
    )
  }

  fun startImageDownload(media: MediaAnalyzeResult, quality: ImageQualityOption, format: String) {
    val cleanName = "${media.title.take(30).trim().replace(" ", "_")}.${format.lowercase()}"
    val mime = when (format.lowercase()) {
      "png" -> "image/png"
      "webp" -> "image/webp"
      else -> "image/jpeg"
    }

    downloadEngine.startDownload(
      title = cleanName,
      sourceUrl = media.originalUrl,
      platform = media.platform,
      mediaType = MediaType.IMAGE,
      mimeType = mime,
      thumbnailUrl = media.thumbnailUrl
    )
  }

  fun startAudioDownload(media: MediaAnalyzeResult, quality: AudioQualityOption) {
    val cleanName = "${media.title.take(30).trim().replace(" ", "_")}.${quality.format.lowercase().take(3)}"
    val mime = when (quality.format.lowercase().take(3)) {
      "fla" -> "audio/flac"
      "m4a" -> "audio/mp4"
      "wav" -> "audio/wav"
      else -> "audio/mpeg"
    }

    downloadEngine.startDownload(
      title = cleanName,
      sourceUrl = media.originalUrl,
      platform = media.platform,
      mediaType = MediaType.AUDIO,
      mimeType = mime,
      thumbnailUrl = media.thumbnailUrl
    )
  }

  fun startThumbnailDownload(media: MediaAnalyzeResult, resolution: String) {
    val cleanName = "${media.title.take(24).trim().replace(" ", "_")}_thumb.jpg"
    downloadEngine.startDownload(
      title = cleanName,
      sourceUrl = media.thumbnailUrl,
      platform = media.platform,
      mediaType = MediaType.THUMBNAIL,
      mimeType = "image/jpeg",
      thumbnailUrl = media.thumbnailUrl
    )
  }

  fun startBatchDownload(items: List<BatchItem>) {
    items.forEach { item ->
      downloadEngine.startDownload(
        title = "${item.title.replace(" ", "_")}.mp4",
        sourceUrl = item.url,
        platform = item.platform,
        mediaType = item.mediaType,
        thumbnailUrl = item.thumbnailUrl
      )
    }
  }

  fun enhanceImage(
    sourceUriOrUrl: String,
    baseFilename: String,
    scaleFactor: Int,
    engineType: String,
    onProgress: (Float) -> Unit,
    onComplete: (Boolean, String?) -> Unit
  ) {
    viewModelScope.launch {
      val result = EnhanceQualityEngine.processImageEnhancement(
        context = getApplication(),
        sourceUrlOrUri = sourceUriOrUrl,
        baseFilename = baseFilename,
        scaleFactor = scaleFactor,
        engineType = engineType,
        onProgress = onProgress
      )

      if (result.success && result.enhancedUri != null) {
        // Add enhanced copy to history
        database.downloadHistoryDao().insert(
          DownloadHistoryEntity(
            id = "enh_${System.currentTimeMillis()}",
            filename = result.filename,
            sourceUrl = sourceUriOrUrl,
            platform = "LinkDrop AI",
            mediaType = MediaType.IMAGE.name,
            quality = "Enhanced ${scaleFactor}X",
            fileSize = result.fileSize,
            formattedSize = StorageHelper.formatBytes(result.fileSize),
            downloadDate = System.currentTimeMillis(),
            localUriString = result.enhancedUri.toString(),
            thumbnailUri = result.enhancedUri.toString()
          )
        )
        onComplete(true, null)
      } else {
        onComplete(false, result.error)
      }
    }
  }

  fun deleteHistoryItem(id: String, deleteStorageFile: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      val entity = database.downloadHistoryDao().getById(id)
      if (entity != null) {
        if (deleteStorageFile) {
          StorageHelper.deleteMediaFile(getApplication(), entity.localUriString, entity.localFilePath)
        }
        database.downloadHistoryDao().deleteById(id)
      }
    }
  }

  fun clearAllHistory(deleteStorageFiles: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      if (deleteStorageFiles) {
        val all = database.downloadHistoryDao().getAllHistory().first()
        all.forEach {
          StorageHelper.deleteMediaFile(getApplication(), it.localUriString, it.localFilePath)
        }
      }
      database.downloadHistoryDao().clearAll()
    }
  }
}
