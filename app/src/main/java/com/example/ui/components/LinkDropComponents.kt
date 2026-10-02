package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaType
import com.example.model.PlatformType
import com.example.model.RecentDownloadItem
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftBg
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.SoundCloudOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

/**
 * Top App Header matching LinkDrop reference:
 * Left: Soft pink rounded square with rotated red link icon + "LinkDrop" logo
 * Right: Crimson outline settings gear button
 */
@Composable
fun LinkDropHeader(
  modifier: Modifier = Modifier,
  onSettingsClick: () -> Unit = {},
  onLogoClick: (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Logo & Name
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .testTag("app_logo_container")
        .then(if (onLogoClick != null) Modifier.clickable { onLogoClick() } else Modifier)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CrimsonLight)
          .border(1.dp, CrimsonBorderLight, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Link,
          contentDescription = "LinkDrop Logo",
          tint = CrimsonPrimary,
          modifier = Modifier
            .size(22.dp)
            .rotate(-45f)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Link",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Drop",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = CrimsonPrimary
        )
      }
    }

    // Settings Gear Icon
    IconButton(
      onClick = onSettingsClick,
      modifier = Modifier
        .testTag("settings_button")
        .size(42.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Settings,
        contentDescription = "Settings",
        tint = CrimsonPrimary,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

/**
 * Crimson Analyze Link Button with sparkles, gradient, and right arrow
 */
@Composable
fun CrimsonAnalyzeButton(
  text: String = "Analyze Link",
  isLoading: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(54.dp)
      .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(27.dp),
        ambientColor = CrimsonPrimary.copy(alpha = 0.35f),
        spotColor = CrimsonPrimary.copy(alpha = 0.5f)
      )
      .clip(RoundedCornerShape(27.dp))
      .background(
        brush = Brush.horizontalGradient(
          colors = listOf(CrimsonPrimary, CrimsonDark)
        )
      )
      .clickable(enabled = !isLoading, onClick = onClick)
      .testTag("analyze_link_button"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 24.dp)
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = "Sparkle Icon",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Analyze Arrow",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

/**
 * 2-Column Grid Feature Card
 */
@Composable
fun FeatureCard(
  title: String,
  description: String,
  icon: ImageVector,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(86.dp)
      .shadow(
        elevation = 3.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = CrimsonPrimary.copy(alpha = 0.08f),
        spotColor = CrimsonPrimary.copy(alpha = 0.12f)
      )
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.dp, CardBorderStroke, RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
      .padding(horizontal = 10.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Rounded icon container
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CrimsonLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = CrimsonPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(9.dp))

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = 4.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = title,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          fontSize = 10.sp,
          fontWeight = FontWeight.Normal,
          color = TextSecondary,
          lineHeight = 13.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Navigate to $title",
        tint = CrimsonPrimary,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

/**
 * Platform Badge Icon
 */
@Composable
fun PlatformBadge(
  platform: PlatformType,
  modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
  ) {
    val (iconColor, icon) = when (platform) {
      PlatformType.YOUTUBE -> Pair(YouTubeRed, Icons.Default.PlayArrow)
      PlatformType.INSTAGRAM -> Pair(InstagramPink, Icons.Default.Image)
      PlatformType.SOUNDCLOUD -> Pair(SoundCloudOrange, Icons.Default.Cloud)
      PlatformType.TIKTOK -> Pair(Color.Black, Icons.Default.PlayArrow)
      PlatformType.TWITTER -> Pair(CrimsonPrimary, Icons.Default.Link)
      else -> Pair(CrimsonPrimary, Icons.Default.Link)
    }

    Box(
      modifier = Modifier
        .size(16.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(iconColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = platform.displayName,
        tint = Color.White,
        modifier = Modifier.size(11.dp)
      )
    }
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = platform.displayName,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Medium,
      color = TextSecondary
    )
  }
}

/**
 * Recent Download Card matching reference image
 */
@Composable
fun RecentDownloadCard(
  item: RecentDownloadItem,
  onItemClick: () -> Unit = {},
  onActionClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 2.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = CrimsonPrimary.copy(alpha = 0.06f),
        spotColor = CrimsonPrimary.copy(alpha = 0.08f)
      )
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.dp, CardBorderStroke, RoundedCornerShape(18.dp))
      .clickable(onClick = onItemClick)
      .testTag("recent_download_${item.id}")
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail with optional play overlay
      Box(
        modifier = Modifier
          .size(width = 68.dp, height = 54.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CrimsonSoftBg),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = item.thumbnailUrl,
          contentDescription = item.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(12.dp))
        )

        // Overlay play icon for video
        if (item.mediaType == MediaType.VIDEO) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Video playback icon",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Media details
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = 4.dp)
      ) {
        Text(
          text = item.title,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        PlatformBadge(platform = item.platform)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = item.specs,
          fontSize = 11.sp,
          color = TextMuted,
          maxLines = 1
        )
      }

      // Checkmark status circle badge
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(CrimsonPrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Download Completed",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Overflow menu
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier
            .size(32.dp)
            .testTag("menu_button_${item.id}")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More actions",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(Color.White)
        ) {
          DropdownMenuItem(
            text = { Text("Open Media") },
            onClick = {
              showMenu = false
              onItemClick()
            }
          )
          DropdownMenuItem(
            text = { Text("Share Link") },
            onClick = {
              showMenu = false
              onActionClick()
            }
          )
          DropdownMenuItem(
            text = { Text("Delete File", color = CrimsonPrimary) },
            onClick = {
              showMenu = false
              onDeleteClick()
            }
          )
        }
      }
    }
  }
}

/**
 * LinkDrop Bottom Navigation Bar matching reference image
 */
@Composable
fun LinkDropBottomBar(
  currentScreen: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(16.dp, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    color = Color.White,
    tonalElevation = 6.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      BottomNavItem(
        label = "Home",
        icon = Icons.Default.Home,
        isSelected = currentScreen == "home",
        testTag = "nav_home",
        onClick = { onNavigate("home") }
      )

      BottomNavItem(
        label = "Downloads",
        icon = Icons.Default.FileDownload,
        badgeCount = 2,
        isSelected = currentScreen == "downloads",
        testTag = "nav_downloads",
        onClick = { onNavigate("downloads") }
      )

      BottomNavItem(
        label = "History",
        icon = Icons.Default.History,
        isSelected = currentScreen == "history",
        testTag = "nav_history",
        onClick = { onNavigate("history") }
      )

      BottomNavItem(
        label = "Settings",
        icon = Icons.Outlined.Settings,
        isSelected = currentScreen == "settings",
        testTag = "nav_settings",
        onClick = { onNavigate("settings") }
      )
    }
  }
}

@Composable
private fun BottomNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  testTag: String,
  badgeCount: Int = 0,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag(testTag)
  ) {
    if (isSelected) {
      // Soft pink pill container around active icon
      Box(
        modifier = Modifier
          .size(width = 60.dp, height = 32.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(CrimsonLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = CrimsonPrimary,
          modifier = Modifier.size(20.dp)
        )
      }
    } else {
      Box(
        modifier = Modifier.size(width = 60.dp, height = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        if (badgeCount > 0) {
          BadgedBox(
            badge = {
              Badge(
                containerColor = CrimsonPrimary,
                contentColor = Color.White
              ) {
                Text(text = badgeCount.toString(), fontSize = 10.sp)
              }
            }
          ) {
            Icon(
              imageVector = icon,
              contentDescription = label,
              tint = TextSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
        } else {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = label,
      fontSize = 11.5.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) CrimsonPrimary else TextSecondary
    )

    Spacer(modifier = Modifier.height(2.dp))

    // Crimson horizontal indicator line under active tab
    if (isSelected) {
      Box(
        modifier = Modifier
          .width(24.dp)
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(CrimsonPrimary)
      )
    } else {
      Spacer(modifier = Modifier.height(3.dp))
    }
  }
}
