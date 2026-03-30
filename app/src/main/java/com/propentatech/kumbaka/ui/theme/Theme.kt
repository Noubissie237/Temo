package com.propentatech.kumbaka.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * THÈME MYLIVE PREMIUM — Orange Exclusif
 * Mode Clair  : Orange + Blanc
 * Mode Sombre : Orange + Noir profond
 */
private val DarkColorScheme = darkColorScheme(
    primary                 = OrangePrimary,
    onPrimary               = Color.White,
    primaryContainer        = OrangeDark,
    onPrimaryContainer      = Color.White,
    secondary               = OrangeLight,
    onSecondary             = Color.White,
    secondaryContainer      = SurfaceVariantDark,
    onSecondaryContainer    = TextPrimaryDark,
    tertiary                = OrangeLight,
    onTertiary              = Color.White,
    background              = BackgroundDark,
    onBackground            = TextPrimaryDark,
    surface                 = SurfaceDark,
    onSurface               = TextPrimaryDark,
    surfaceVariant          = SurfaceVariantDark,
    onSurfaceVariant        = TextSecondaryDark,
    error                   = ErrorRed,
    onError                 = Color.White,
    outline                 = OrangePrimary.copy(alpha = 0.4f)
)

private val LightColorScheme = lightColorScheme(
    primary                 = OrangePrimary,
    onPrimary               = Color.White,
    primaryContainer        = OrangeGlow,
    onPrimaryContainer      = OrangeDark,
    secondary               = OrangeLight,
    onSecondary             = Color.White,
    secondaryContainer      = SurfaceVariantLight,
    onSecondaryContainer    = TextPrimary,
    tertiary                = OrangeLight,
    onTertiary              = Color.White,
    background              = BackgroundLight,
    onBackground            = TextPrimary,
    surface                 = SurfaceLight,
    onSurface               = TextPrimary,
    surfaceVariant          = SurfaceVariantLight,
    onSurfaceVariant        = TextSecondary,
    error                   = ErrorRed,
    onError                 = Color.White,
    outline                 = OrangePrimary.copy(alpha = 0.4f)
)

@Composable
fun MyLiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivé — on force notre palette orange
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Colorer la status bar avec l'orange
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = OrangePrimary.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}