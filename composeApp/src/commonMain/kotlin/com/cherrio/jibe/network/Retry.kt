package com.cherrio.jibe.network

class Retry internal constructor(
    val onConnected: () -> Unit,
    val onError: suspend (deviceId: String) -> Unit,
)