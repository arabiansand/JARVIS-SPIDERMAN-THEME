package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.AppTheme

fun getThemeColorScheme(theme: AppTheme): ColorScheme {
    return when (theme) {
        AppTheme.MIDNIGHT_BLUE -> darkColorScheme(
            primary = CyanAccent,
            secondary = RedAccent,
            tertiary = Color(0xFF64FFDA),
            background = Color(0xFF0C1017),
            surface = Color(0xFF141C26),
            onPrimary = Color(0xFF0C1017),
            onSecondary = Color.White,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
        AppTheme.CYBER_RED -> darkColorScheme(
            primary = RedAccent,
            secondary = CyanAccent,
            tertiary = Color(0xFFFF9100),
            background = Color(0xFF14080A),
            surface = Color(0xFF220E13),
            onPrimary = Color.White,
            onSecondary = Color(0xFF14080A),
            onBackground = Color(0xFFF5E1E4),
            onSurface = Color(0xFFF5E1E4)
        )
        AppTheme.MATRIX_EMERALD -> darkColorScheme(
            primary = EmeraldAccent,
            secondary = CyanAccent,
            tertiary = Color(0xFFB9F6CA),
            background = Color(0xFF08140D),
            surface = Color(0xFF102418),
            onPrimary = Color(0xFF08140D),
            onSecondary = Color.White,
            onBackground = Color(0xFFE0F2E9),
            onSurface = Color(0xFFE0F2E9)
        )
        AppTheme.SOLAR_GOLD -> darkColorScheme(
            primary = GoldAccent,
            secondary = Color(0xFFFF5252),
            tertiary = Color(0xFFFFE57F),
            background = Color(0xFF141108),
            surface = Color(0xFF241F10),
            onPrimary = Color(0xFF141108),
            onSecondary = Color.White,
            onBackground = Color(0xFFF5EFE0),
            onSurface = Color(0xFFF5EFE0)
        )
    }
}

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.MIDNIGHT_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(appTheme)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

