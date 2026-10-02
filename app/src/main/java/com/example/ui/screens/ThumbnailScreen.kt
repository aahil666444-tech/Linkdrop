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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Wallpaper
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.CrimsonAnalyzeButton
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class ThumbnailQuality(
  val id: String,
  val label: String,
  val resolution: String,
  val size: String,
  val isRecommended: Boolean = false
)

@Composable
fun ThumbnailScreen(
  onBack: () -> Unit,
  onDownloadThumbnail: (ThumbnailQuality) -> Unit
) {
  BackHandler { onBack() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val thumbnailOptions = listOf(
    ThumbnailQuality("thumb_max", "MaxRes HD (Full Frame)", "1920 x 1080", "1.4 MB", true),
    ThumbnailQuality("thumb_hq", "High Quality Standard", "1280 x 720", "740 KB"),
    ThumbnailQuality("thumb_sd", "Standard Definition", "640 x 480", "320 KB")
  )

  var selectedId by remember { mutableStateOf(thumbnailOptions.first().id) }
  val selectedOpt = thumbnailOptions.first { it.id == selectedId }

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
          modifier = Modifier.testTag("thumbnail_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Save Thumbnail",
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
      // Thumbnail View
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(CrimsonSoftBg)
        ) {
          AsyncImage(
            model = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000&auto=format&fit=crop&q=80",
            contentDescription = "Full thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(10.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.75f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = selectedOpt.resolution,
              color = Color.White,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      item {
        Text(
          text = "Select Thumbnail Quality",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(thumbnailOptions.size) { i ->
        val opt = thumbnailOptions[i]
        val isSelected = opt.id == selectedId
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .shadow(
              elevation = if (isSelected) 3.dp else 1.dp,
              shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CrimsonSoftBg else Color.White)
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) CrimsonPrimary else CardBorderStroke,
              shape = RoundedCornerShape(16.dp)
            )
            .clickable { selectedId = opt.id }
            .testTag("thumb_opt_${opt.id}")
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) CrimsonPrimary else CrimsonBorder, CircleShape)
                .background(if (isSelected) CrimsonPrimary else Color.Transparent),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = Color.White,
                  modifier = Modifier.size(13.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = opt.label,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                if (opt.isRecommended) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(CrimsonPrimary)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "Highest Quality",
                      color = Color.White,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
              Text(
                text = "${opt.resolution}  •  JPG format",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Text(
              text = opt.size,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) CrimsonPrimary else TextPrimary
            )
          }
        }
      }

      // Actions
      item {
        Spacer(modifier = Modifier.height(18.dp))

        CrimsonAnalyzeButton(
          text = "Download Thumbnail (${selectedOpt.size})",
          onClick = { onDownloadThumbnail(selectedOpt) },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(CrimsonLight)
              .border(1.dp, CrimsonBorderLight, RoundedCornerShape(24.dp))
              .clickable {
                scope.launch {
                  snackbarHostState.showSnackbar("Thumbnail copied to clipboard!")
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Image",
                tint = CrimsonPrimary,
                modifier = Modifier.size(17.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Copy Image",
                color = CrimsonPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(CrimsonLight)
              .border(1.dp, CrimsonBorderLight, RoundedCornerShape(24.dp))
              .clickable {
                scope.launch {
                  snackbarHostState.showSnackbar("Set as device wallpaper!")
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Wallpaper,
                contentDescription = "Set Wallpaper",
                tint = CrimsonPrimary,
                modifier = Modifier.size(17.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Set Wallpaper",
                color = CrimsonPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
