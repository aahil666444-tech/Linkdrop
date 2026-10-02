package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataProvider
import com.example.model.AudioQualityOption
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
fun AudioSelectionScreen(
  onBack: () -> Unit,
  onExtractAudio: (AudioQualityOption) -> Unit
) {
  BackHandler { onBack() }

  val media = MockDataProvider.getSampleMedia("")
  val audioOptions = media.audioQualities

  var selectedOptionId by remember { mutableStateOf(audioOptions.first().id) }
  var isPlaying by remember { mutableStateOf(false) }
  var embedArtwork by remember { mutableStateOf(true) }
  var trimRange by remember { mutableStateOf(0f..100f) }

  val selectedOption = audioOptions.first { it.id == selectedOptionId }

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
          modifier = Modifier.testTag("audio_selection_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Extract Audio",
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
      // Audio Waveform Card & Player Preview
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = CrimsonSoftBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Soundtrack & Audio Stream",
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Clean 320kbps stereo extraction",
                  fontSize = 12.sp,
                  color = TextSecondary
                )
              }

              // Play/pause preview button
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(CrimsonPrimary)
                  .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = "Preview Audio",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated waveform bars
            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              val barWidth = 4.dp.toPx()
              val barSpacing = 3.dp.toPx()
              val count = (size.width / (barWidth + barSpacing)).toInt()
              val heights = listOf(
                0.2f, 0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.8f,
                0.45f, 0.85f, 0.95f, 0.5f, 0.35f, 0.65f, 0.75f, 0.3f,
                0.55f, 0.8f, 0.9f, 0.4f, 0.6f, 0.25f, 0.7f, 0.85f
              )

              for (i in 0 until count) {
                val factor = heights[i % heights.size]
                val h = size.height * factor
                val x = i * (barWidth + barSpacing)
                val y = (size.height - h) / 2
                drawRoundRect(
                  color = if (i < count * 0.45f) CrimsonPrimary else CrimsonBorder,
                  topLeft = Offset(x, y),
                  size = Size(barWidth, h),
                  cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration labels
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "00:00", fontSize = 11.5.sp, color = TextSecondary)
              Text(text = "08:42", fontSize = 11.5.sp, color = TextSecondary)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Audio Format & Bitrate Options
      item {
        Text(
          text = "Audio Format & Bitrate",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(audioOptions, key = { it.id }) { opt ->
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
            .testTag("audio_opt_${opt.id}")
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
                  text = "${opt.format} (${opt.bitrate})",
                  fontSize = 14.5.sp,
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
                      text = "Studio Best",
                      color = Color.White,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
              Text(
                text = "Sample Rate: ${opt.sampleRate}",
                fontSize = 11.5.sp,
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

      // Trimmer Controls
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Audio Trimmer (Optional)",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Extract only a specific section or ringtone clip.",
          fontSize = 12.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        RangeSlider(
          value = trimRange,
          onValueChange = { trimRange = it },
          valueRange = 0f..100f,
          colors = SliderDefaults.colors(
            thumbColor = CrimsonPrimary,
            activeTrackColor = CrimsonPrimary,
            inactiveTrackColor = CrimsonLight
          )
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Start: 00:00",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CrimsonPrimary
          )
          Text(
            text = "End: 08:42",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CrimsonPrimary
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Embed ID3 Tag Toggle
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
              text = "Embed Album Cover & ID3 Tags",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            Text(
              text = "Includes artwork, artist, and track metadata",
              fontSize = 11.5.sp,
              color = TextSecondary
            )
          }

          Switch(
            checked = embedArtwork,
            onCheckedChange = { embedArtwork = it },
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
          text = "Extract & Download (${selectedOption.fileSize})",
          onClick = { onExtractAudio(selectedOption) },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
