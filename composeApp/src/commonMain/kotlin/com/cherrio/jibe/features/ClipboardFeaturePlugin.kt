package com.cherrio.jibe.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface ClipboardProvider {
    val changes: Flow<String>
    fun setClipboard(text: String)
}


class ClipboardFeaturePlugin(
    private val clipboardProvider: ClipboardProvider,
    private val scope: CoroutineScope,
    override val onSend: (ByteArray) -> Unit,
) : FeaturePlugin {
    override val name: String = "Clipboard"
    private var job: Job? = null
    private val headerByte = 0x01.toByte()

    override fun consume(message: ByteArray) {
        val header = message[0]
        if (header == headerByte){
            val data = message.copyOfRange(1, message.size)
            clipboardProvider.setClipboard(data.decodeToString())
        }
    }

    override fun enable() {
        job = scope.launch {
            clipboardProvider.changes.collect { change ->
                val data = byteArrayOf(headerByte, *change.encodeToByteArray())
                onSend(data)
            }
        }
    }

    override fun disable() {
        job?.cancel()
        job = null
    }
}