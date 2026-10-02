package com.example.providers

import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import com.example.model.AudioQualityOption
import com.example.model.ImageQualityOption
import com.example.model.MediaAnalyzeResult
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.VideoQualityOption
import com.example.storage.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class AnalysisResult(
  val isSuccess: Boolean,
  val media: MediaAnalyzeResult? = null,
  val errorMessage: String? = null
)

/**
 * Base provider adapter interface.
 * Defines a clean, legitimate contract for media source integrations.
 */
interface MediaProvider {
  val id: String
  val name: String
  val platform: PlatformType
  fun canHandle(url: String): Boolean
  suspend fun analyze(url: String): AnalysisResult
}

/**
 * Handles direct public downloadable media streams (MP4, WEBM, MKV, JPG, PNG, MP3, etc.).
 * Probes the remote server via HTTP HEAD/Range requests to determine the real Content-Type
 * and real Content-Length without inventing nonexistent formats or resolutions.
 */
class DirectMediaProvider(private val httpClient: OkHttpClient) : MediaProvider {
  override val id: String = "direct_media_provider"
  override val name: String = "Direct Media Provider"
  override val platform: PlatformType = PlatformType.OTHER

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    if (!URLUtil.isValidUrl(clean) || (!clean.startsWith("http://") && !clean.startsWith("https://"))) {
      return false
    }
    val mediaExtensions = listOf(
      ".mp4", ".mkv", ".webm", ".mov", ".avi",
      ".jpg", ".jpeg", ".png", ".webp", ".gif",
      ".mp3", ".wav", ".flac", ".m4a", ".aac", ".ogg"
    )
    val uri = try { Uri.parse(clean) } catch (_: Exception) { null }
    val path = uri?.path?.lowercase() ?: ""
    return mediaExtensions.any { path.endsWith(it) } ||
      clean.contains("gtv-videos-bucket") ||
      clean.contains("unsplash.com") ||
      clean.contains("storage.googleapis.com")
  }

  override suspend fun analyze(url: String): AnalysisResult = withContext(Dispatchers.IO) {
    try {
      val cleanUrl = url.trim()

      val headRequest = Request.Builder()
        .url(cleanUrl)
        .head()
        .header("User-Agent", "Mozilla/5.0 LinkDrop/2.4 Android")
        .build()

      var response = try {
        httpClient.newCall(headRequest).execute()
      } catch (_: Exception) {
        null
      }

      if (response == null || !response.isSuccessful) {
        val rangeRequest = Request.Builder()
          .url(cleanUrl)
          .header("Range", "bytes=0-1024")
          .header("User-Agent", "Mozilla/5.0 LinkDrop/2.4 Android")
          .get()
          .build()
        response = try {
          httpClient.newCall(rangeRequest).execute()
        } catch (e: Exception) {
          return@withContext AnalysisResult(
            isSuccess = false,
            errorMessage = "Unable to connect to source server: ${e.localizedMessage ?: "Connection error"}"
          )
        }
      }

      if (response == null || !response.isSuccessful) {
        return@withContext AnalysisResult(
          isSuccess = false,
          errorMessage = "Unable to analyze this link (Server returned HTTP ${response?.code ?: "error"})."
        )
      }

      val contentType = response.header("Content-Type")?.lowercase() ?: ""
      val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
      val formattedSize = if (contentLength > 0) StorageHelper.formatBytes(contentLength) else "Direct Stream"

      val uri = Uri.parse(cleanUrl)
      val filenameFromPath = uri.lastPathSegment?.substringBefore("?") ?: "media_file"
      val host = uri.host ?: "Direct Link"

      when {
        contentType.startsWith("video/") || isVideoExtension(filenameFromPath) -> {
          val cleanName = if (filenameFromPath.contains(".")) filenameFromPath else "$filenameFromPath.mp4"
          val ext = cleanName.substringAfterLast(".", "mp4").uppercase()
          val result = MediaAnalyzeResult(
            title = cleanName,
            author = host,
            authorHandle = "@$host",
            platform = PlatformType.OTHER,
            originalUrl = cleanUrl,
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            duration = "Direct Stream",
            views = "Public Media",
            uploadDate = "Available Now",
            description = "Direct $ext video file from $host ($formattedSize).",
            videoQualities = listOf(
              VideoQualityOption(
                id = "direct_video_orig",
                label = "Original ($ext)",
                resolution = "Source Resolution",
                fps = "Source FPS",
                fileSize = formattedSize,
                format = ext,
                codec = "Native",
                isRecommended = true
              )
            ),
            audioQualities = emptyList(),
            imageQualities = emptyList()
          )
          return@withContext AnalysisResult(isSuccess = true, media = result)
        }

        contentType.startsWith("image/") || isImageExtension(filenameFromPath) -> {
          val cleanName = if (filenameFromPath.contains(".")) filenameFromPath else "$filenameFromPath.jpg"
          val ext = cleanName.substringAfterLast(".", "jpg").uppercase()
          val result = MediaAnalyzeResult(
            title = cleanName,
            author = host,
            authorHandle = "@$host",
            platform = PlatformType.OTHER,
            originalUrl = cleanUrl,
            thumbnailUrl = cleanUrl,
            duration = "",
            views = "Image File",
            uploadDate = "Available Now",
            description = "High-resolution $ext image from $host ($formattedSize).",
            videoQualities = emptyList(),
            audioQualities = emptyList(),
            imageQualities = listOf(
              ImageQualityOption(
                id = "direct_img_orig",
                label = "Original ($ext)",
                dimensions = "Source Dimensions",
                fileSize = formattedSize,
                format = ext,
                isRecommended = true
              )
            )
          )
          return@withContext AnalysisResult(isSuccess = true, media = result)
        }

        contentType.startsWith("audio/") || isAudioExtension(filenameFromPath) -> {
          val cleanName = if (filenameFromPath.contains(".")) filenameFromPath else "$filenameFromPath.mp3"
          val ext = cleanName.substringAfterLast(".", "mp3").uppercase()
          val result = MediaAnalyzeResult(
            title = cleanName,
            author = host,
            authorHandle = "@$host",
            platform = PlatformType.OTHER,
            originalUrl = cleanUrl,
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            duration = "Audio File",
            views = "Audio Stream",
            uploadDate = "Available Now",
            description = "Audio stream from $host ($formattedSize).",
            videoQualities = emptyList(),
            audioQualities = listOf(
              AudioQualityOption(
                id = "direct_audio_orig",
                format = ext,
                bitrate = "Source Bitrate",
                fileSize = formattedSize,
                isRecommended = true
              )
            ),
            imageQualities = emptyList()
          )
          return@withContext AnalysisResult(isSuccess = true, media = result)
        }

        else -> {
          return@withContext AnalysisResult(
            isSuccess = false,
            errorMessage = "This URL points to a standard webpage (${contentType.ifEmpty { "HTML" }}), not a direct downloadable media file."
          )
        }
      }
    } catch (e: Exception) {
      Log.e("DirectMediaProvider", "Analysis error", e)
      return@withContext AnalysisResult(
        isSuccess = false,
        errorMessage = "Network error while probing link: ${e.localizedMessage ?: "Connection timed out"}"
      )
    }
  }

  private fun isVideoExtension(name: String): Boolean =
    listOf(".mp4", ".mkv", ".webm", ".mov", ".avi").any { name.endsWith(it, ignoreCase = true) }

  private fun isImageExtension(name: String): Boolean =
    listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp").any { name.endsWith(it, ignoreCase = true) }

  private fun isAudioExtension(name: String): Boolean =
    listOf(".mp3", ".wav", ".flac", ".m4a", ".aac", ".ogg").any { name.endsWith(it, ignoreCase = true) }
}

/**
 * Provider adapter interface for YouTube.
 * Adheres strictly to provider access policies (no scraping/DRM bypass).
 */
class YouTubeProvider : MediaProvider {
  override val id: String = "youtube_provider"
  override val name: String = "YouTube Provider"
  override val platform: PlatformType = PlatformType.YOUTUBE

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("youtube.com") || clean.contains("youtu.be")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

/**
 * Provider adapter interface for Instagram.
 */
class InstagramProvider : MediaProvider {
  override val id: String = "instagram_provider"
  override val name: String = "Instagram Provider"
  override val platform: PlatformType = PlatformType.INSTAGRAM

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("instagram.com")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

/**
 * Provider adapter interface for Pinterest.
 */
class PinterestProvider : MediaProvider {
  override val id: String = "pinterest_provider"
  override val name: String = "Pinterest Provider"
  override val platform: PlatformType = PlatformType.PINTEREST

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("pinterest.com")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

/**
 * Provider adapter interface for TikTok.
 */
class TikTokProvider : MediaProvider {
  override val id: String = "tiktok_provider"
  override val name: String = "TikTok Provider"
  override val platform: PlatformType = PlatformType.TIKTOK

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("tiktok.com")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

/**
 * Provider adapter interface for SoundCloud.
 */
class SoundCloudProvider : MediaProvider {
  override val id: String = "soundcloud_provider"
  override val name: String = "SoundCloud Provider"
  override val platform: PlatformType = PlatformType.SOUNDCLOUD

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("soundcloud.com")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

/**
 * Provider adapter interface for Twitter / X.
 */
class TwitterProvider : MediaProvider {
  override val id: String = "twitter_provider"
  override val name: String = "Twitter Provider"
  override val platform: PlatformType = PlatformType.TWITTER

  override fun canHandle(url: String): Boolean {
    val clean = url.trim().lowercase()
    return clean.contains("twitter.com") || clean.contains("x.com")
  }

  override suspend fun analyze(url: String): AnalysisResult {
    return AnalysisResult(
      isSuccess = false,
      errorMessage = "This source isn't currently supported."
    )
  }
}

object ProviderRegistry {
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  private val directProvider = DirectMediaProvider(httpClient)

  val providers: List<MediaProvider> = listOf(
    YouTubeProvider(),
    InstagramProvider(),
    PinterestProvider(),
    TikTokProvider(),
    SoundCloudProvider(),
    TwitterProvider(),
    directProvider
  )

  /**
   * Verified direct public media URLs for testing the complete end-to-end pipeline.
   */
  val sampleTestUrls = listOf(
    Pair("★ Test Download (MP4)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
    Pair("Sample Nature Image (JPG)", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1280&q=80"),
    Pair("Sample Cute Cat (JPG)", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=1280&q=80"),
    Pair("Big Buck Bunny (MP4)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
  )

  fun getProviderFor(url: String): MediaProvider {
    val clean = url.trim()
    return providers.firstOrNull { it.canHandle(clean) } ?: directProvider
  }

  suspend fun analyzeUrl(url: String): AnalysisResult {
    val clean = url.trim()
    if (clean.isBlank()) {
      return AnalysisResult(isSuccess = false, errorMessage = "Please enter a valid media link.")
    }
    if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
      return AnalysisResult(isSuccess = false, errorMessage = "Invalid URL. Links must start with http:// or https://")
    }

    val provider = getProviderFor(clean)
    return provider.analyze(clean)
  }
}
