package com.cherrio.jibe.di

import com.cherrio.jibe.Platform
import com.cherrio.jibe.features.ClipboardFeaturePlugin
import com.cherrio.jibe.features.ClipboardProvider
import com.cherrio.jibe.features.PluginManager
import com.cherrio.jibe.getPlatform
import com.cherrio.jibe.network.ConnectionManager
import com.cherrio.jibe.network.KtorTransportFactory
import com.cherrio.jibe.network.TransportFactory
import kotlinx.coroutines.*

object Di {
    @PublishedApi
    internal val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @PublishedApi
    internal val transportFactory = KtorTransportFactory()
    @PublishedApi
    internal val platform = getPlatform()
    @PublishedApi
    internal val connectionManager = ConnectionManager(
        transportFactory = transportFactory,
        platform = platform,
        scope = coroutineScope,
    )
    @PublishedApi
    internal val pluginManager = PluginManager(connectionManager, coroutineScope)

    fun close() {
        connectionManager.stop()
    }
    fun injectClipboardProvider(clipboardProvider: ClipboardProvider) {
        val clipboardFeaturePlugin = ClipboardFeaturePlugin(clipboardProvider, coroutineScope, pluginManager::sendMessage)
        pluginManager.registerPlugin(clipboardFeaturePlugin)
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutineScope.launch(block = block)
    }

    inline fun <reified T> get(): T {
        return when (T::class) {
            TransportFactory::class -> transportFactory
            Platform::class -> platform
            ConnectionManager::class -> connectionManager
            PluginManager::class -> pluginManager
            else -> error("No provision for ${T::class}")
        } as T
    }
}