package com.example.code.ui.screens.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@SuppressLint("MissingPermission")
@Composable
fun BluetoothScreen(
    pairedDevices: Set<BluetoothDevice>,
    discoveredDevices: Set<BluetoothDevice>,
    scan: () -> Set<BluetoothDevice>
) {
    // Discoverable Bluetooth Device List
    var devices: Set<BluetoothDevice> by remember { mutableStateOf(emptySet()) }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Column {
            Text(text = "Choose a device to pair with")
        }
        Column {
            Text(text = "My Paired Devices")
            pairedDevices.forEach { device ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    elevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = device.name)
                        Text(text = device.address)
                    }
                }
            }
            Text(text = "Available Devices")
            Button(
                onClick = { devices = scan() }
            ) {
                Text(
                    text = "Scan",
                )
            }
            devices.forEach { device ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    elevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = device.name)
                        Text(text = device.address)
                    }
                }
            }
        }
    }

}

