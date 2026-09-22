package com.sabalapps.cuteanimalstrace.ui.theme

import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Soft mint primary, coral/peach accent, warm cream paper.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6E59), onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEEDD), onPrimaryContainer = Color(0xFF143C2D),
    inversePrimary = Color(0xFFA3D6BF),
    secondary = Color(0xFFB2584C), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCD3), onSecondaryContainer = Color(0xFF5B2A22),
    tertiary = Color(0xFF8B6420), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8BE), onTertiaryContainer = Color(0xFF4A3400),
    background = Color(0xFFFFFAF4), onBackground = Color(0xFF23282A),
    surface = Color(0xFFFFFAF4), onSurface = Color(0xFF23282A),
    surfaceVariant = Color(0xFFE8EDE6), onSurfaceVariant = Color(0xFF515C55),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF5EC),
    surfaceContainer = Color(0xFFF7EFE6),
    surfaceContainerHigh = Color(0xFFF1E9E0),
    surfaceContainerHighest = Color(0xFFEAE3DA),
    outline = Color(0xFF7A857D), outlineVariant = Color(0xFFD3DCD3),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FDCC0), onPrimary = Color(0xFF07382A),
    primaryContainer = Color(0xFF26513F), onPrimaryContainer = Color(0xFFBCEBD4),
    inversePrimary = Color(0xFF2F6E59),
    secondary = Color(0xFFF5BAAE), onSecondary = Color(0xFF552821),
    secondaryContainer = Color(0xFF6F3F37), onSecondaryContainer = Color(0xFFFFDCD3),
    tertiary = Color(0xFFEECE8E), onTertiary = Color(0xFF412F00),
    tertiaryContainer = Color(0xFF5C4512), onTertiaryContainer = Color(0xFFFFE8BE),
    background = Color(0xFF141815), onBackground = Color(0xFFE3E8E0),
    surface = Color(0xFF141815), onSurface = Color(0xFFE3E8E0),
    surfaceVariant = Color(0xFF3B463F), onSurfaceVariant = Color(0xFFBFCAC0),
    surfaceContainerLowest = Color(0xFF0E120F),
    surfaceContainerLow = Color(0xFF1A201C),
    surfaceContainer = Color(0xFF1F2621),
    surfaceContainerHigh = Color(0xFF2A322C),
    surfaceContainerHighest = Color(0xFF353D37),
    outline = Color(0xFF8A958B), outlineVariant = Color(0xFF3B463F),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    scrim = Color(0xFF000000),
)

private val Sans = FontFamily.SansSerif

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 41.sp, letterSpacing = (-0.4).sp),
    headlineLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 37.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 33.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 29.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 19.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.4.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun CuteAnimalsTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    } else if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
