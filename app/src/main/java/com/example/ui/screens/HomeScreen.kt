package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.database.DownloadHistoryEntity
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.providers.ProviderRegistry
import com.example.storage.StorageHelper
import com.example.ui.components.CrimsonAnalyzeButton
import com.example.ui.components.FeatureCard
import com.example.ui.components.LinkDropBottomBar
import com.example.ui.components.LinkDropHeader
import com.example.ui.components.RecentDownloadCard
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
  urlInput: String,
  onUrlChange: (String) -> Unit,
  onAnalyzeClick: (String) -> Unit,
  onNavigateToScreen: (String) -> Unit,
  onOpenRecentEntity: (DownloadHistoryEntity) -> Unit,
  recentDownloads: List<DownloadHistoryEntity>,
  onDeleteRecentEntity: (String, Boolean) -> Unit
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var pendingDeleteEntity by remember { mutableStateOf<DownloadHistoryEntity?>(null) }
  var showAboutAppDialog by remember { mutableStateOf(false) }

  if (showAboutAppDialog) {
    AlertDialog(
      onDismissRequest = { showAboutAppDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(CrimsonLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Link,
              contentDescription = "LinkDrop",
              tint = CrimsonPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "LinkDrop Studio",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Image(
            painter = painterResource(id = R.drawable.img_app_picture),
            contentDescription = "LinkDrop App Picture",
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.FillWidth
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Fast & Direct Media Downloader",
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp,
            color = CrimsonPrimary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Save high-definition 4K & 1080p videos, crystal-clear MP3 audio, full-size images, and thumbnails from public web links directly to your Android device storage and Gallery.",
            fontSize = 12.5.sp,
            color = TextSecondary,
            lineHeight = 18.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showAboutAppDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
          Text("Got it")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            showAboutAppDialog = false
            val sample = ProviderRegistry.sampleTestUrls.first().second
            onUrlChange(sample)
            onAnalyzeClick(sample)
          }
        ) {
          Text("Try Sample Link")
        }
      }
    )
  }

  if (pendingDeleteEntity != null) {
    val entity = pendingDeleteEntity!!
    AlertDialog(
      onDismissRequest = { pendingDeleteEntity = null },
      title = { Text("Delete Download") },
      text = { Text("Remove '${entity.filename}' from history only, or also delete the file from your device storage?") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteRecentEntity(entity.id, true)
            pendingDeleteEntity = null
            scope.launch { snackbarHostState.showSnackbar("Deleted file and history entry") }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
          Text("Delete File & History")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            onDeleteRecentEntity(entity.id, false)
            pendingDeleteEntity = null
            scope.launch { snackbarHostState.showSnackbar("Removed from history") }
          }
        ) {
          Text("History Only")
        }
      }
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      LinkDropHeader(
        modifier = Modifier.statusBarsPadding(),
        onSettingsClick = { onNavigateToScreen("settings") },
        onLogoClick = { showAboutAppDialog = true }
      )
    },
    bottomBar = {
      LinkDropBottomBar(
        currentScreen = "home",
        onNavigate = onNavigateToScreen
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Heading section
      item {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = buildAnnotatedString {
            append("Download your ")
            withStyle(style = SpanStyle(color = CrimsonPrimary)) {
              append("media")
            }
          },
          fontSize = 32.sp,
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimary,
          letterSpacing = (-0.5).sp,
          modifier = Modifier.testTag("home_heading")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Paste a supported public link and choose what you want to save.",
          fontSize = 14.sp,
          color = TextSecondary,
          lineHeight = 20.sp,
          modifier = Modifier.testTag("home_subtitle")
        )

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Rounded URL Input Container
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 3.dp,
              shape = RoundedCornerShape(28.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.08f),
              spotColor = CrimsonPrimary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(1.dp, CrimsonBorderLight, RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("url_input_container"),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Soft pink circle badge with red link icon
            Box(
              modifier = Modifier
                .padding(start = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(CrimsonLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Link Icon",
                tint = CrimsonPrimary,
                modifier = Modifier
                  .size(19.dp)
                  .rotate(-45f)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input field
            Box(
              modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (urlInput.isEmpty()) {
                Text(
                  text = "Paste video or image link...",
                  color = TextMuted,
                  fontSize = 13.5.sp,
                  maxLines = 1
                )
              }
              BasicTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                singleLine = true,
                textStyle = TextStyle(
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(CrimsonPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("url_text_input")
              )
            }

            // Clear 'X' button if text present
            if (urlInput.isNotEmpty()) {
              IconButton(
                onClick = { onUrlChange("") },
                modifier = Modifier
                  .size(32.dp)
                  .testTag("clear_url_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear link",
                  tint = TextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Rounded Paste Button pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CrimsonLight)
                .border(1.dp, CrimsonBorderLight, RoundedCornerShape(20.dp))
                .clickable {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  if (clipboard.hasPrimaryClip() && (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true || clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true)) {
                    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    if (text.isNotBlank()) {
                      onUrlChange(text)
                      scope.launch {
                        snackbarHostState.showSnackbar("Pasted link from clipboard!")
                      }
                    } else {
                      val sample = ProviderRegistry.sampleTestUrls.first().second
                      onUrlChange(sample)
                      scope.launch {
                        snackbarHostState.showSnackbar("Pasted verified direct test URL!")
                      }
                    }
                  } else {
                    val sample = ProviderRegistry.sampleTestUrls.first().second
                    onUrlChange(sample)
                    scope.launch {
                      snackbarHostState.showSnackbar("Pasted verified direct test URL!")
                    }
                  }
                }
                .testTag("paste_button")
                .padding(horizontal = 14.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste",
                  tint = CrimsonPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Paste",
                  color = CrimsonPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick verified test links for seamless validation
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(ProviderRegistry.sampleTestUrls) { (label, url) ->
            FilterChip(
              selected = urlInput == url,
              onClick = { onUrlChange(url) },
              label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CrimsonPrimary,
                selectedLabelColor = Color.White,
                containerColor = CrimsonLight,
                labelColor = CrimsonPrimary
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = urlInput == url,
                borderColor = CrimsonBorderLight,
                selectedBorderColor = CrimsonPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Analyze Link Big Red Button
      item {
        CrimsonAnalyzeButton(
          text = "Analyze Link",
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            if (urlInput.isBlank()) {
              scope.launch { snackbarHostState.showSnackbar("Please enter or paste a valid link.") }
            } else {
              onAnalyzeClick(urlInput)
            }
          }
        )

        Spacer(modifier = Modifier.height(20.dp))
      }

      // 2-Column Feature Cards Grid
      item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Row 1: Download Video & Download Image
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FeatureCard(
              title = "Download Video",
              description = "Save videos in high quality from supported sites.",
              icon = Icons.Default.PlayArrow,
              testTag = "card_download_video",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("video_quality") }
            )

            FeatureCard(
              title = "Download Image",
              description = "Save images in original quality.",
              icon = Icons.Default.Image,
              testTag = "card_download_image",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("image_download") }
            )
          }

          // Row 2: Save Thumbnail & Download Audio
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FeatureCard(
              title = "Save Thumbnail",
              description = "Download video thumbnails in high resolution.",
              icon = Icons.Default.Wallpaper,
              testTag = "card_save_thumbnail",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("thumbnail") }
            )

            FeatureCard(
              title = "Download Audio",
              description = "Extract audio from videos and save as MP3.",
              icon = Icons.Default.MusicNote,
              testTag = "card_download_audio",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("audio_selection") }
            )
          }

          // Row 3: Enhance Quality & Batch Download
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            FeatureCard(
              title = "Enhance Quality",
              description = "Upscale and improve media quality.",
              icon = Icons.Default.AutoAwesome,
              testTag = "card_enhance_quality",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("enhance_quality") }
            )

            FeatureCard(
              title = "Batch Download",
              description = "Download multiple links at once.",
              icon = Icons.Default.Layers,
              testTag = "card_batch_download",
              modifier = Modifier.weight(1f),
              onClick = { onNavigateToScreen("batch_download") }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // LinkDrop Official App Picture Showcase Card
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.12f),
              spotColor = CrimsonPrimary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(20.dp))
            .clickable { showAboutAppDialog = true }
            .testTag("app_picture_showcase_card")
        ) {
          Column {
            // App Picture Illustration
            Image(
              painter = painterResource(id = R.drawable.img_app_picture),
              contentDescription = "LinkDrop App Picture",
              modifier = Modifier
                .fillMaxWidth()
                .height(165.dp),
              contentScale = ContentScale.FillWidth
            )

            // Info Bar below Picture
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 11.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "LinkDrop Media Studio",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp,
                  color = TextPrimary
                )
                Text(
                  text = "Tap to view app info & quick sample links",
                  fontSize = 11.5.sp,
                  color = TextSecondary
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .background(CrimsonLight)
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = "App Info",
                  color = CrimsonPrimary,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Recent Downloads Section Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Downloads",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.testTag("recent_downloads_heading")
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clickable { onNavigateToScreen("history") }
              .testTag("see_all_button")
              .padding(4.dp)
          ) {
            Text(
              text = "See all",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = CrimsonPrimary
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = "See all downloads",
              tint = CrimsonPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Real Recent Downloads from Room Database
      if (recentDownloads.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No recent downloads yet. Paste a link to get started!",
              color = TextSecondary,
              fontSize = 13.sp
            )
          }
        }
      } else {
        items(recentDownloads, key = { it.id }) { entity ->
          val plat = PlatformType.values().firstOrNull { it.displayName == entity.platform } ?: PlatformType.OTHER
          val mType = try { MediaType.valueOf(entity.mediaType) } catch (_: Exception) { MediaType.VIDEO }
          val recentItem = RecentDownloadItem(
            id = entity.id,
            title = entity.filename,
            platform = plat,
            mediaType = mType,
            specs = "${mType.name.lowercase().replaceFirstChar { it.uppercase() }}  •  ${entity.quality}  •  ${entity.formattedSize}",
            thumbnailUrl = entity.thumbnailUri ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            isCompleted = true,
            fileSize = entity.formattedSize,
            date = "Saved"
          )

          RecentDownloadCard(
            item = recentItem,
            onItemClick = { onOpenRecentEntity(entity) },
            onActionClick = {
              val mime = when (mType) {
                MediaType.VIDEO -> "video/*"
                MediaType.IMAGE -> "image/*"
                MediaType.THUMBNAIL -> "image/*"
                MediaType.AUDIO -> "audio/*"
              }
              StorageHelper.shareMedia(context, entity.localUriString, mime, entity.filename)
            },
            onDeleteClick = {
              pendingDeleteEntity = entity
            },
            modifier = Modifier.padding(bottom = 10.dp)
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
