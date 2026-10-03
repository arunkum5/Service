package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BrightRed,
    onPrimary = Color.White,
    primaryContainer = DarkCrimson,
    onPrimaryContainer = Color.White,
    secondary = AccentGold,
    onSecondary = Color.Black,
    background = DeepCharcoal,
    surface = CardDark,
    surfaceVariant = Color(0xFF2A2A30),
    onSurface = Color.White,
    onBackground = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = DarkCrimson,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = LightGrayBg,
    surface = LightGraySurface,
    surfaceVariant = LightGrayBorder,
    onSurface = TextDark,
    onBackground = TextDark,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep distinct GVD Auto red brand identity
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
