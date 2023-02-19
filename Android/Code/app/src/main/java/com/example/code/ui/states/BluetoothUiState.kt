package com.example.code.ui.states

import android.bluetooth.BluetoothDevice

data class BluetoothUiState(
    val discoveredDevices: Set<BluetoothDevice> = emptySet(),
    val receivedMessages: String = "",
    val robotStatusMessages: String = ""
)
