package com.cherrio.jibe.network

import com.cherrio.jibe.Platform
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConnectionManager(
    private val transportFactory: TransportFactory,
    private val platform: Platform,
    scope: CoroutineScope,
) : CoroutineScope by scope {

    private val _events = MutableStateFlow(ConnectionState())
    val events = _events
        .asStateFlow()

    private val activeConnections = ConcurrentMap<String, Transport>()

    fun startServer(port: Int) = launch {
        val server = transportFactory.createServer(port)
        println("Server listening on port $port")
        while (isActive) {
            val socket = server.accept()
            val device = socket.awaitHandshake(platform.loadDevice())
            openStream(device = device,socket)
        }
    }

    fun connectTo(host: String, port: Int) = launch {
        retryOnClose {
            try {
                val transport = transportFactory.createClient(host, port)
                val device = transport.sendHandshake(platform.loadDevice())
                onConnected()
                openStream(device = device, transport = transport)
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                else {
                    onError(host)
                }
            }
        }
    }

    fun send(bytes: ByteArray) {
        launch {
            activeConnections.values.forEach {
                it.send(bytes)
            }
        }
    }

    fun stop() {
        activeConnections.values.forEach { runCatching { it.close() } }
        activeConnections.clear()
    }

    private suspend fun openStream(device: Device, transport: Transport) {
        val id = device.ip
        activeConnections[id] = transport
        transport.isConnected = true
        _events.update { it.copy(device = device, isConnected = true, event = ConnectionEvent.Connected) }
        openMessagingStream(id, transport)
    }

    private suspend fun openMessagingStream(id: String, transport: Transport) {
        transport.incoming.collect {
            if (it.isNotEmpty()) {
                _events.update { state -> state.copy(payload = it, event = ConnectionEvent.ReceivedMessage) }
            }
        }
        _events.update { state -> state.copy(event = ConnectionEvent.Disconnected, isConnected = false) }
        transport.isConnected = false
        activeConnections.remove(id)
    }
    private suspend inline fun retryOnClose(
        times: Int = 10,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        backoffMultiplier: Double = 2.0,
        block: suspend Retry.() -> Unit // if it returns, device has disconnected
    ) {
        var attempt = 0
        var delay = initialDelay
        val retry = Retry(
            onConnected = { attempt = -1; delay = initialDelay },
            onError = { _ ->
                if (times > 0 && attempt + 1 >= times) {
                    _events.update { state -> state.copy(event = ConnectionEvent.RetryFailed) }
                }
            }
        )
        while (attempt < times || times == 0) {
            println("Attempting $attempt, with delay $delay ms")
            retry.block()
            attempt++
            delay = (delay * backoffMultiplier).toLong().coerceIn(initialDelay, maxDelay)
            delay(delay)

        }
    }

}
