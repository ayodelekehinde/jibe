package com.cherrio.jibe.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

interface Transport {
    val incoming: Flow<ByteArray>
    var isConnected: Boolean
    suspend fun send(data: ByteArray)
    fun close()
}

interface TransportFactory {
    suspend fun createClient(host: String, port: Int): Transport
    suspend fun createServer(port: Int): ServerSocketTransport
}
interface ServerSocketTransport {
    suspend fun accept(): Transport
    suspend fun close()
}
data class ConnectionState(
    val event: ConnectionEvent = ConnectionEvent.None,
    val device: Device = Device.EMPTY,
    val isConnected: Boolean = false,
    val payload: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionState

        if (isConnected != other.isConnected) return false
        if (event != other.event) return false
        if (device != other.device) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isConnected.hashCode()
        result = 31 * result + event.hashCode()
        result = 31 * result + device.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

enum class ConnectionEvent {
    None, Connected, Disconnected, RetryFailed, ReceivedMessage
}

suspend fun Transport.awaitHandshake(me: Device): Device{
    val data = me.toString().encodeToByteArray()
    val fullData = byteArrayOf(0x03, *data)
    val device = readHandshake()
    send(fullData)
    return device
}

suspend fun Transport.sendHandshake(me: Device): Device{
    val data = me.toString().encodeToByteArray()
    val fullData = byteArrayOf(0x03, *data)
    send(fullData)
    return readHandshake()
}

private suspend fun Transport.readHandshake(): Device {
    return withTimeout(5.seconds) {
        val deviceData = incoming.first()
        val payload = deviceData.copyOfRange(1, deviceData.size).decodeToString()
        val (name, ip, port, type) = payload.trim().split(",")
        Device(
            name = name,
            ip = ip,
            port = port.toInt(),
            type = Device.Type.toType(type),
        )
    }
}