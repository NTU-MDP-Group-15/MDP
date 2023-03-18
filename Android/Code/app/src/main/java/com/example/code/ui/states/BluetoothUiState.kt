package com.example.code.ui.states

import android.bluetooth.BluetoothDevice

//Default bluetooth state
data class BluetoothUiState(
    val discoveredDevices: Set<BluetoothDevice> = emptySet(),
    val receivedMessages: String = ""
)
