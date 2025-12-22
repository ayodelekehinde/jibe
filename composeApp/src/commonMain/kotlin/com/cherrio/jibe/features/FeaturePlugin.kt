package com.cherrio.jibe.features

interface FeaturePlugin {
    val name: String
    val onSend: (ByteArray) -> Unit
    fun consume(message: ByteArray)
    fun enable()
    fun disable()
}