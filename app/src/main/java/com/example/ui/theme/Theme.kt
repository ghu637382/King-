package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MayaCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = MayaViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF432A99),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = MayaTeal,
    onTertiary = Color(0xFF00382E),
    background = MayaDarkBackground,
    onBackground = MayaOnDark,
    surface = MayaDarkSurface,
    onSurface = MayaOnDark,
    surfaceVariant = MayaDarkSurfaceVariant,
    onSurfaceVariant = MayaOnDarkMuted,
    outline = MayaDarkBorder,
    error = MayaError
)

private val LightColorScheme = lightColorScheme(
    primary = MayaCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F6FF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = MayaVioletDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = Color(0xFF260058),
    tertiary = Color(0xFF00897B),
    onTertiary = Color.White,
    background = MayaLightBackground,
    onBackground = MayaOnLight,
    surface = MayaLightSurface,
    onSurface = MayaOnLight,
    surfaceVariant = MayaLightSurfaceVariant,
    onSurfaceVariant = MayaOnLightMuted,
    outline = MayaLightBorder,
    error = MayaError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature Maya voice styling by default
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
