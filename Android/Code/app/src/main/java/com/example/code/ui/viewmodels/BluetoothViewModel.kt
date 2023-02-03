package com.example.code.ui.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.code.ui.states.BluetoothUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothViewModel : ViewModel() {
    // TODO: should only handle data, move others to BluetoothService
    // Debugging
    private val TAG: String? = "BluetoothViewModel"

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

    // Disconnect with Bluetooth Device

    // Forget Bluetooth Device
}

