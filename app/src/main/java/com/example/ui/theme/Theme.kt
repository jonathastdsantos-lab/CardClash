package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonEmerald,
    secondary = NeonCyan,
    tertiary = BrightGold,
    background = StadiumObsidian,
    surface = StadiumConcrete,
    onPrimary = StadiumObsidian,
    onSecondary = StadiumObsidian,
    onBackground = HolographicMagenta,
    onSurface = HolographicMagenta
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NeonEmerald,
    secondary = NeonCyan,
    tertiary = BrightGold,
    background = StadiumObsidian, // Keep consistent dark mode background
    surface = StadiumConcrete,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = HolographicMagenta,
    onSurface = HolographicMagenta
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default to ensure maximum stability and our custom stadium styling
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
