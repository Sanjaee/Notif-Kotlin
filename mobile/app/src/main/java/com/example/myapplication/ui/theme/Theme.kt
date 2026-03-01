package com.example.myapplication.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray300,
    onSecondary = Black,
    tertiary = Gray400,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = Gray900,
    onSurface = White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    error = Color(0xFFEF4444),
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Gray700,
    onSecondary = White,
    tertiary = Gray600,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    error = Color(0xFFDC2626),
    onError = White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force light theme for black & white design
    // Dynamic color disabled for consistent black & white theme
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