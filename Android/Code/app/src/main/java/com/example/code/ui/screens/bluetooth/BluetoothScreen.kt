package com.example.code.ui.screens.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.ui.viewmodels.BluetoothViewModel

@SuppressLint("MissingPermission")
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel
) {
    val bluetoothUiState by viewModel.uiState.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Column {
            Text(text = "Choose a device to pair with")
        }
        Column {
            Text(text = "My Paired Devices")
            LazyColumn(devices = bluetoothUiState.pairedDevices.toList())
            Text(text = "Available Devices")
            Button(
                onClick = { viewModel.scan() }
            ) {
                Text(
                    text = "Scan for Devices",
                )
            }
            LazyColumn(devices = bluetoothUiState.discoveredDevices.toList())
        }
    }

}

@SuppressLint("MissingPermission")
@Composable
fun LazyColumn(devices: List<BluetoothDevice>) {
    LazyColumn {
        items(devices) { device ->
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
                    Text(text = if(device.name != null) device.name else "" )
                    Text(text = device.address)
                }
            }
        }
    }
}

