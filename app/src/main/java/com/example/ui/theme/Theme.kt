package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkGlassColorScheme = darkColorScheme(
    primary = IndigoSoft,
    onPrimary = Color.White,
    primaryContainer = IndigoAccent,
    onPrimaryContainer = Color.White,
    secondary = TurquoiseAccent,
    onSecondary = MidnightDeep,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = EmeraldPaidDark,
    onTertiary = MidnightDeep,
    background = MidnightDeep,
    onBackground = TextOnDark,
    surface = SlateGlassDark,
    onSurface = TextOnDark,
    surfaceVariant = SlateGlassElevatedDark,
    onSurfaceVariant = TextMutedDark,
    error = RoseUnpaidDark,
    onError = Color.White,
    outline = GlassBorderDark
)

private val LightGlassColorScheme = lightColorScheme(
    primary = IndigoAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = EmeraldPaid,
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = TextOnLight,
    surface = LightGlassSurface,
    onSurface = TextOnLight,
    surfaceVariant = LightGlassElevated,
    onSurfaceVariant = TextMutedLight,
    error = RoseUnpaid,
    onError = Color.White,
    outline = GlassBorderLight
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun HesabatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkGlassColorScheme else LightGlassColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
