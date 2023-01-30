package com.example.code.ui.states

import android.bluetooth.BluetoothDevice

data class BluetoothUiState(
    val pairedDevices: Set<BluetoothDevice> = emptySet(),
    val discoveredDevices: Set<BluetoothDevice> = emptySet()
)
