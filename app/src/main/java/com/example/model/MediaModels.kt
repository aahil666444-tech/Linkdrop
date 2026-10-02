package com.example.model

enum class MediaType {
  VIDEO,
  IMAGE,
  AUDIO,
  THUMBNAIL
}

enum class PlatformType(val displayName: String) {
  YOUTUBE("YouTube"),
  INSTAGRAM("Instagram"),
  SOUNDCLOUD("SoundCloud"),
  TIKTOK("TikTok"),
  TWITTER("X / Twitter"),
  PINTEREST("Pinterest"),
  OTHER("Web Link")
}

data class RecentDownloadItem(
  val id: String,
  val title: String,
  val platform: PlatformType,
  val mediaType: MediaType,
  val specs: String,
  val thumbnailUrl: String,
  val isCompleted: Boolean = true,
  val date: String = "Today",
  val fileSize: String = "42.6 MB",
  val duration: String = "03:45"
)

data class VideoQualityOption(
  val id: String,
  val label: String,
  val resolution: String,
  val fps: String,
  val fileSize: String,
  val format: String = "MP4",
  val codec: String = "H.264",
  val isHdr: Boolean = false,
  val isRecommended: Boolean = false
)

data class AudioQualityOption(
  val id: String,
  val format: String,
  val bitrate: String,
  val fileSize: String,
  val sampleRate: String = "48 kHz",
  val isRecommended: Boolean = false
)

data class ImageQualityOption(
  val id: String,
  val label: String,
  val dimensions: String,
  val fileSize: String,
  val format: String = "JPG",
  val isRecommended: Boolean = false
)

data class ActiveDownloadTask(
  val id: String,
  val title: String,
  val platform: PlatformType,
  val mediaType: MediaType,
  val progress: Float,
  val speed: String,
  val remainingTime: String,
  val downloadedSize: String,
  val totalSize: String,
  val isPaused: Boolean = false,
  val thumbnailUrl: String
)

enum class BatchStatus {
  READY,
  QUEUED,
  DOWNLOADING,
  COMPLETED,
  FAILED
}

data class BatchItem(
  val id: String,
  val url: String,
  val title: String,
  val platform: PlatformType,
  val mediaType: MediaType,
  val status: BatchStatus,
  val estimatedSize: String,
  val thumbnailUrl: String
)

data class MediaAnalyzeResult(
  val title: String,
  val author: String,
  val authorHandle: String,
  val platform: PlatformType,
  val originalUrl: String,
  val thumbnailUrl: String,
  val duration: String,
  val views: String,
  val uploadDate: String,
  val description: String,
  val videoQualities: List<VideoQualityOption>,
  val audioQualities: List<AudioQualityOption>,
  val imageQualities: List<ImageQualityOption>
)
