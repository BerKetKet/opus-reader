package app.opusreader.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OpusColors.AccentBlue,
    secondary = OpusColors.AccentGold,
    background = OpusColors.BackgroundDark,
    surface = OpusColors.SurfaceDark,
    surfaceVariant = OpusColors.SurfaceVariantDark,
    onPrimary = OpusColors.TextPrimaryDark,
    onSecondary = OpusColors.TextPrimaryDark,
    onBackground = OpusColors.TextPrimaryDark,
    onSurface = OpusColors.TextPrimaryDark,
    onSurfaceVariant = OpusColors.TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = OpusColors.AccentBlue,
    secondary = OpusColors.AccentGold,
    background = OpusColors.BackgroundLight,
    surface = OpusColors.SurfaceLight,
    surfaceVariant = OpusColors.SurfaceLight,
    onPrimary = OpusColors.TextPrimaryLight,
    onSecondary = OpusColors.TextPrimaryLight,
    onBackground = OpusColors.TextPrimaryLight,
    onSurface = OpusColors.TextPrimaryLight,
    onSurfaceVariant = OpusColors.TextSecondaryLight
)

@Composable
fun OpusTheme(
    darkTheme: Boolean = true, // Default to dark theme as canonical Kindle reference
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OpusTypography,
        content = content
    )
}
