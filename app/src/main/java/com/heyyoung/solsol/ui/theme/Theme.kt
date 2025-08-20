package com.heyyoung.solsol.ui.theme

import android.app.Activity
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
    primary = SolsolPrimaryDark,
    onPrimary = SolsolOnPrimaryDark,
    primaryContainer = SolsolPrimaryContainerDark,
    onPrimaryContainer = SolsolOnPrimaryContainerDark,
    secondary = SolsolSecondaryDark,
    onSecondary = SolsolOnSecondaryDark,
    secondaryContainer = SolsolSecondaryContainerDark,
    onSecondaryContainer = SolsolOnSecondaryContainerDark,
    background = SolsolBackground,
    onBackground = SolsolOnBackground,
    surface = SolsolSurface,
    onSurface = SolsolOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SolsolPrimary,
    onPrimary = SolsolOnPrimary,
    primaryContainer = SolsolPrimaryContainer,
    onPrimaryContainer = SolsolOnPrimaryContainer,
    secondary = SolsolSecondary,
    onSecondary = SolsolOnSecondary,
    secondaryContainer = SolsolSecondaryContainer,
    onSecondaryContainer = SolsolOnSecondaryContainer,
    background = SolsolBackground,
    onBackground = SolsolOnBackground,
    surface = SolsolSurface,
    onSurface = SolsolOnSurface
)

@Composable
fun SolsolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}