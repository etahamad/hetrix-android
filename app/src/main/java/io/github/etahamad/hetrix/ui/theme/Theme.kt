package io.github.etahamad.hetrix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HetrixPrimary,
    onPrimary = HetrixOnPrimary,
    primaryContainer = HetrixPrimaryContainer,
    onPrimaryContainer = HetrixOnPrimaryContainer,
    secondary = HetrixSecondary,
    onSecondary = HetrixOnSecondary,
    secondaryContainer = HetrixSecondaryContainer,
    onSecondaryContainer = HetrixOnSecondaryContainer,
    tertiary = HetrixTertiary,
    onTertiary = HetrixOnTertiary,
    tertiaryContainer = HetrixTertiaryContainer,
    onTertiaryContainer = HetrixOnTertiaryContainer,
    error = HetrixError,
    onError = HetrixOnError,
    errorContainer = HetrixErrorContainer,
    onErrorContainer = HetrixOnErrorContainer,
    background = HetrixDarkBackground,
    onBackground = HetrixDarkOnBackground,
    surface = HetrixDarkSurface,
    onSurface = HetrixDarkOnSurface,
    surfaceVariant = HetrixDarkSurfaceVariant,
    onSurfaceVariant = HetrixDarkOnSurfaceVariant,
    outline = HetrixDarkOutline,
    outlineVariant = HetrixDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0D9488),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF5B21B6),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = HetrixLightBackground,
    onBackground = HetrixLightOnBackground,
    surface = HetrixLightSurface,
    onSurface = HetrixLightOnSurface,
    surfaceVariant = HetrixLightSurfaceVariant,
    onSurfaceVariant = HetrixLightOnSurfaceVariant,
    outline = HetrixLightOutline,
    outlineVariant = HetrixLightOutlineVariant
)

@Composable
fun HetrixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HetrixTypography,
        shapes = HetrixShapes,
        content = content
    )
}
