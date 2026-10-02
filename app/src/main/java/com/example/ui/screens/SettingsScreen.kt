package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.settings.AppSettings
import com.example.ui.components.LinkDropBottomBar
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  settings: AppSettings,
  onNavigateToScreen: (String) -> Unit,
  onToggleWifiOnly: (Boolean) -> Unit,
  onToggleNotification: (Boolean) -> Unit,
  onToggleAutoAnalyze: (Boolean) -> Unit,
  onSelectVideoQuality: (String) -> Unit,
  onSelectAudioFormat: (String) -> Unit,
  onClearHistory: () -> Unit,
  activeDownloadsCount: Int = 0
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var showVideoQualityDialog by remember { mutableStateOf(false) }
  var showAudioFormatDialog by remember { mutableStateOf(false) }
  var cacheCleaned by remember { mutableStateOf(false) }

  // Video Quality Dialog
  if (showVideoQualityDialog) {
    val options = listOf("4K Ultra HD (2160p)", "1080p Full HD", "720p HD", "480p SD")
    AlertDialog(
      onDismissRequest = { showVideoQualityDialog = false },
      title = { Text("Default Video Quality") },
      text = {
        Column {
          options.forEach { opt ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectVideoQuality(opt)
                  showVideoQualityDialog = false
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.defaultVideoQuality == opt,
                onClick = {
                  onSelectVideoQuality(opt)
                  showVideoQualityDialog = false
                },
                colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = opt, fontSize = 14.sp, color = TextPrimary)
            }
          }
        }
      },
      confirmButton = {}
    )
  }

  // Audio Format Dialog
  if (showAudioFormatDialog) {
    val options = listOf("MP3 (320 kbps)", "M4A / AAC (256 kbps)", "FLAC Lossless", "MP3 (128 kbps)")
    AlertDialog(
      onDismissRequest = { showAudioFormatDialog = false },
      title = { Text("Default Audio Format") },
      text = {
        Column {
          options.forEach { opt ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSelectAudioFormat(opt)
                  showAudioFormatDialog = false
                }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.defaultAudioFormat == opt,
                onClick = {
                  onSelectAudioFormat(opt)
                  showAudioFormatDialog = false
                },
                colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = opt, fontSize = 14.sp, color = TextPrimary)
            }
          }
        }
      },
      confirmButton = {}
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
        Text(
          text = "Settings",
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimary
        )
      }
    },
    bottomBar = {
      LinkDropBottomBar(
        currentScreen = "settings",
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
      // Preferences Section
      item {
        SectionTitle(title = "Download Preferences")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
          SettingsClickableRow(
            icon = Icons.Default.Folder,
            title = "Download Location",
            subtitle = "Android MediaStore (Movies/LinkDrop, Pictures/LinkDrop)",
            onClick = {
              scope.launch {
                snackbarHostState.showSnackbar("Downloads are saved directly to device Gallery & Files!")
              }
            }
          )

          Spacer(modifier = Modifier.height(10.dp))

          SettingsClickableRow(
            icon = Icons.Default.VideoSettings,
            title = "Default Video Quality",
            subtitle = settings.defaultVideoQuality,
            onClick = { showVideoQualityDialog = true }
          )

          Spacer(modifier = Modifier.height(10.dp))

          SettingsClickableRow(
            icon = Icons.Default.Settings,
            title = "Default Audio Format",
            subtitle = settings.defaultAudioFormat,
            onClick = { showAudioFormatDialog = true }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Network & Automation
      item {
        SectionTitle(title = "Network & Automation")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
          SettingsSwitchRow(
            icon = Icons.Default.NetworkWifi,
            title = "Download over Wi-Fi Only",
            subtitle = "Prevent mobile data consumption",
            checked = settings.wifiOnly,
            onCheckedChange = onToggleWifiOnly
          )

          Spacer(modifier = Modifier.height(10.dp))

          SettingsSwitchRow(
            icon = Icons.Default.Notifications,
            title = "Notify on Completion",
            subtitle = "Show notification when downloads finish",
            checked = settings.notifyOnComplete,
            onCheckedChange = onToggleNotification
          )

          Spacer(modifier = Modifier.height(10.dp))

          SettingsSwitchRow(
            icon = Icons.Default.Settings,
            title = "Auto-Analyze on Paste",
            subtitle = "Start analysis immediately upon paste",
            checked = settings.autoAnalyzeOnPaste,
            onCheckedChange = onToggleAutoAnalyze
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Storage & Cache
      item {
        SectionTitle(title = "Storage & Cleanup")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                try {
                  context.cacheDir.deleteRecursively()
                  cacheCleaned = true
                  scope.launch { snackbarHostState.showSnackbar("Temporary app cache cleared!") }
                } catch (_: Exception) {}
              }
              .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CrimsonLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = "Clear Cache",
                tint = CrimsonPrimary,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Clear Temporary Cache",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = if (cacheCleaned) "0 B used" else "Cached network responses and temporary streams",
                fontSize = 11.5.sp,
                color = TextSecondary
              )
            }

            Text(
              text = if (cacheCleaned) "Cleaned" else "Clear",
              color = CrimsonPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onClearHistory()
                scope.launch { snackbarHostState.showSnackbar("All download history cleared!") }
              }
              .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CrimsonLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear History",
                tint = CrimsonPrimary,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Clear Download History Log",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = "Remove all completed records from the database",
                fontSize = 11.5.sp,
                color = TextSecondary
              )
            }

            Text(
              text = "Clear",
              color = CrimsonPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // About
      item {
        SectionTitle(title = "About LinkDrop")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
          // App Picture Banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(CrimsonLight)
          ) {
            coil.compose.AsyncImage(
              model = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
              contentDescription = "LinkDrop App Banner",
              contentScale = androidx.compose.ui.layout.ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                      CrimsonPrimary.copy(alpha = 0.85f),
                      Color.Black.copy(alpha = 0.6f)
                    )
                  )
                )
            )
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.White),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "LinkDrop App Icon",
                  tint = CrimsonPrimary,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "LinkDrop Studio",
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "High-Speed Native Media Downloader",
                  color = Color.White.copy(alpha = 0.9f),
                  fontSize = 11.5.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Official LinkDrop App Picture
          Image(
            painter = painterResource(id = R.drawable.img_app_picture),
            contentDescription = "LinkDrop App Picture",
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.FillWidth
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CrimsonLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About",
                tint = CrimsonPrimary,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "LinkDrop for Android",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Version 2.4.0 (Build 2026.09) • Native Storage",
                fontSize = 11.5.sp,
                color = TextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Supported platforms: Direct MP4/WebM videos, JPG/PNG/WebP images, MP3/FLAC audio, and public media links.",
            fontSize = 11.5.sp,
            color = TextMuted,
            lineHeight = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}

@Composable
private fun SectionTitle(title: String) {
  Text(
    text = title,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = CrimsonPrimary
  )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      content()
    }
  }
}

@Composable
private fun SettingsClickableRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(CrimsonLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = CrimsonPrimary,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
      Text(text = subtitle, fontSize = 11.5.sp, color = TextSecondary)
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = "Open",
      tint = CrimsonPrimary,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(CrimsonLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = CrimsonPrimary,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
      Text(text = subtitle, fontSize = 11.5.sp, color = TextSecondary)
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = CrimsonPrimary,
        uncheckedThumbColor = TextMuted,
        uncheckedTrackColor = CrimsonLight
      )
    )
  }
}
