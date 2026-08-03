package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WinCyanPrimary,
    onPrimary = WinBlueDarkBg,
    primaryContainer = WinCyanSecondary,
    secondary = WinCyanSecondary,
    background = WinBlueDarkBg,
    surface = WinCardBg,
    onBackground = WinTextPrimary,
    onSurface = WinTextPrimary,
    outline = WinBorderColor
)

private val LightColorScheme = lightColorScheme(
    primary = WinCyanSecondary,
    onPrimary = WinTextPrimary,
    primaryContainer = WinCyanPrimary,
    secondary = WinCyanSecondary,
    background = WinBlueDarkBg,
    surface = WinCardBg,
    onBackground = WinTextPrimary,
    onSurface = WinTextPrimary,
    outline = WinBorderColor
)

@Composable
fun WinLinkTheme(
    darkTheme: Boolean = true, // default dark mode for tech network UI
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
