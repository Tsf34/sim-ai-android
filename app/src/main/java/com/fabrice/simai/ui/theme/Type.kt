package com.fabrice.simai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/**
 * Application du thème Sim AI : Vert & Noir, moderne et mobile-first.
 */
@Composable
fun SimAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SimAiColorScheme,
        typography = Typography(),
        content = content
    )
}
