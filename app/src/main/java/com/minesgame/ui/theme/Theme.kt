package com.minesgame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.Black,
    tertiary = Cyan,
    background = Background,
    onBackground = TextPrimary,
    surface = Panel,
    onSurface = TextPrimary,
    surfaceVariant = Tile,
    onSurfaceVariant = SecondaryText,
    error = Red,
    onError = Color.White,
)

@Composable
fun MinesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MinesTypography,
        content = content,
    )
}
