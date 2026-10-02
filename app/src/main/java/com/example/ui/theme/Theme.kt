package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = CrimsonPrimary,
  onPrimary = Color.White,
  primaryContainer = CrimsonLight,
  onPrimaryContainer = CrimsonDark,
  secondary = CrimsonDark,
  onSecondary = Color.White,
  secondaryContainer = CrimsonSoftBg,
  onSecondaryContainer = CrimsonPrimary,
  tertiary = CrimsonPrimary,
  onTertiary = Color.White,
  background = BackgroundLight,
  onBackground = TextPrimary,
  surface = SurfaceLight,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondary,
  outline = CrimsonBorder,
  outlineVariant = CrimsonBorderLight
)

private val DarkColorScheme = darkColorScheme(
  primary = CrimsonPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF4A1017),
  onPrimaryContainer = Color(0xFFFFD1D6),
  secondary = Color(0xFFFF6B7A),
  onSecondary = Color.Black,
  background = Color(0xFF121214),
  onBackground = Color(0xFFE4E4E7),
  surface = Color(0xFF1A1A1E),
  onSurface = Color(0xFFE4E4E7),
  surfaceVariant = Color(0xFF24242A),
  onSurfaceVariant = Color(0xFFA1A1AA),
  outline = Color(0xFF4A2026),
  outlineVariant = Color(0xFF32161A)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Keep reference visual theme clean and light as requested
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
