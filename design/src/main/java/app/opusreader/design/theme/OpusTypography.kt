package app.opusreader.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.opusreader.design.R

val BookerlyFontFamily = FontFamily(
    Font(R.font.bookerly_regular, FontWeight.Normal),
    Font(R.font.bookerly_bold, FontWeight.Bold),
    Font(R.font.bookerly_bold, FontWeight.SemiBold),
    Font(R.font.bookerly_display, FontWeight.Medium)
)

val AmazonEmberFontFamily = FontFamily(
    Font(R.font.amazon_ember_light, FontWeight.Light),
    Font(R.font.amazon_ember_regular, FontWeight.Normal),
    Font(R.font.amazon_ember_medium, FontWeight.Medium),
    Font(R.font.amazon_ember_medium, FontWeight.SemiBold),
    Font(R.font.amazon_ember_medium, FontWeight.Bold)
)

private val defaultPlatformStyle = PlatformTextStyle(includeFontPadding = false)

val OpusTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = BookerlyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = BookerlyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    titleLarge = TextStyle(
        fontFamily = BookerlyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    titleMedium = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    titleSmall = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextSecondaryDark,
        platformStyle = defaultPlatformStyle
    ),
    bodySmall = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextTertiaryDark,
        platformStyle = defaultPlatformStyle
    ),
    labelLarge = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextPrimaryDark,
        platformStyle = defaultPlatformStyle
    ),
    labelMedium = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextSecondaryDark,
        platformStyle = defaultPlatformStyle
    ),
    labelSmall = TextStyle(
        fontFamily = AmazonEmberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        color = OpusColors.TextTertiaryDark,
        platformStyle = defaultPlatformStyle
    )
)
