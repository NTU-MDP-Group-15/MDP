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
import androidx.compose.ui.unit.sp
import com.example.code.service.BluetoothService
import com.example.code.ui.viewmodels.BluetoothViewModel

@SuppressLint("MissingPermission")
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel,
    bluetoothService: BluetoothService
) {
    val bluetoothUiState by viewModel.uiState.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .fillMaxHeight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { bluetoothService.start() }
            ) {
                Text(text = "Open Socket for Connection")
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { bluetoothService.stop() }
            ) {
                Text(text = "Stop Bluetooth Service")
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { bluetoothService.scan() }
            ) {
                Text(text = "Scan for Devices")
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(text = "Available Devices", fontSize = 20.sp)
            LazyColumn(
                devices = bluetoothUiState.discoveredDevices.toList(),
                service = bluetoothService
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LazyColumn(
    devices: List<BluetoothDevice>,
    service: BluetoothService
) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
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
                    Text(text = if (device.name != null) device.name else "")
                    Text(text = device.address)
                    Button(
                        onClick = { service.connect(device) }
                    ) {
                        Text(text = "Connect")
                    }
                }
            }
        }
    }
}