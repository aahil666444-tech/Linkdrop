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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockDataProvider
import com.example.model.BatchItem
import com.example.model.BatchStatus
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
fun BatchDownloadScreen(
  onBack: () -> Unit,
  onStartBatchDownload: (List<BatchItem>) -> Unit
) {
  BackHandler { onBack() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val batchList = remember { mutableStateListOf(*MockDataProvider.sampleBatchItems.toTypedArray()) }
  var multiLineInput by remember { mutableStateOf("") }

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
          modifier = Modifier.testTag("batch_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Batch Download",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.size(48.dp))
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
      item {
        Text(
          text = "Add Multiple Links",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Paste multiple video or image URLs separated by line breaks.",
          fontSize = 12.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Multi-line box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CrimsonBorderLight, RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          if (multiLineInput.isEmpty()) {
            Text(
              text = "https://youtube.com/watch?v=...\nhttps://instagram.com/p/...\nhttps://soundcloud.com/...",
              color = TextMuted,
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          }

          BasicTextField(
            value = multiLineInput,
            onValueChange = { multiLineInput = it },
            textStyle = TextStyle(
              color = TextPrimary,
              fontSize = 13.sp,
              lineHeight = 18.sp
            ),
            cursorBrush = SolidColor(CrimsonPrimary),
            modifier = Modifier
              .fillMaxSize()
              .testTag("batch_multiline_input")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              multiLineInput = "https://youtube.com/watch?v=scenic_8k\nhttps://instagram.com/reel/cat_vibes\nhttps://tiktok.com/@user/dance_trick"
              scope.launch {
                snackbarHostState.showSnackbar("Sample links pasted!")
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBorderLight)
          ) {
            Text(text = "Paste Demo Links", fontSize = 12.5.sp, color = CrimsonPrimary, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = {
              if (multiLineInput.isNotBlank()) {
                val lines = multiLineInput.lines().filter { it.isNotBlank() }
                lines.forEachIndexed { i, line ->
                  batchList.add(
                    BatchItem(
                      id = "b_${System.currentTimeMillis()}_$i",
                      url = line,
                      title = "Parsed Media Link #${batchList.size + 1}",
                      platform = com.example.model.PlatformType.YOUTUBE,
                      mediaType = com.example.model.MediaType.VIDEO,
                      status = BatchStatus.READY,
                      estimatedSize = "38.2 MB",
                      thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80"
                    )
                  )
                }
                multiLineInput = ""
                scope.launch {
                  snackbarHostState.showSnackbar("Added ${lines.size} links to batch!")
                }
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
            shape = RoundedCornerShape(20.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Add to Queue", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Queue list header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Batch Queue (${batchList.size} items)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          if (batchList.isNotEmpty()) {
            Text(
              text = "Clear All",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = CrimsonPrimary,
              modifier = Modifier
                .clickable { batchList.clear() }
                .padding(4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      items(batchList, key = { it.id }) { item ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AsyncImage(
              model = item.thumbnailUrl,
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .size(width = 62.dp, height = 48.dp)
                .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              PlatformBadge(platform = item.platform)
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Estimated: ${item.estimatedSize}",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            IconButton(
              onClick = { batchList.remove(item) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Remove",
                tint = CrimsonPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))

        if (batchList.isNotEmpty()) {
          CrimsonAnalyzeButton(
            text = "Download All (${batchList.size} items)",
            onClick = { onStartBatchDownload(batchList) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
