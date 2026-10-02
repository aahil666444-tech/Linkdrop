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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.MockDataProvider
import com.example.model.ImageQualityOption
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

@Composable
fun ImageDownloadScreen(
  onBack: () -> Unit,
  onSaveToGallery: (ImageQualityOption, String) -> Unit
) {
  BackHandler { onBack() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val media = MockDataProvider.getSampleMedia("")
  val imageOptions = media.imageQualities

  var selectedOptionId by remember { mutableStateOf(imageOptions.first().id) }
  var selectedFormat by remember { mutableStateOf("PNG") }
  var keepExif by remember { mutableStateOf(true) }
  var removeWatermark by remember { mutableStateOf(true) }

  val currentOption = imageOptions.first { it.id == selectedOptionId }

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
          modifier = Modifier.testTag("image_download_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Download Image",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        IconButton(
          onClick = {
            scope.launch {
              snackbarHostState.showSnackbar("Sharing image...")
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share Image",
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
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Preview Photo
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(20.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(CrimsonSoftBg)
        ) {
          AsyncImage(
            model = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800&auto=format&fit=crop&q=80",
            contentDescription = "Image preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(12.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = currentOption.dimensions,
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Resolution Picker
      item {
        Text(
          text = "Available Image Resolutions",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(imageOptions, key = { it.id }) { opt ->
        val isSelected = opt.id == selectedOptionId
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
            .clickable { selectedOptionId = opt.id }
            .testTag("image_opt_${opt.id}")
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
              Text(
                text = opt.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = opt.dimensions,
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Text(
              text = opt.fileSize,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) CrimsonPrimary else TextPrimary
            )
          }
        }
      }

      // Format Chips
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Save Format",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("PNG (Lossless)", "JPG (Standard)", "WEBP (Modern)").forEach { formatTag ->
            val format = formatTag.split(" ").first()
            FilterChip(
              selected = selectedFormat == format,
              onClick = { selectedFormat = format },
              label = { Text(text = formatTag, fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CrimsonPrimary,
                selectedLabelColor = Color.White,
                containerColor = CrimsonLight,
                labelColor = CrimsonPrimary
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedFormat == format,
                borderColor = CrimsonBorderLight,
                selectedBorderColor = CrimsonPrimary
              )
            )
          }
        }
      }

      // Toggles
      item {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrimsonSoftBg)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Remove Watermark / Clean Photo",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            Text(
              text = "Automatically clean platform overlays",
              fontSize = 11.5.sp,
              color = TextSecondary
            )
          }

          Switch(
            checked = removeWatermark,
            onCheckedChange = { removeWatermark = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CrimsonPrimary,
              uncheckedThumbColor = TextMuted,
              uncheckedTrackColor = CrimsonLight
            )
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CrimsonAnalyzeButton(
          text = "Save Image to Gallery (${currentOption.fileSize})",
          onClick = { onSaveToGallery(currentOption, selectedFormat) },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
