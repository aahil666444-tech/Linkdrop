package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaAnalyzeResult
import com.example.ui.components.CrimsonAnalyzeButton
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalysisUiState

@Composable
fun AnalyzeLoadingScreen(
  targetUrl: String,
  analysisState: AnalysisUiState,
  onSuccess: (MediaAnalyzeResult) -> Unit,
  onRetry: () -> Unit,
  onCancel: () -> Unit
) {
  BackHandler { onCancel() }

  LaunchedEffect(analysisState) {
    if (analysisState is AnalysisUiState.Success) {
      onSuccess(analysisState.media)
    }
  }

  // Pulsing animation
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = Color.White,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onCancel,
          modifier = Modifier.testTag("analyze_cancel_top")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel Analysis",
            tint = TextSecondary
          )
        }

        Text(
          text = if (analysisState is AnalysisUiState.Error) "Analysis Error" else "Analyzing Link",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.size(48.dp))
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      when (analysisState) {
        is AnalysisUiState.Error -> {
          // Error State
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(CrimsonLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Error",
              tint = CrimsonPrimary,
              modifier = Modifier.size(44.dp)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = "Unable to analyze this link.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = analysisState.message,
            fontSize = 13.5.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
          )

          Spacer(modifier = Modifier.height(24.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(CrimsonLight.copy(alpha = 0.5f))
              .border(1.dp, CrimsonBorderLight, RoundedCornerShape(14.dp))
              .padding(12.dp)
          ) {
            Text(
              text = targetUrl,
              fontSize = 12.sp,
              color = TextSecondary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(36.dp))

          CrimsonAnalyzeButton(
            text = "Try Direct Sample Link",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBorderLight)
          ) {
            Text(text = "Go Back", fontSize = 14.5.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
          }
        }

        else -> {
          // Loading / Analyzing State
          Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(150.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(CrimsonPrimary.copy(alpha = pulseAlpha))
            )

            Box(
              modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(CrimsonLight)
                .border(1.dp, CrimsonBorderLight, CircleShape)
            )

            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(CrimsonPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Extracting link",
                tint = Color.White,
                modifier = Modifier
                  .size(32.dp)
                  .rotate(-45f)
              )
            }
          }

          Spacer(modifier = Modifier.height(32.dp))

          Text(
            text = "Analyzing link...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Probing media streams and available formats...",
            fontSize = 13.5.sp,
            color = CrimsonPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(CrimsonLight.copy(alpha = 0.5f))
              .border(1.dp, CrimsonBorderLight, RoundedCornerShape(16.dp))
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Analysis sparkles",
                tint = CrimsonPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = targetUrl,
                fontSize = 12.5.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          LinearProgressIndicator(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .testTag("analyze_progress_bar"),
            color = CrimsonPrimary,
            trackColor = CrimsonLight,
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(40.dp))

          OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("cancel_analyze_button"),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = TextSecondary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonBorderLight)
          ) {
            Text(text = "Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
