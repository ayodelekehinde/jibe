package com.cherrio.jibe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.cherrio.jibe.network.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.streams.asSequence

class JVMPlatform(override val isMobile: Boolean = false) : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override suspend fun loadDevice(): Device {
        return withContext(Dispatchers.IO) {
            val localHost = System.getenv("COMPUTERNAME")
                ?: System.getenv("HOSTNAME")
                ?: InetAddress.getLocalHost().hostName
            NetworkInterface.networkInterfaces()
                .asSequence()
                .filter {
                    !it.isLoopback && it.isUp && it.inetAddresses.toList()
                        .any { add -> add is Inet4Address && !add.isLoopbackAddress }
                }.map { it.inetAddresses.toList().filterIsInstance<Inet4Address>() }
                .flatten()
                .firstOrNull()?.let {
                    Device(
                        name = localHost,
                        ip = it.hostAddress,
                        port = 8008,
                        type = Device.Type.DESKTOP
                    )
                }?: Device.EMPTY
        }
    }
}

actual fun getPlatform(): Platform = JVMPlatform()

@Composable
actual fun rememberQrCode(): () -> Unit = {  }

val LocalAppShutDown = staticCompositionLocalOf<() -> Unit> { error("Platform not initialized") }

@Composable
actual fun rememberShutDown(): () -> Unit {
    val shutdown = LocalAppShutDown.current
    return {
        shutdown()
    }
}