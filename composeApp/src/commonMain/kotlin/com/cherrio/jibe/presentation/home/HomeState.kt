package com.cherrio.jibe.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material.icons.filled.PhoneAndroid
import com.cherrio.jibe.network.Device

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val device: Device? = null,
    val devices: List<Device> = listOf(),
    val connectionDevice: Device? = null,
    val isConnected: Boolean = false,
    val isMobile: Boolean = false,
)


fun Device.getDeviceIcon() = when(type){
    Device.Type.MOBILE -> Icons.Default.PhoneAndroid
    Device.Type.DESKTOP -> Icons.Default.LaptopChromebook
    Device.Type.UNKNOWN -> Icons.Default.DeviceUnknown
}