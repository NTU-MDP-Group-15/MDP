package com.example.code.ui.viewmodels

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import com.example.code.ui.states.BluetoothUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothViewModel : ViewModel() {
    // Bluetooth UI State
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    // Add Discovered Device
    fun addDiscoveredDevice(device: BluetoothDevice) {
        _uiState.update { currentState ->
            currentState.copy(discoveredDevices = _uiState.value.discoveredDevices.plus(device))
        }
    }

    // Clear Discovered Devices
    fun clearDiscoveredDevices() {
        _uiState.update { currentState ->
            currentState.copy(discoveredDevices = emptySet())
        }
    }

    // Add Received Message
    fun addReceivedMessage(message: String) {
        _uiState.update { currentState ->
            currentState.copy(receivedMessages = _uiState.value.receivedMessages.plus(message))
        }
    }

    fun addRobotStatusMessage(message: String) {
        _uiState.update {currentState ->
            currentState.copy(robotStatusMessages = message)
        }
    }
}

