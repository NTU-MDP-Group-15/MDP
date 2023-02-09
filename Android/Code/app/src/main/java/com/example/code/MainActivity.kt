package com.example.code

//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.ActivityInfo
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.code.service.BluetoothService
//import com.example.code.ui.navigation.NavRailItems
//import com.example.code.ui.screens.arena.ArenaScreen
//import com.example.code.ui.screens.bluetooth.BluetoothScreen
//import com.example.code.ui.screens.debug.DebugScreen
//import com.example.code.ui.theme.CodeTheme
//import com.example.code.ui.viewmodels.BluetoothViewModel
//
//class MainActivity : ComponentActivity() {
//    // Bluetooth View Model
//    private val bluetoothViewModel = BluetoothViewModel()
//
//    // Initialise Bluetooth Service
//    private val bluetoothService = BluetoothService(bluetoothViewModel)
//
//    // Bluetooth Permissions
//    private val activityResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == RESULT_OK) {
//            Log.d("Bluetooth", ":request permission result ok")
//        } else {
//            Log.d("Bluetooth", ":request permission result canceled / denied")
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Request for Location Information (Bluetooth Permission)
//        if (ContextCompat.checkSelfPermission(
//                baseContext, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
//            )
//        }
//
//        // Request to Turn On Bluetooth (if Off)
//        if (!bluetoothService.mAdapter.isEnabled) {
//            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            activityResultLauncher.launch(enableBluetoothIntent)
//        }
//
//        // Request to make Device Discoverable for 300s
////        val discoverableIntent: Intent =
////            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
////                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
////            }
////        activityResultLauncher.launch(discoverableIntent)
//
//        // Register for broadcasts when a device is discovered
//        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        val startFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//        val endFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        registerReceiver(bluetoothService.receiver, foundFilter)
//        registerReceiver(bluetoothService.receiver, startFilter)
//        registerReceiver(bluetoothService.receiver, endFilter)
//
//        setContent {
//            // Set Landscape Orientation
//            val activity = (LocalContext.current as Activity)
//            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//            // Navigation Rail
//            val navController = rememberNavController()
//            val items = listOf("Bluetooth", "Arena", "Debug")
//
//            // App Entry Point
//            CodeTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colors.background
//                ) {
//                    Row {
//                        // Nav Rail
//                        NavRailItems(navController, items)
//                        // Screens
//                        NavHost(
//                            navController = navController,
//                            startDestination = "Arena",
//                            modifier = Modifier.padding(50.dp)
//                        ) {
//                            composable(route = "Bluetooth") {
//                                BluetoothScreen(
//                                    viewModel = bluetoothViewModel,
//                                    bluetoothService = bluetoothService
//                                )
//                            }
//                            composable(route = "Arena") { ArenaScreen()}
//                            composable(route = "Debug") {
//                                DebugScreen(
//                                    viewModel = bluetoothViewModel,
//                                    bluetoothService = bluetoothService
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onDestroy() {
//        super.onDestroy()
//
//        // Stop Bluetooth Service
//        bluetoothService.stop()
//
//        // Remove Bluetooth Broadcast Receiver
//        unregisterReceiver(bluetoothService.receiver)
//    }
//}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.code.ui.screens.arena.DragableScreen
import com.example.code.ui.screens.arena.MainScreen
import com.example.code.ui.theme.CodeTheme
import com.example.code.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainViewModel = MainViewModel()
        setContent {
            CodeTheme {
                DragableScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.8f))
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}