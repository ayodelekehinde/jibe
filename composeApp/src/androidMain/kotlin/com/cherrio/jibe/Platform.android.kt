package com.cherrio.jibe

import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.cherrio.jibe.network.Device
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class AndroidPlatform(override val isMobile: Boolean = true) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override suspend fun loadDevice(): Device {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
        return withContext(Dispatchers.IO) {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .filter {
                    !it.isLoopback && it.isUp && it.inetAddresses.toList()
                        .any { add -> add is Inet4Address && !add.isLoopbackAddress }
                }.map { it.inetAddresses.toList().filterIsInstance<Inet4Address>() }
                .flatten()
                .firstOrNull()?.let {
                    Device(
                        name = deviceName,
                        ip = it.hostAddress.orEmpty(),
                        port = 8008,
                        type = Device.Type.MOBILE
                    )
                }?: Device.EMPTY
        }
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun rememberQrCode(): () -> Unit {
    val context = LocalContext.current
    val openService: (String) -> Unit = {
        val intent = Intent(context, ConnectionForegroundService::class.java).apply {
            action = "ACTION_CONNECT"
            putExtra("host", it)
            putExtra("port", 8008)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRError -> println("QR Error: ${result.exception.message}")
            QRResult.QRMissingPermission -> println("No permission")
            is QRResult.QRSuccess -> openService(result.content.rawValue.orEmpty())
            QRResult.QRUserCanceled -> println("User canceled")
        }
    }
    return { scanQrCodeLauncher.launch(null) }
}

@Composable
actual fun rememberShutDown(): () -> Unit{
    val activity = LocalActivity.current
    return remember(activity) {
        {
            activity?.stopService(
                Intent(activity, ConnectionForegroundService::class.java)
            )
            activity?.finish()
        }
    }
}