package com.cherrio.jibe.network

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class KtorTransport(
    private val socket: Socket
) : Transport {

    private val input = socket.openReadChannel()
    private val output = socket.openWriteChannel()

    override val incoming: Flow<ByteArray> = flow {
        while (!input.isClosedForRead) {
            val type = input.readByte()
            val length = input.readInt()
            val payload = ByteArray(length)
            input.readFully(payload)
            emit(byteArrayOf(type, *payload))
        }
    }.catch { close() }
    override var isConnected: Boolean = false

    override suspend fun send(data: ByteArray) {
        if (socket.isClosed) return
        try {
            val headerByte = data[0]
            val realData = data.copyOfRange(1, data.size)
            output.writeByte(headerByte)
            output.writeInt(realData.size)
            output.writeFully(realData)
            output.flush()
        } catch (_: Throwable) {
            close()
        }

    }

    override fun close() {
        socket.close()
    }
}
