package com.sabalapps.cuteanimalstrace.ui.theme

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

private val LightColors = lightColorScheme(
    primary = Color(0xFF356B59), onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EFE2), onPrimaryContainer = Color(0xFF173C30),
    secondary = Color(0xFF94564E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD3), onSecondaryContainer = Color(0xFF572D28),
    background = Color(0xFFFFF9F3), onBackground = Color(0xFF292E29),
    surface = Color(0xFFFFF9F3), onSurface = Color(0xFF292E29),
    surfaceVariant = Color(0xFFECEEE5), onSurfaceVariant = Color(0xFF535E55),
    surfaceContainer = Color(0xFFF3EFE7), surfaceContainerLow = Color(0xFFFAF3EC),
    surfaceContainerHigh = Color(0xFFECE8E0), outline = Color(0xFF737E74),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3D6BF), onPrimary = Color(0xFF0C3828),
    primaryContainer = Color(0xFF285140), onPrimaryContainer = Color(0xFFC0ECD5),
    secondary = Color(0xFFF1B6AC), onSecondary = Color(0xFF592D27),
    secondaryContainer = Color(0xFF74423A), onSecondaryContainer = Color(0xFFFFDAD3),
    background = Color(0xFF171C19), onBackground = Color(0xFFE4E8DF),
    surface = Color(0xFF171C19), onSurface = Color(0xFFE4E8DF),
    surfaceVariant = Color(0xFF3E4941), onSurfaceVariant = Color(0xFFC2CDC2),
    surfaceContainer = Color(0xFF222924), surfaceContainerLow = Color(0xFF1C231F),
    surfaceContainerHigh = Color(0xFF2D342E), outline = Color(0xFF8C988D),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 39.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 31.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp),
)

@Composable
fun CuteAnimalsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp)),
        content = content,
    )
}
