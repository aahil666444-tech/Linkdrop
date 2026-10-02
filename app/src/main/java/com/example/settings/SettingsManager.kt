package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
  val defaultVideoQuality: String = "1080p Full HD",
  val defaultAudioFormat: String = "MP3 (320 kbps)",
  val wifiOnly: Boolean = false,
  val notifyOnComplete: Boolean = true,
  val autoAnalyzeOnPaste: Boolean = true,
  val downloadDirectory: String = "LinkDrop",
  val saveThumbnailWithMedia: Boolean = false
)

class SettingsManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("linkdrop_settings", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<AppSettings> = _settings.asStateFlow()

  private fun loadSettings(): AppSettings {
    return AppSettings(
      defaultVideoQuality = prefs.getString("video_quality", "1080p Full HD") ?: "1080p Full HD",
      defaultAudioFormat = prefs.getString("audio_format", "MP3 (320 kbps)") ?: "MP3 (320 kbps)",
      wifiOnly = prefs.getBoolean("wifi_only", false),
      notifyOnComplete = prefs.getBoolean("notify_on_complete", true),
      autoAnalyzeOnPaste = prefs.getBoolean("auto_analyze_on_paste", true),
      downloadDirectory = prefs.getString("download_dir", "LinkDrop") ?: "LinkDrop",
      saveThumbnailWithMedia = prefs.getBoolean("save_thumb_with_media", false)
    )
  }

  fun updateWifiOnly(enabled: Boolean) {
    prefs.edit().putBoolean("wifi_only", enabled).apply()
    _settings.value = _settings.value.copy(wifiOnly = enabled)
  }

  fun updateNotifyOnComplete(enabled: Boolean) {
    prefs.edit().putBoolean("notify_on_complete", enabled).apply()
    _settings.value = _settings.value.copy(notifyOnComplete = enabled)
  }

  fun updateAutoAnalyzeOnPaste(enabled: Boolean) {
    prefs.edit().putBoolean("auto_analyze_on_paste", enabled).apply()
    _settings.value = _settings.value.copy(autoAnalyzeOnPaste = enabled)
  }

  fun updateDefaultVideoQuality(quality: String) {
    prefs.edit().putString("video_quality", quality).apply()
    _settings.value = _settings.value.copy(defaultVideoQuality = quality)
  }

  fun updateDefaultAudioFormat(format: String) {
    prefs.edit().putString("audio_format", format).apply()
    _settings.value = _settings.value.copy(defaultAudioFormat = format)
  }

  fun updateDownloadDirectory(dir: String) {
    prefs.edit().putString("download_dir", dir).apply()
    _settings.value = _settings.value.copy(downloadDirectory = dir)
  }
}
