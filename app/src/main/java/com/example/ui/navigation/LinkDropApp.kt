package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MockDataProvider
import com.example.model.MediaAnalyzeResult
import com.example.model.MediaType
import com.example.providers.ProviderRegistry
import com.example.storage.StorageHelper
import com.example.ui.screens.AnalyzeLoadingScreen
import com.example.ui.screens.AudioSelectionScreen
import com.example.ui.screens.BatchDownloadScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.EnhanceQualityScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageDownloadScreen
import com.example.ui.screens.MediaPreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThumbnailScreen
import com.example.ui.screens.VideoQualityScreen
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.LinkDropViewModel

@Composable
fun LinkDropApp(viewModel: LinkDropViewModel = viewModel()) {
  val context = LocalContext.current

  var currentScreen by remember { mutableStateOf("home") }
  val screenStack = remember { mutableStateListOf("home") }

  var currentUrlInput by remember { mutableStateOf("") }
  var currentMediaResult by remember { mutableStateOf(MockDataProvider.getSampleMedia("")) }

  val recentHistoryList by viewModel.recentHistoryList.collectAsStateWithLifecycle()
  val historyList by viewModel.historyList.collectAsStateWithLifecycle()
  val activeJobs by viewModel.activeJobs.collectAsStateWithLifecycle()
  val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
  val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()

  fun navigateTo(screen: String) {
    if (currentScreen != screen) {
      screenStack.add(screen)
      currentScreen = screen
    }
  }

  fun navigateBack() {
    if (screenStack.size > 1) {
      screenStack.removeAt(screenStack.lastIndex)
      currentScreen = screenStack.last()
    } else {
      currentScreen = "home"
    }
  }

  if (currentScreen != "home") {
    BackHandler {
      navigateBack()
    }
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
      (slideInHorizontally { width -> width / 4 } + fadeIn())
        .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut())
    },
    label = "ScreenTransition",
    modifier = Modifier.fillMaxSize()
  ) { screen ->
    when (screen) {
      "home" -> HomeScreen(
        urlInput = currentUrlInput,
        onUrlChange = { currentUrlInput = it },
        onAnalyzeClick = { url ->
          viewModel.analyzeUrl(url)
          navigateTo("analyze_loading")
        },
        onNavigateToScreen = { target ->
          if (target == "home") {
            screenStack.clear()
            screenStack.add("home")
            currentScreen = "home"
          } else {
            navigateTo(target)
          }
        },
        onOpenRecentEntity = { entity ->
          val mime = when (entity.mediaType.uppercase()) {
            "VIDEO" -> "video/*"
            "IMAGE" -> "image/*"
            "THUMBNAIL" -> "image/*"
            "AUDIO" -> "audio/*"
            else -> "*/*"
          }
          StorageHelper.openMedia(context, entity.localUriString, mime)
        },
        recentDownloads = recentHistoryList,
        onDeleteRecentEntity = { id, deleteStorageFile ->
          viewModel.deleteHistoryItem(id, deleteStorageFile)
        }
      )

      "analyze_loading" -> AnalyzeLoadingScreen(
        targetUrl = currentUrlInput.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" },
        analysisState = analysisState,
        onSuccess = { media ->
          currentMediaResult = media
          screenStack.removeAt(screenStack.lastIndex)
          screenStack.add("media_preview")
          currentScreen = "media_preview"
        },
        onRetry = {
          val sampleUrl = ProviderRegistry.sampleTestUrls.first().second
          currentUrlInput = sampleUrl
          viewModel.analyzeUrl(sampleUrl)
        },
        onCancel = {
          viewModel.clearAnalysis()
          navigateBack()
        }
      )

      "media_preview" -> MediaPreviewScreen(
        media = currentMediaResult,
        onBack = { navigateBack() },
        onChooseVideoQuality = { navigateTo("video_quality") },
        onChooseAudio = { navigateTo("audio_selection") },
        onChooseThumbnail = { navigateTo("thumbnail") },
        onChooseEnhance = { navigateTo("enhance_quality") },
        onStartInstantDownload = {
          val bestQuality = currentMediaResult.videoQualities.firstOrNull { it.isRecommended }
            ?: currentMediaResult.videoQualities.firstOrNull()
          if (bestQuality != null) {
            viewModel.startVideoDownload(currentMediaResult, bestQuality, bestQuality.format)
          } else {
            // Download as generic video
            viewModel.downloadEngine.startDownload(
              title = "${currentMediaResult.title.take(30).trim().replace(" ", "_")}.mp4",
              sourceUrl = currentMediaResult.originalUrl,
              platform = currentMediaResult.platform,
              mediaType = MediaType.VIDEO,
              thumbnailUrl = currentMediaResult.thumbnailUrl
            )
          }
          navigateTo("downloads")
        }
      )

      "video_quality" -> VideoQualityScreen(
        onBack = { navigateBack() },
        onStartDownload = { quality, format ->
          viewModel.startVideoDownload(currentMediaResult, quality, format)
          navigateTo("downloads")
        }
      )

      "image_download" -> ImageDownloadScreen(
        onBack = { navigateBack() },
        onSaveToGallery = { option, format ->
          viewModel.startImageDownload(currentMediaResult, option, format)
          navigateTo("downloads")
        }
      )

      "audio_selection" -> AudioSelectionScreen(
        onBack = { navigateBack() },
        onExtractAudio = { option ->
          viewModel.startAudioDownload(currentMediaResult, option)
          navigateTo("downloads")
        }
      )

      "thumbnail" -> ThumbnailScreen(
        onBack = { navigateBack() },
        onDownloadThumbnail = { quality ->
          viewModel.startThumbnailDownload(currentMediaResult, quality.resolution)
          navigateTo("downloads")
        }
      )

      "enhance_quality" -> EnhanceQualityScreen(
        onBack = { navigateBack() },
        onEnhanceRequested = { scaleFactor, engineId, onProgress, onDone ->
          viewModel.enhanceImage(
            sourceUriOrUrl = currentMediaResult.thumbnailUrl,
            baseFilename = currentMediaResult.title,
            scaleFactor = scaleFactor,
            engineType = engineId,
            onProgress = onProgress,
            onComplete = onDone
          )
        }
      )

      "batch_download" -> BatchDownloadScreen(
        onBack = { navigateBack() },
        onStartBatchDownload = { items ->
          viewModel.startBatchDownload(items)
          navigateTo("downloads")
        }
      )

      "downloads" -> DownloadsScreen(
        activeJobs = activeJobs,
        completedEntities = historyList,
        onNavigateToScreen = { target ->
          if (target == "home") {
            screenStack.clear()
            screenStack.add("home")
            currentScreen = "home"
          } else {
            navigateTo(target)
          }
        },
        onPauseJob = { id -> viewModel.downloadEngine.pauseDownload(id) },
        onResumeJob = { id -> viewModel.downloadEngine.resumeDownload(id) },
        onCancelJob = { id -> viewModel.downloadEngine.cancelDownload(id) },
        onRetryJob = { id -> viewModel.downloadEngine.retryDownload(id) },
        onDeleteHistory = { id, deleteFile ->
          viewModel.deleteHistoryItem(id, deleteFile)
        }
      )

      "history" -> HistoryScreen(
        historyEntities = historyList,
        onNavigateToScreen = { target ->
          if (target == "home") {
            screenStack.clear()
            screenStack.add("home")
            currentScreen = "home"
          } else {
            navigateTo(target)
          }
        },
        onDeleteItem = { id, deleteFile ->
          viewModel.deleteHistoryItem(id, deleteFile)
        },
        onClearAll = { deleteFiles ->
          viewModel.clearAllHistory(deleteFiles)
        }
      )

      "settings" -> SettingsScreen(
        settings = appSettings,
        onNavigateToScreen = { target ->
          if (target == "home") {
            screenStack.clear()
            screenStack.add("home")
            currentScreen = "home"
          } else {
            navigateTo(target)
          }
        },
        onToggleWifiOnly = { viewModel.settingsManager.updateWifiOnly(it) },
        onToggleNotification = { viewModel.settingsManager.updateNotifyOnComplete(it) },
        onToggleAutoAnalyze = { viewModel.settingsManager.updateAutoAnalyzeOnPaste(it) },
        onSelectVideoQuality = { viewModel.settingsManager.updateDefaultVideoQuality(it) },
        onSelectAudioFormat = { viewModel.settingsManager.updateDefaultAudioFormat(it) },
        onClearHistory = { viewModel.clearAllHistory(false) }
      )
    }
  }
}
