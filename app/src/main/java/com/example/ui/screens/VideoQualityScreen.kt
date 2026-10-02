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
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataProvider
import com.example.model.VideoQualityOption
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

@Composable
fun VideoQualityScreen(
  onBack: () -> Unit,
  onStartDownload: (VideoQualityOption, String) -> Unit
) {
  BackHandler { onBack() }

  val media = MockDataProvider.getSampleMedia("")
  val qualities = media.videoQualities

  var selectedQualityId by remember { mutableStateOf(qualities.firstOrNull { it.isRecommended }?.id ?: qualities.first().id) }
  var selectedFormat by remember { mutableStateOf("MP4") }
  var selectedCodec by remember { mutableStateOf("H.264") }
  var embedSubtitles by remember { mutableStateOf(true) }

  val selectedOption = qualities.first { it.id == selectedQualityId }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
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
          modifier = Modifier.testTag("video_quality_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Video Quality Selection",
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
          text = "Select Video Resolution",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Choose your preferred resolution and frame rate.",
          fontSize = 12.5.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Quality Options List
      items(qualities, key = { it.id }) { option ->
        val isSelected = option.id == selectedQualityId

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .shadow(
              elevation = if (isSelected) 4.dp else 1.dp,
              shape = RoundedCornerShape(18.dp),
              ambientColor = CrimsonPrimary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) CrimsonSoftBg else Color.White)
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) CrimsonPrimary else CardBorderStroke,
              shape = RoundedCornerShape(18.dp)
            )
            .clickable { selectedQualityId = option.id }
            .testTag("video_option_${option.id}")
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Radio indicator circle
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                  width = 2.dp,
                  color = if (isSelected) CrimsonPrimary else CrimsonBorder,
                  shape = CircleShape
                )
                .background(if (isSelected) CrimsonPrimary else Color.Transparent),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = option.label,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                if (option.isRecommended) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(CrimsonPrimary)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "Recommended",
                      color = Color.White,
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                if (option.isHdr) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(CrimsonLight)
                      .border(1.dp, CrimsonBorderLight, RoundedCornerShape(6.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "HDR",
                      color = CrimsonPrimary,
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${option.resolution}  •  ${option.fps}  •  ${option.codec}",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }

            Text(
              text = option.fileSize,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) CrimsonPrimary else TextPrimary
            )
          }
        }
      }

      // Container Format selection
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Container Format",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("MP4", "MKV", "WebM").forEach { format ->
            FilterChip(
              selected = selectedFormat == format,
              onClick = { selectedFormat = format },
              label = { Text(text = format, fontWeight = FontWeight.SemiBold) },
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

      // Codec selection
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Video Codec",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("H.264", "H.265 / HEVC", "AV1").forEach { codec ->
            FilterChip(
              selected = selectedCodec == codec,
              onClick = { selectedCodec = codec },
              label = { Text(text = codec, fontWeight = FontWeight.SemiBold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CrimsonPrimary,
                selectedLabelColor = Color.White,
                containerColor = CrimsonLight,
                labelColor = CrimsonPrimary
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedCodec == codec,
                borderColor = CrimsonBorderLight,
                selectedBorderColor = CrimsonPrimary
              )
            )
          }
        }
      }

      // Subtitles Toggle
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CrimsonSoftBg)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Include Embedded Subtitles",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            Text(
              text = "English CC & Auto-translated tracks",
              fontSize = 11.5.sp,
              color = TextSecondary
            )
          }

          Switch(
            checked = embedSubtitles,
            onCheckedChange = { embedSubtitles = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = CrimsonPrimary,
              uncheckedThumbColor = TextMuted,
              uncheckedTrackColor = CrimsonLight
            )
          )
        }

        Spacer(modifier = Modifier.height(28.dp))
      }

      // Start Download Button
      item {
        CrimsonAnalyzeButton(
          text = "Download Video (${selectedOption.fileSize})",
          onClick = { onStartDownload(selectedOption, selectedFormat) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
