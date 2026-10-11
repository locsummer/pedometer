package com.pedometer.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pedometer.app.data.model.ColorMode

@Composable
fun PedometerTheme(
    accentColor: Color = GreenPrimary,
    colorMode: ColorMode = ColorMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (colorMode) {
        ColorMode.DARK -> true
        ColorMode.LIGHT -> false
        ColorMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkCard,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightCard,
            onPrimary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
