package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaAnalyzeResult
import com.example.ui.components.CrimsonAnalyzeButton
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MediaPreviewScreen(
  media: MediaAnalyzeResult,
  onBack: () -> Unit,
  onChooseVideoQuality: () -> Unit,
  onChooseAudio: () -> Unit,
  onChooseThumbnail: () -> Unit,
  onChooseEnhance: () -> Unit,
  onStartInstantDownload: () -> Unit
) {
  BackHandler { onBack() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("preview_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Media Preview",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        IconButton(
          onClick = {
            scope.launch {
              snackbarHostState.showSnackbar("Media link copied to share!")
            }
          },
          modifier = Modifier.testTag("preview_share_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = CrimsonPrimary
          )
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Hero Media Card with Video Preview
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(22.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.15f),
              spotColor = CrimsonPrimary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(CrimsonSoftBg)
        ) {
          AsyncImage(
            model = media.thumbnailUrl,
            contentDescription = media.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          // Duration Badge
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(12.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.75f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = media.duration,
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // HD Badge
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(12.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(CrimsonPrimary)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "8K / 60FPS",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Centered Play Button
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(54.dp)
              .clip(CircleShape)
              .background(CrimsonPrimary.copy(alpha = 0.9f))
              .clickable {
                scope.launch {
                  snackbarHostState.showSnackbar("Playing preview...")
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play Video Preview",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Title & Creator Information
      item {
        Text(
          text = media.title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            PlatformBadge(platform = media.platform)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "•  ${media.author}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }

          Text(
            text = media.views,
            fontSize = 12.sp,
            color = TextMuted
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Quick Action Modes Header
      item {
        Text(
          text = "Select Download Format",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Action options
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionOption(
            title = "Video",
            subtext = "MP4 / MKV",
            icon = Icons.Default.Videocam,
            modifier = Modifier.weight(1f),
            testTag = "preview_opt_video",
            onClick = onChooseVideoQuality
          )

          QuickActionOption(
            title = "Audio",
            subtext = "MP3 / FLAC",
            icon = Icons.Default.MusicNote,
            modifier = Modifier.weight(1f),
            testTag = "preview_opt_audio",
            onClick = onChooseAudio
          )

          QuickActionOption(
            title = "Thumbnail",
            subtext = "High-Res",
            icon = Icons.Default.Wallpaper,
            modifier = Modifier.weight(1f),
            testTag = "preview_opt_thumbnail",
            onClick = onChooseThumbnail
          )

          QuickActionOption(
            title = "Enhance",
            subtext = "AI 4K",
            icon = Icons.Default.AutoAwesome,
            modifier = Modifier.weight(1f),
            testTag = "preview_opt_enhance",
            onClick = onChooseEnhance
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Specs Summary Box
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = CrimsonSoftBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Media Specifications",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            val primaryVideo = media.videoQualities.firstOrNull()
            val primaryAudio = media.audioQualities.firstOrNull()
            val primaryImg = media.imageQualities.firstOrNull()

            when {
              primaryVideo != null -> {
                SpecRow("Format", primaryVideo.format)
                SpecRow("Quality", primaryVideo.label)
                SpecRow("File Size", primaryVideo.fileSize)
                if (media.duration.isNotBlank()) {
                  SpecRow("Duration", media.duration)
                }
              }
              primaryImg != null -> {
                SpecRow("Format", primaryImg.format)
                SpecRow("Dimensions", primaryImg.dimensions)
                SpecRow("File Size", primaryImg.fileSize)
              }
              primaryAudio != null -> {
                SpecRow("Format", primaryAudio.format)
                SpecRow("Bitrate", primaryAudio.bitrate)
                SpecRow("File Size", primaryAudio.fileSize)
              }
              else -> {
                SpecRow("Source", media.author)
                SpecRow("Platform", media.platform.displayName)
                SpecRow("Status", "Direct Stream")
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Prominent Action Buttons
      item {
        val hasVideo = media.videoQualities.isNotEmpty()
        val hasImage = media.imageQualities.isNotEmpty()

        val mainButtonText = when {
          hasVideo -> "Select Video Quality"
          hasImage -> "Select Image Resolution"
          else -> "Select Format"
        }

        val onMainAction = when {
          hasVideo -> onChooseVideoQuality
          hasImage -> onChooseThumbnail
          else -> onChooseVideoQuality
        }

        CrimsonAnalyzeButton(
          text = mainButtonText,
          onClick = onMainAction,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        val bestQuality = media.videoQualities.firstOrNull { it.isRecommended }
          ?: media.videoQualities.firstOrNull()

        val downloadBtnLabel = if (bestQuality != null) {
          "Instant Download (${bestQuality.format})"
        } else if (media.imageQualities.isNotEmpty()) {
          "Save Original Image"
        } else {
          "Instant Download"
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(CrimsonLight)
            .border(1.dp, CrimsonBorderLight, RoundedCornerShape(25.dp))
            .clickable(onClick = onStartInstantDownload)
            .testTag("preview_instant_download"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FileDownload,
              contentDescription = "Direct Download",
              tint = CrimsonPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = downloadBtnLabel,
              color = CrimsonPrimary,
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
private fun QuickActionOption(
  title: String,
  subtext: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .shadow(2.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White)
      .border(1.dp, CrimsonBorderLight, RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
      .padding(vertical = 12.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(CrimsonLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = CrimsonPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtext,
        fontSize = 10.sp,
        color = TextSecondary
      )
    }
  }
}

@Composable
private fun SpecRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 12.5.sp, color = TextSecondary)
    Text(text = value, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
  }
}
