package com.cherrio.jibe

import androidx.compose.runtime.Composable
import com.cherrio.jibe.network.Device

interface Platform {
    val name: String
    val isMobile: Boolean
    suspend fun loadDevice(): Device
}

expect fun getPlatform(): Platform

@Composable
expect fun rememberQrCode(): () -> Unit

@Composable
expect fun rememberShutDown(): () -> Unit