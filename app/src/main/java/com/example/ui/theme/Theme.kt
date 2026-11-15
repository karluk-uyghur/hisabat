package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class HesabatColors(
    val scrimTop: Color,
    val scrimBottom: Color,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val glassInnerSurface: Color,
    val glassBorder: Color,
    val paidText: Color,
    val paidBg: Color,
    val paidBorder: Color,
    val unpaidText: Color,
    val unpaidBg: Color,
    val unpaidBorder: Color,
    val editText: Color,
    val badgeGradientStart: Color,
    val badgeGradientMid: Color,
    val badgeGradientEnd: Color,
)

private val DarkHesabatColors = HesabatColors(
    scrimTop = Color(0xCC090D16),
    scrimBottom = Color(0xF2090D16),
    glassSurface = SlateGlassDark,
    glassSurfaceElevated = SlateGlassElevatedDark,
    glassInnerSurface = Color(0x991E293B),
    glassBorder = GlassBorderDark,
    paidText = EmeraldPaidDark,
    paidBg = Color(0x2610B981),
    paidBorder = Color(0x5910B981),
    unpaidText = RoseUnpaidDark,
    unpaidBg = Color(0x26F43F5E),
    unpaidBorder = Color(0x59F43F5E),
    editText = Color(0xFFFBBF24),
    badgeGradientStart = IndigoAccent,
    badgeGradientMid = Color(0xFF6366F1),
    badgeGradientEnd = TurquoiseAccent,
)

private val LightHesabatColors = HesabatColors(
    scrimTop = Color(0xD9F8FAFC),
    scrimBottom = Color(0xF5F1F5F9),
    glassSurface = LightGlassSurface,
    glassSurfaceElevated = LightGlassElevated,
    glassInnerSurface = Color(0xCCFFFFFF),
    glassBorder = GlassBorderLight,
    paidText = EmeraldPaid,
    paidBg = Color(0x1F059669),
    paidBorder = Color(0x4D059669),
    unpaidText = RoseUnpaid,
    unpaidBg = Color(0x1FE11D48),
    unpaidBorder = Color(0x4DE11D48),
    editText = Color(0xFFD97706),
    badgeGradientStart = IndigoAccent,
    badgeGradientMid = Color(0xFF4F46E5),
    badgeGradientEnd = Color(0xFF0891B2),
)

val LocalHesabatColors = staticCompositionLocalOf { DarkHesabatColors }

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
    val extendedColors = if (darkTheme) DarkHesabatColors else LightHesabatColors

    CompositionLocalProvider(LocalHesabatColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
