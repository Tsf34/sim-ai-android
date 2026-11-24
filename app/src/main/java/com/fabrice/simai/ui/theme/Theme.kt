package com.fabrice.simai.ui.theme

import androidx.compose.material3.ColorScheme

/**
 * Schéma de couleurs du thème Sim AI (Vert & Noir).
 * On ne redéfinit pas les couleurs système dynamiques : le thème est voué à rester Vert & Noir.
 */
val SimAiColorScheme = ColorScheme(
    primary = SimAiGreen,
    onPrimary = SimAiBlack,
    primaryContainer = SimAiGreenDark,
    onPrimaryContainer = SimAiGreenLight,
    secondary = SimAiGreenLight,
    onSecondary = SimAiBlack,
    tertiary = SimAiRed,
    onTertiary = SimAiWhite,
    background = SimAiBlack,
    onBackground = SimAiWhite,
    surface = SimAiBlack2,
    onSurface = SimAiWhite,
    surfaceVariant = Color(0xFF121712),
    onSurfaceVariant = SimAiGray,
    outline = SimAiGray,
    error = SimAiRed,
    onError = SimAiWhite,
)
