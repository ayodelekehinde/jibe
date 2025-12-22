package com.cherrio.jibe.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers

class KtorTransportFactory : TransportFactory {
    private val selectorManager = SelectorManager(Dispatchers.IO)

    override suspend fun createClient(host: String, port: Int): Transport {
        val socket = aSocket(selectorManager).tcp().connect(host, port)
        return KtorTransport(socket)
    }

    override suspend fun createServer(port: Int): ServerSocketTransport {
        val serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", port)
        return object : ServerSocketTransport {
            override suspend fun accept(): Transport {
                val socket = serverSocket.accept()
                return KtorTransport(socket)
            }
            override suspend fun close() = serverSocket.close()
        }
    }
}
