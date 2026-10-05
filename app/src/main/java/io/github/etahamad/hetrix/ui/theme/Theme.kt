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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HetrixPrimaryDark,
    onPrimary = HetrixOnPrimaryDark,
    primaryContainer = HetrixPrimaryContainerDark,
    onPrimaryContainer = HetrixOnPrimaryContainerDark,
    secondary = HetrixSecondaryDark,
    onSecondary = HetrixOnSecondaryDark,
    secondaryContainer = HetrixSecondaryContainerDark,
    onSecondaryContainer = HetrixOnSecondaryContainerDark,
    tertiary = HetrixTertiaryDark,
    onTertiary = HetrixOnTertiaryDark,
    tertiaryContainer = HetrixTertiaryContainerDark,
    onTertiaryContainer = HetrixOnTertiaryContainerDark,
    error = HetrixErrorDark,
    onError = HetrixOnErrorDark,
    errorContainer = HetrixErrorContainerDark,
    onErrorContainer = HetrixOnErrorContainerDark,
    background = HetrixDarkBackground,
    onBackground = HetrixDarkOnBackground,
    surface = HetrixDarkSurface,
    onSurface = HetrixDarkOnSurface,
    surfaceVariant = HetrixDarkSurfaceVariant,
    onSurfaceVariant = HetrixDarkOnSurfaceVariant,
    surfaceContainerLowest = HetrixDarkSurfaceContainerLowest,
    surfaceContainerLow = HetrixDarkSurfaceContainerLow,
    surfaceContainer = HetrixDarkSurfaceContainer,
    surfaceContainerHigh = HetrixDarkSurfaceContainerHigh,
    surfaceContainerHighest = HetrixDarkSurfaceContainerHighest,
    outline = HetrixDarkOutline,
    outlineVariant = HetrixDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = HetrixPrimaryLight,
    onPrimary = HetrixOnPrimaryLight,
    primaryContainer = HetrixPrimaryContainerLight,
    onPrimaryContainer = HetrixOnPrimaryContainerLight,
    secondary = HetrixSecondaryLight,
    onSecondary = HetrixOnSecondaryLight,
    secondaryContainer = HetrixSecondaryContainerLight,
    onSecondaryContainer = HetrixOnSecondaryContainerLight,
    tertiary = HetrixTertiaryLight,
    onTertiary = HetrixOnTertiaryLight,
    tertiaryContainer = HetrixTertiaryContainerLight,
    onTertiaryContainer = HetrixOnTertiaryContainerLight,
    error = HetrixErrorLight,
    onError = HetrixOnErrorLight,
    errorContainer = HetrixErrorContainerLight,
    onErrorContainer = HetrixOnErrorContainerLight,
    background = HetrixLightBackground,
    onBackground = HetrixLightOnBackground,
    surface = HetrixLightSurface,
    onSurface = HetrixLightOnSurface,
    surfaceVariant = HetrixLightSurfaceVariant,
    onSurfaceVariant = HetrixLightOnSurfaceVariant,
    surfaceContainerLowest = HetrixLightSurfaceContainerLowest,
    surfaceContainerLow = HetrixLightSurfaceContainerLow,
    surfaceContainer = HetrixLightSurfaceContainer,
    surfaceContainerHigh = HetrixLightSurfaceContainerHigh,
    surfaceContainerHighest = HetrixLightSurfaceContainerHighest,
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
