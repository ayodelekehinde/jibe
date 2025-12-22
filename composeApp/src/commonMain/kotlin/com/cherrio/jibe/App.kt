package com.cherrio.jibe

import androidx.compose.runtime.Composable
import com.cherrio.jibe.design.JibeTheme
import com.cherrio.jibe.presentation.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    JibeTheme {
        HomeScreen()
    }
}