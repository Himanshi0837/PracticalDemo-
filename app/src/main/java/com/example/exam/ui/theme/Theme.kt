
package com.example.exam.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(

    // Primary
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary
    secondary = SecondaryRose,
    onSecondary = Color.White,
    secondaryContainer = RoseGold,
    onSecondaryContainer = Wine,

    // Tertiary
    tertiary = RoseGold,
    onTertiary = Wine,
    tertiaryContainer = AppSurfaceVariant,
    onTertiaryContainer = TextPrimary,

    // Background
    background = AppBackground,
    onBackground = TextPrimary,

    // Surface
    surface = AppSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    // Borders
    outline = AppBorder,
    outlineVariant = DividerColor,

    // Error
    error = SignalRed,
    onError = Color.White,
    errorContainer = SignalRedBg,
    onErrorContainer = SignalRed,

    // Inverse
    inverseSurface = Wine,
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryBlueLight
)

@Composable
fun ExamTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window

            window?.let {
                it.statusBarColor = AppBackground.toArgb()
                it.navigationBarColor = AppBackground.toArgb()

                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
