package com.cherrio.jibe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrio.jibe.Platform
import com.cherrio.jibe.di.Di
import com.cherrio.jibe.network.ConnectionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val manager: ConnectionManager = Di.get(),
    private val platform: Platform = Di.get(),
): ViewModel() {
    private val initialState = HomeState(isMobile = platform.isMobile)
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<HomeState> = _state
        .onStart { initialize() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialState
        )
    private fun initialize() {
        println("Initialized")
        if (!platform.isMobile){
            viewModelScope.launch {
                val device = platform.loadDevice()
                _state.update { it.copy(connectionDevice = device) }
                manager.startServer(device.port)
            }
        }
        manager.events
            .onEach { connectionState ->
                _state.update {
                    it.copy(
                        isConnected = connectionState.isConnected,
                        device = if (!connectionState.device.isEmpty) connectionState.device else null
                    )
                }
            }.produceIn(viewModelScope)

    }
}