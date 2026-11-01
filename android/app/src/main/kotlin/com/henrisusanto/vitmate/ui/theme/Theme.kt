package com.henrisusanto.vitmate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF3E1200),
    secondary = AmberSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2C1600),
    background = WarmBackground,
    onBackground = TextPrimaryLight,
    surface = WarmSurface,
    onSurface = TextPrimaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF551900),
    primaryContainer = OrangePrimaryContainerDark,
    onPrimaryContainer = OrangePrimaryContainer,
    secondary = AmberSecondaryDark,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF452B00),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF5F3D00),
    onSecondaryContainer = AmberSecondaryContainer,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark
)

@Composable
fun JajanCuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
