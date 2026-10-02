package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.storage.StorageHelper
import com.example.ui.components.CrimsonAnalyzeButton
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class EnhanceModel(
  val id: String,
  val name: String,
  val description: String,
  val multiplier: String
)

@Composable
fun EnhanceQualityScreen(
  onBack: () -> Unit,
  onEnhanceRequested: (scaleFactor: Int, engineId: String, onProgress: (Float) -> Unit, onDone: (Boolean, String?) -> Unit) -> Unit
) {
  BackHandler { onBack() }
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val models = listOf(
    EnhanceModel("m_4k", "AI Super-Resolution", "Upscale to crystal-clear high-definition", "HD"),
    EnhanceModel("m_face", "Portrait Detail Restoration", "Sharpen facial features, eyes, and hair details", "Face"),
    EnhanceModel("m_noise", "Grain & Artifact Suppression", "Remove compression noise and artifacts", "Clean"),
    EnhanceModel("m_hdr", "Dynamic HDR Color Tuning", "Expand color gamut and tonal contrast", "HDR")
  )

  var selectedModelId by remember { mutableStateOf(models.first().id) }
  var selectedScaleFactor by remember { mutableIntStateOf(4) } // 2 or 4
  var splitRatio by remember { mutableFloatStateOf(0.5f) }

  var isProcessing by remember { mutableStateOf(false) }
  var processingProgress by remember { mutableFloatStateOf(0f) }
  var completedSavedMessage by remember { mutableStateOf<String?>(null) }

  if (completedSavedMessage != null) {
    AlertDialog(
      onDismissRequest = { completedSavedMessage = null },
      icon = {
        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = CrimsonPrimary, modifier = Modifier.size(36.dp))
      },
      title = { Text("Quality Enhanced!") },
      text = { Text("The enhanced copy was saved separately to your device Gallery (Pictures/LinkDrop/Images). Original file was preserved.") },
      confirmButton = {
        Button(
          onClick = { completedSavedMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
          Text("Done")
        }
      }
    )
  }

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
          modifier = Modifier.testTag("enhance_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Text(
          text = "Enhance Quality",
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
      // Interactive Before / After Split View
      item {
        Text(
          text = "Before & After Interactive Preview",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Drag the slider to compare original vs enhanced.",
          fontSize = 12.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CrimsonSoftBg)
        ) {
          val totalWidth = maxWidth

          // Enhanced image layer
          AsyncImage(
            model = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000&auto=format&fit=crop&q=80",
            contentDescription = "Enhanced",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          // Original layer
          Box(
            modifier = Modifier
              .width(totalWidth * splitRatio)
              .fillMaxSize()
              .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
          ) {
            AsyncImage(
              model = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=300&auto=format&fit=crop&q=40",
              contentDescription = "Original Low-Res",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .width(totalWidth)
                .fillMaxSize()
                .blur(4.dp)
            )
          }

          // Split divider line & handle
          Box(
            modifier = Modifier
              .offset { IntOffset((totalWidth.toPx() * splitRatio).toInt() - 2.dp.roundToPx(), 0) }
              .width(4.dp)
              .fillMaxSize()
              .background(Color.White)
          )

          // Drag Handle
          Box(
            modifier = Modifier
              .offset { IntOffset((totalWidth.toPx() * splitRatio).toInt() - 18.dp.roundToPx(), (230.dp.toPx() / 2 - 18.dp.toPx()).toInt()) }
              .size(36.dp)
              .clip(CircleShape)
              .background(CrimsonPrimary)
              .border(2.dp, Color.White, CircleShape)
              .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  val newRatio = (splitRatio + dragAmount.x / totalWidth.toPx()).coerceIn(0.1f, 0.9f)
                  splitRatio = newRatio
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.UnfoldMore,
              contentDescription = "Drag to compare",
              tint = Color.White,
              modifier = Modifier
                .size(20.dp)
                .rotate(90f)
            )
          }

          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(10.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black.copy(alpha = 0.6f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(text = "Original 1080p", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(10.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(CrimsonPrimary)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(text = "Enhanced ${selectedScaleFactor}X", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // 2X vs 4X Scale Factor Selector
      item {
        Text(
          text = "Select Resolution Multiplier",
          fontSize = 14.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          FilterChip(
            selected = selectedScaleFactor == 2,
            onClick = { selectedScaleFactor = 2 },
            label = { Text("2X Upscale (Fast • Double Resolution)", fontWeight = FontWeight.SemiBold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CrimsonPrimary,
              selectedLabelColor = Color.White,
              containerColor = CrimsonLight,
              labelColor = CrimsonPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selectedScaleFactor == 2,
              borderColor = CrimsonBorderLight,
              selectedBorderColor = CrimsonPrimary
            )
          )

          FilterChip(
            selected = selectedScaleFactor == 4,
            onClick = { selectedScaleFactor = 4 },
            label = { Text("4X Super-Resolution (Ultra Crisp)", fontWeight = FontWeight.SemiBold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CrimsonPrimary,
              selectedLabelColor = Color.White,
              containerColor = CrimsonLight,
              labelColor = CrimsonPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selectedScaleFactor == 4,
              borderColor = CrimsonBorderLight,
              selectedBorderColor = CrimsonPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(18.dp))
      }

      // Enhancement Models
      item {
        Text(
          text = "Select Enhancement Engine",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(models.size) { i ->
        val m = models[i]
        val isSelected = m.id == selectedModelId

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
            .clickable { selectedModelId = m.id }
            .testTag("enhance_model_${m.id}")
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
                text = m.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = m.description,
                fontSize = 11.5.sp,
                color = TextSecondary
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) CrimsonPrimary else CrimsonLight)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = m.multiplier,
                color = if (isSelected) Color.White else CrimsonPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Processing state & button
      item {
        Spacer(modifier = Modifier.height(18.dp))

        if (isProcessing) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(CrimsonSoftBg)
              .border(1.dp, CardBorderStroke, RoundedCornerShape(16.dp))
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Processing ${selectedScaleFactor}X Image Enhancement...",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold,
              color = CrimsonPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { processingProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = CrimsonPrimary,
              trackColor = CrimsonLight,
              strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${(processingProgress * 100).toInt()}% • Upscaling & sharpening details",
              fontSize = 11.5.sp,
              color = TextSecondary
            )
          }
        } else {
          CrimsonAnalyzeButton(
            text = "Process & Save Enhanced (${selectedScaleFactor}X)",
            onClick = {
              isProcessing = true
              processingProgress = 0.05f
              onEnhanceRequested(
                selectedScaleFactor,
                selectedModelId,
                { progress -> processingProgress = progress },
                { success, errorMsg ->
                  isProcessing = false
                  if (success) {
                    completedSavedMessage = "Saved successfully"
                  } else {
                    scope.launch {
                      snackbarHostState.showSnackbar(errorMsg ?: "Enhancement failed")
                    }
                  }
                }
              )
            },
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}
