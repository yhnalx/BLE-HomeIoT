package com.example.bluetooth_iot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ModernSkeuomorphicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            surface = Color(0xFFE0E5EC),
            onSurface = Color(0xFF444444),
            primary = Color(0xFF1A73E8)
        ),
        content = content
    )
}
