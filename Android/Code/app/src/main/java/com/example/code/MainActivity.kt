package com.example.code

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.code.ui.navigation.NavRailItems
import com.example.code.ui.theme.CodeTheme
import com.example.code.ui.viewmodels.BluetoothViewModel

class MainActivity : ComponentActivity() {
    // Bluetooth View Model
    private val bluetoothViewModel = BluetoothViewModel()

    // Bluetooth Permissions
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i("Bluetooth", ":request permission result ok")
        } else {
            Log.i("Bluetooth", ":request permission result canceled / denied")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered.
        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val startFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val endFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothViewModel.receiver, foundFilter)
        registerReceiver(bluetoothViewModel.receiver, startFilter)
        registerReceiver(bluetoothViewModel.receiver, endFilter)

        if (ContextCompat.checkSelfPermission(
                baseContext, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        setContent {
            // Set Landscape Orientation
            val activity = (LocalContext.current as Activity)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // Navigation Rail
            val navController = rememberNavController()
            val items = listOf("Bluetooth", "Arena", "Debug")

            // Request to Turn On Bluetooth
            if (!bluetoothViewModel.bluetoothAdapter.isEnabled) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityResultLauncher.launch(enableBluetoothIntent)
            }

            // App Entry Point
            CodeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row {
                        // Nav Rail
                        NavRailItems(navController, items)
                        // Screens
//                        NavHost(
//                            navController = navController,
//                            startDestination = "Bluetooth",
//                            modifier = Modifier.padding(50.dp)
//                        ) {
//                            composable(route = "Bluetooth") {
//                                BluetoothScreen(
//                                    pairedDevices = pairedDevices,
//                                    discoveredDevices = discoveredDevices,
//                                    scan = { scan() }
//                                )
//                            }
//                            composable(route = "Arena") { ArenaScreen() }
//                            composable(route = "Debug") { DebugScreen() }
//                        }
                        MainScreen(
                            bluetoothViewModel = BluetoothViewModel()
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        if (bluetoothViewModel.bluetoothAdapter.isDiscovering)
            bluetoothViewModel.bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(bluetoothViewModel.receiver)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    bluetoothViewModel: BluetoothViewModel
) {
    val bluetoothUiState by bluetoothViewModel.uiState.collectAsState()

    Column {
        Text(text = "My Paired Devices")
        bluetoothUiState.pairedDevices.forEach { device ->
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
            onClick = { bluetoothViewModel.scan() }
        ) {
            Text(
                text = "Scan",
            )
        }
        bluetoothUiState.discoveredDevices.forEach { device ->
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