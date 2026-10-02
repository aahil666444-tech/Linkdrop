package com.example.data

import com.example.model.ActiveDownloadTask
import com.example.model.AudioQualityOption
import com.example.model.BatchItem
import com.example.model.BatchStatus
import com.example.model.ImageQualityOption
import com.example.model.MediaAnalyzeResult
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.model.VideoQualityOption

object MockDataProvider {

  // Exact 3 items from the reference screenshot
  val initialRecentDownloads = listOf(
    RecentDownloadItem(
      id = "rec_1",
      title = "Nature_8K_Travel.mp4",
      platform = PlatformType.YOUTUBE,
      mediaType = MediaType.VIDEO,
      specs = "Video  •  1080p  •  42.6 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "10 mins ago",
      fileSize = "42.6 MB",
      duration = "08:24"
    ),
    RecentDownloadItem(
      id = "rec_2",
      title = "Cute_Cat.jpg",
      platform = PlatformType.INSTAGRAM,
      mediaType = MediaType.IMAGE,
      specs = "Image  •  1080p  •  3.2 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "1 hour ago",
      fileSize = "3.2 MB",
      duration = ""
    ),
    RecentDownloadItem(
      id = "rec_3",
      title = "Calm_Vibes.mp3",
      platform = PlatformType.SOUNDCLOUD,
      mediaType = MediaType.AUDIO,
      specs = "Audio  •  320kbps  •  6.8 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "Yesterday",
      fileSize = "6.8 MB",
      duration = "03:52"
    )
  )

  // Additional history items
  val fullHistoryItems = initialRecentDownloads + listOf(
    RecentDownloadItem(
      id = "rec_4",
      title = "Cyberpunk_Night_City_Wallpaper.png",
      platform = PlatformType.PINTEREST,
      mediaType = MediaType.IMAGE,
      specs = "Image  •  4K  •  8.9 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "2 days ago",
      fileSize = "8.9 MB"
    ),
    RecentDownloadItem(
      id = "rec_5",
      title = "Viral_Cooking_Recipe.mp4",
      platform = PlatformType.TIKTOK,
      mediaType = MediaType.VIDEO,
      specs = "Video  •  1080p  •  18.4 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "3 days ago",
      fileSize = "18.4 MB",
      duration = "01:00"
    ),
    RecentDownloadItem(
      id = "rec_6",
      title = "Podcast_Episode_84_Mastery.mp3",
      platform = PlatformType.OTHER,
      mediaType = MediaType.AUDIO,
      specs = "Audio  •  192kbps  •  54.2 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "5 days ago",
      fileSize = "54.2 MB",
      duration = "45:10"
    ),
    RecentDownloadItem(
      id = "rec_7",
      title = "Tech_Conference_Keynote_Cover.jpg",
      platform = PlatformType.TWITTER,
      mediaType = MediaType.THUMBNAIL,
      specs = "Thumbnail  •  1080p  •  1.4 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&auto=format&fit=crop&q=80",
      isCompleted = true,
      date = "Last week",
      fileSize = "1.4 MB"
    )
  )

  // Active Downloads
  val sampleActiveDownloads = listOf(
    ActiveDownloadTask(
      id = "act_1",
      title = "Epic_Cinematic_Fpv_Drone_4K.mp4",
      platform = PlatformType.YOUTUBE,
      mediaType = MediaType.VIDEO,
      progress = 0.68f,
      speed = "8.4 MB/s",
      remainingTime = "12s left",
      downloadedSize = "84.2 MB",
      totalSize = "124.0 MB",
      isPaused = false,
      thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80"
    ),
    ActiveDownloadTask(
      id = "act_2",
      title = "Sunset_Acoustic_Guitar_Session.flac",
      platform = PlatformType.SOUNDCLOUD,
      mediaType = MediaType.AUDIO,
      progress = 0.35f,
      speed = "4.2 MB/s",
      remainingTime = "25s left",
      downloadedSize = "12.8 MB",
      totalSize = "36.5 MB",
      isPaused = false,
      thumbnailUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=600&auto=format&fit=crop&q=80"
    )
  )

  // Sample Analyzed Media
  fun getSampleMedia(inputUrl: String): MediaAnalyzeResult {
    val platform = when {
      inputUrl.contains("youtube") || inputUrl.contains("youtu.be") -> PlatformType.YOUTUBE
      inputUrl.contains("instagram") -> PlatformType.INSTAGRAM
      inputUrl.contains("soundcloud") -> PlatformType.SOUNDCLOUD
      inputUrl.contains("tiktok") -> PlatformType.TIKTOK
      inputUrl.contains("twitter") || inputUrl.contains("x.com") -> PlatformType.TWITTER
      inputUrl.contains("pinterest") -> PlatformType.PINTEREST
      else -> PlatformType.YOUTUBE
    }

    return MediaAnalyzeResult(
      title = "Switzerland in 8K Ultra HD 60FPS - Peaceful Relaxing Alpine Nature",
      author = "Wanderlust Media Studios",
      authorHandle = "@wanderlust_visuals",
      platform = platform,
      originalUrl = inputUrl.ifEmpty { "https://www.youtube.com/watch?v=dQw4w9WgXcQ" },
      thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80",
      duration = "08:42",
      views = "3,482,910 views",
      uploadDate = "Sep 18, 2026",
      description = "Stunning 8K aerial footage across the Swiss Alps, Jungfrau region and turquoise glacial lakes. Captured in Dolby Vision 60FPS.",
      videoQualities = listOf(
        VideoQualityOption(
          id = "vq_4k",
          label = "4K Ultra HD",
          resolution = "2160p",
          fps = "60 fps",
          fileSize = "486.4 MB",
          format = "MP4",
          codec = "AV1 / H.265",
          isHdr = true,
          isRecommended = false
        ),
        VideoQualityOption(
          id = "vq_1080",
          label = "Full HD (Best Balance)",
          resolution = "1080p",
          fps = "60 fps",
          fileSize = "142.6 MB",
          format = "MP4",
          codec = "H.264",
          isHdr = false,
          isRecommended = true
        ),
        VideoQualityOption(
          id = "vq_720",
          label = "High Definition",
          resolution = "720p",
          fps = "30 fps",
          fileSize = "62.4 MB",
          format = "MP4",
          codec = "H.264",
          isHdr = false,
          isRecommended = false
        ),
        VideoQualityOption(
          id = "vq_480",
          label = "Standard Quality",
          resolution = "480p",
          fps = "30 fps",
          fileSize = "28.1 MB",
          format = "MP4",
          codec = "H.264",
          isHdr = false,
          isRecommended = false
        )
      ),
      audioQualities = listOf(
        AudioQualityOption(
          id = "aq_320",
          format = "MP3",
          bitrate = "320 kbps",
          fileSize = "9.8 MB",
          sampleRate = "48 kHz",
          isRecommended = true
        ),
        AudioQualityOption(
          id = "aq_256",
          format = "M4A / AAC",
          bitrate = "256 kbps",
          fileSize = "7.6 MB",
          sampleRate = "44.1 kHz",
          isRecommended = false
        ),
        AudioQualityOption(
          id = "aq_flac",
          format = "FLAC Lossless",
          bitrate = "1411 kbps",
          fileSize = "29.4 MB",
          sampleRate = "96 kHz / 24bit",
          isRecommended = false
        ),
        AudioQualityOption(
          id = "aq_128",
          format = "MP3 (Compact)",
          bitrate = "128 kbps",
          fileSize = "3.9 MB",
          sampleRate = "44.1 kHz",
          isRecommended = false
        )
      ),
      imageQualities = listOf(
        ImageQualityOption(
          id = "iq_orig",
          label = "Original Ultra HD Frame",
          dimensions = "3840 x 2160",
          fileSize = "12.8 MB",
          format = "PNG",
          isRecommended = true
        ),
        ImageQualityOption(
          id = "iq_1080",
          label = "Full HD Crisp",
          dimensions = "1920 x 1080",
          fileSize = "3.4 MB",
          format = "JPG",
          isRecommended = false
        ),
        ImageQualityOption(
          id = "iq_thumb",
          label = "YouTube MaxRes Thumbnail",
          dimensions = "1280 x 720",
          fileSize = "860 KB",
          format = "JPG",
          isRecommended = false
        )
      )
    )
  }

  // Sample Batch Items
  val sampleBatchItems = listOf(
    BatchItem(
      id = "b_1",
      url = "https://youtube.com/watch?v=alpine_journey",
      title = "Alpine Peaks Drone Flyover 4K",
      platform = PlatformType.YOUTUBE,
      mediaType = MediaType.VIDEO,
      status = BatchStatus.READY,
      estimatedSize = "112.5 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_2",
      url = "https://instagram.com/p/sunset_glamour",
      title = "Golden Hour Portrait Shot",
      platform = PlatformType.INSTAGRAM,
      mediaType = MediaType.IMAGE,
      status = BatchStatus.READY,
      estimatedSize = "4.1 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_3",
      url = "https://soundcloud.com/chillhop/lofi_beats_34",
      title = "Midnight Coffee Beats - Chillhop",
      platform = PlatformType.SOUNDCLOUD,
      mediaType = MediaType.AUDIO,
      status = BatchStatus.READY,
      estimatedSize = "8.4 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
    ),
    BatchItem(
      id = "b_4",
      url = "https://tiktok.com/@creator/video/98372",
      title = "Fast Espresso Art Trick",
      platform = PlatformType.TIKTOK,
      mediaType = MediaType.VIDEO,
      status = BatchStatus.READY,
      estimatedSize = "14.2 MB",
      thumbnailUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600&auto=format&fit=crop&q=80"
    )
  )
}
