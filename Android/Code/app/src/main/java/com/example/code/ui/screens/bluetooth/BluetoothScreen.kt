package com.example.code.ui.screens.bluetooth

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BluetoothScreen() {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Column {
            Text(text = "Choose a device to pair with")
        }
        Column {
            Text(text = "Available Bluetooth Devices")
        }
    }
}

