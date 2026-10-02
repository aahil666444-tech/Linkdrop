package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.DownloadHistoryEntity
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.storage.StorageHelper
import com.example.ui.components.LinkDropBottomBar
import com.example.ui.components.RecentDownloadCard
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
  historyEntities: List<DownloadHistoryEntity>,
  onNavigateToScreen: (String) -> Unit,
  onDeleteItem: (String, Boolean) -> Unit,
  onClearAll: (Boolean) -> Unit,
  activeDownloadsCount: Int = 0
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }
  var pendingDeleteItem by remember { mutableStateOf<DownloadHistoryEntity?>(null) }
  var showClearAllDialog by remember { mutableStateOf(false) }

  val filteredList = historyEntities.filter { entity ->
    val matchesSearch = entity.filename.contains(searchQuery, ignoreCase = true) ||
      entity.platform.contains(searchQuery, ignoreCase = true)
    val matchesFilter = when (selectedFilter) {
      "Videos" -> entity.mediaType.equals("VIDEO", ignoreCase = true)
      "Images" -> entity.mediaType.equals("IMAGE", ignoreCase = true)
      "Audio" -> entity.mediaType.equals("AUDIO", ignoreCase = true)
      "Thumbnails" -> entity.mediaType.equals("THUMBNAIL", ignoreCase = true)
      else -> true
    }
    matchesSearch && matchesFilter
  }

  // Delete Dialog with choices
  if (pendingDeleteItem != null) {
    val item = pendingDeleteItem!!
    AlertDialog(
      onDismissRequest = { pendingDeleteItem = null },
      title = { Text("Delete Download") },
      text = { Text("Choose whether to remove '${item.filename}' from your history only or also delete the downloaded file from your device storage.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteItem(item.id, true)
            pendingDeleteItem = null
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
            onDeleteItem(item.id, false)
            pendingDeleteItem = null
            scope.launch { snackbarHostState.showSnackbar("Removed from history (file preserved)") }
          }
        ) {
          Text("History Only")
        }
      }
    )
  }

  // Clear All Dialog
  if (showClearAllDialog) {
    AlertDialog(
      onDismissRequest = { showClearAllDialog = false },
      title = { Text("Clear All History") },
      text = { Text("Do you want to clear your history log only, or also delete all downloaded files from storage?") },
      confirmButton = {
        Button(
          onClick = {
            onClearAll(true)
            showClearAllDialog = false
            scope.launch { snackbarHostState.showSnackbar("Cleared history and all files") }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
          Text("Delete All Files")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            onClearAll(false)
            showClearAllDialog = false
            scope.launch { snackbarHostState.showSnackbar("History log cleared (files kept)") }
          }
        ) {
          Text("Clear History Only")
        }
      }
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
    snackbarHost = { SnackbarHost(snackbarHostState) },
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
            text = "Download History",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
          )

          if (historyEntities.isNotEmpty()) {
            IconButton(
              onClick = { showClearAllDialog = true }
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear History",
                tint = CrimsonPrimary
              )
            }
          }
        }
      }
    },
    bottomBar = {
      LinkDropBottomBar(
        currentScreen = "history",
        onNavigate = onNavigateToScreen,
        activeDownloadsCount = activeDownloadsCount
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Search Bar
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, CrimsonBorderLight, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = CrimsonPrimary,
              modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search downloaded files...",
                  color = TextMuted,
                  fontSize = 13.5.sp
                )
              }
              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(CrimsonPrimary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("history_search_input")
              )
            }

            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { searchQuery = "" },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                  tint = TextSecondary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Filter chips
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val filters = listOf("All", "Videos", "Images", "Audio", "Thumbnails")
          items(filters) { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = { Text(text = filter, fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CrimsonPrimary,
                selectedLabelColor = Color.White,
                containerColor = CrimsonLight,
                labelColor = CrimsonPrimary
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedFilter == filter,
                borderColor = CrimsonBorderLight,
                selectedBorderColor = CrimsonPrimary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // List of history items from Room database
      if (filteredList.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (searchQuery.isEmpty()) "No history records found." else "No files found matching '$searchQuery'.",
              color = TextSecondary,
              fontSize = 14.sp
            )
          }
        }
      } else {
        items(filteredList, key = { it.id }) { entity ->
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

          RecentDownloadCard(
            item = recentItem,
            onItemClick = {
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
              pendingDeleteItem = entity
            },
            modifier = Modifier.padding(bottom = 10.dp)
          )
        }
      }
    }
  }
}
