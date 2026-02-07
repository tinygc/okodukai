package com.tinygc.okodukai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CoralRed,
    onPrimary = Cream,
    secondary = SkyBlue,
    onSecondary = Cream,
    background = Cream,
    onBackground = DeepInk,
    surface = Cream,
    onSurface = DeepInk
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralRed,
    onPrimary = Cream,
    secondary = SkyBlue,
    onSecondary = Cream,
    background = DeepInk,
    onBackground = Cream,
    surface = DeepInk,
    onSurface = Cream
)

@Composable
fun OkodukaiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
