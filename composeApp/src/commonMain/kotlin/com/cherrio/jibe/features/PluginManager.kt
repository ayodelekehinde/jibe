package com.cherrio.jibe.features

import com.cherrio.jibe.network.ConnectionEvent
import com.cherrio.jibe.network.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PluginManager(
    private val connectionManager: ConnectionManager,
    private val coroutineScope: CoroutineScope,
) {
    init {
        initialize()
    }
    private val plugins = mutableMapOf<String, FeaturePlugin>()

    fun registerPlugin(plugin: FeaturePlugin) {
        plugins[plugin.name] = plugin
    }

    fun unregisterPlugin(plugin: FeaturePlugin) {
        plugins.remove(plugin.name)
    }

    fun sendMessage(message: ByteArray) {
        connectionManager.send(message)
    }
    fun startPlugins(){
        plugins.values.forEach { it.enable() }
    }
    fun stopPlugins(){
        plugins.values.forEach { it.disable() }
    }

    private fun initialize() {
        coroutineScope.launch {
            connectionManager.events.collect { event ->
                when (event.event) {
                    ConnectionEvent.ReceivedMessage -> {
                        plugins.values.forEach { plugin -> plugin.consume(event.payload) }
                    }
                    else -> Unit
                }
            }
        }
    }
}