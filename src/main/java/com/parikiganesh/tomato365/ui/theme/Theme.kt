package com.parikiganesh.tomato365.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenOnContainer,
    secondary = TomatoRed,
    onSecondary = TomatoOnRed,
    background = AppBackground,
    surface = AppSurface
)

@Composable
fun TomatoPricesTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTypography provides AppTypography()) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = Typography,
            content = content
        )
    }
}
