package com.example.ui.screens

import android.app.AlertDialog
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.database.DownloadHistoryEntity
import com.example.download.DownloadJob
import com.example.download.DownloadStatus
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.storage.StorageHelper
import com.example.ui.components.LinkDropBottomBar
import com.example.ui.components.PlatformBadge
import com.example.ui.components.RecentDownloadCard
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DownloadsScreen(
  activeJobs: List<DownloadJob>,
  completedEntities: List<DownloadHistoryEntity>,
  onNavigateToScreen: (String) -> Unit,
  onPauseJob: (String) -> Unit,
  onResumeJob: (String) -> Unit,
  onCancelJob: (String) -> Unit,
  onRetryJob: (String) -> Unit,
  onDeleteHistory: (String, Boolean) -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(if (activeJobs.isNotEmpty()) 0 else 1) }

  val availableStorage = remember { StorageHelper.getAvailableStorageBytes(context) }
  val formattedStorage = remember { StorageHelper.formatBytes(availableStorage) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Downloads",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
          )

          if (selectedTab == 0 && activeJobs.isNotEmpty()) {
            val anyRunning = activeJobs.any { it.status == DownloadStatus.DOWNLOADING }
            Text(
              text = if (anyRunning) "Pause All" else "Resume All",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = CrimsonPrimary,
              modifier = Modifier
                .clickable {
                  activeJobs.forEach { job ->
                    if (anyRunning) onPauseJob(job.id) else onResumeJob(job.id)
                  }
                }
                .padding(4.dp)
            )
          }
        }
      }
    },
    bottomBar = {
      val activeCount = activeJobs.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
      LinkDropBottomBar(
        currentScreen = "downloads",
        onNavigate = onNavigateToScreen,
        activeDownloadsCount = activeCount
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Storage bar info with real device storage
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = CrimsonSoftBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CrimsonLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Storage",
                tint = CrimsonPrimary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Device Free Storage",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "$formattedStorage Available",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = CrimsonPrimary
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              LinearProgressIndicator(
                progress = { 0.35f },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = CrimsonPrimary,
                trackColor = CrimsonLight,
                strokeCap = StrokeCap.Round
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Tabs: Active vs Completed
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = CrimsonPrimary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = CrimsonPrimary,
              height = 3.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = "Active (${activeJobs.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED || it.status == DownloadStatus.QUEUED }})",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = "Completed (${completedEntities.size})",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      if (selectedTab == 0) {
        val nonCompleted = activeJobs.filter { it.status != DownloadStatus.COMPLETED }
        if (nonCompleted.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No active downloads currently running.",
                color = TextSecondary,
                fontSize = 14.sp
              )
            }
          }
        } else {
          items(nonCompleted, key = { it.id }) { job ->
            ActiveJobCard(
              job = job,
              onTogglePause = {
                if (job.status == DownloadStatus.PAUSED) onResumeJob(job.id) else onPauseJob(job.id)
              },
              onCancel = { onCancelJob(job.id) },
              onRetry = { onRetryJob(job.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }
        }
      } else {
        if (completedEntities.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No completed downloads yet.",
                color = TextSecondary,
                fontSize = 14.sp
              )
            }
          }
        } else {
          items(completedEntities, key = { it.id }) { entity ->
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
              date = "Saved"
            )

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
              AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Download") },
                text = { Text("Would you like to remove this item from your history only, or also delete the downloaded file from your device?") },
                confirmButton = {
                  Button(
                    onClick = {
                      showDeleteDialog = false
                      onDeleteHistory(entity.id, true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                  ) {
                    Text("Delete File & History")
                  }
                },
                dismissButton = {
                  OutlinedButton(
                    onClick = {
                      showDeleteDialog = false
                      onDeleteHistory(entity.id, false)
                    }
                  ) {
                    Text("History Only")
                  }
                }
              )
            }

            RecentDownloadCard(
              item = recentItem,
              onItemClick = {
                // Open real media with Gallery / Intent
                val mime = when (mType) {
                  MediaType.VIDEO -> "video/*"
                  MediaType.IMAGE -> "image/*"
                  MediaType.THUMBNAIL -> "image/*"
                  MediaType.AUDIO -> "audio/*"
                }
                StorageHelper.openMedia(context, entity.localUriString, mime)
              },
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
                showDeleteDialog = true
              },
              modifier = Modifier.padding(bottom = 10.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ActiveJobCard(
  job: DownloadJob,
  onTogglePause: () -> Unit,
  onCancel: () -> Unit,
  onRetry: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(2.dp, RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.dp, CardBorderStroke, RoundedCornerShape(18.dp))
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AsyncImage(
          model = job.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80" },
          contentDescription = job.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(width = 64.dp, height = 50.dp)
            .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = job.title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          PlatformBadge(platform = job.platform)
          Spacer(modifier = Modifier.height(2.dp))

          val subtext = when (job.status) {
            DownloadStatus.FAILED -> job.errorMessage ?: "Failed"
            DownloadStatus.PAUSED -> "Paused • ${StorageHelper.formatBytes(job.downloadedBytes)}"
            else -> "${StorageHelper.formatBytes(job.downloadedBytes)} • ${job.speedText} ${if (job.remainingTimeText.isNotEmpty()) "(${job.remainingTimeText})" else ""}"
          }

          Text(
            text = subtext,
            fontSize = 11.sp,
            color = if (job.status == DownloadStatus.FAILED) CrimsonPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (job.status == DownloadStatus.FAILED) {
          IconButton(
            onClick = onRetry,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Retry",
              tint = CrimsonPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        } else {
          IconButton(
            onClick = onTogglePause,
            modifier = Modifier.size(34.dp)
          ) {
            Icon(
              imageVector = if (job.status == DownloadStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (job.status == DownloadStatus.PAUSED) "Resume" else "Pause",
              tint = CrimsonPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        IconButton(
          onClick = onCancel,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel",
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { if (job.progress > 0) job.progress else 0.05f },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = if (job.status == DownloadStatus.PAUSED || job.status == DownloadStatus.FAILED) TextMuted else CrimsonPrimary,
        trackColor = CrimsonLight,
        strokeCap = StrokeCap.Round
      )
    }
  }
}
