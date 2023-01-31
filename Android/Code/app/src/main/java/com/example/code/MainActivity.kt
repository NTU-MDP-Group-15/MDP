package com.example.code

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.ui.navigation.NavRailItems
import com.example.code.ui.screens.arena.ArenaScreen
import com.example.code.ui.screens.bluetooth.BluetoothScreen
import com.example.code.ui.screens.debug.DebugScreen
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

        // Request for Location Information (Bluetooth)
        if (ContextCompat.checkSelfPermission(
                baseContext, Manifest.permission.ACCESS_FINE_LOCATION
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

            // Request to Turn On Bluetooth (if Off)
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
                        NavHost(
                            navController = navController,
                            startDestination = "Bluetooth",
                            modifier = Modifier.padding(50.dp)
                        ) {
                            composable(route = "Bluetooth") {
                                BluetoothScreen(viewModel = bluetoothViewModel)
                            }
                            composable(route = "Arena") { ArenaScreen() }
                            composable(route = "Debug") { DebugScreen() }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        // Remove Bluetooth Broadcast Receiver
        if (bluetoothViewModel.bluetoothAdapter.isDiscovering)
            bluetoothViewModel.bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(bluetoothViewModel.receiver)
    }
}