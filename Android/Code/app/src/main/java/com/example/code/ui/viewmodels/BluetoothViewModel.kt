package com.example.code.ui.viewmodels

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.code.ui.states.BluetoothUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class BluetoothViewModel : ViewModel() {
    // Bluetooth Adapter
    val bluetoothAdapter: BluetoothAdapter = getDefaultAdapter()

    // Bluetooth UI State
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    // Init Bluetooth UI State Values
    init {
        _uiState.value = BluetoothUiState(pairedDevices = bluetoothAdapter.bondedDevices)
    }

    @SuppressLint("MissingPermission")
    var pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
    var discoveredDevices: Set<BluetoothDevice> = emptySet()
    var devices: Set<BluetoothDevice> by mutableStateOf(emptySet())

    // Register BroadcastReceiver
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        val updated = discoveredDevices.plus(device)
                        discoveredDevices = updated
                        _uiState.update { currentState ->
                            currentState.copy(discoveredDevices = updated)
                        }
                    }
                    Log.i("Bluetooth", "onReceive: Device Found")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i("Bluetooth", "onReceive: Started Discovery")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i("Bluetooth", "onReceive: Finished Discovery")
                }
            }
        }
    }

    // Scan for Bluetooth Devices
    @SuppressLint("MissingPermission")
    fun scan() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            bluetoothAdapter.startDiscovery()
        } else {
            bluetoothAdapter.startDiscovery()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothAdapter.cancelDiscovery()
        }, 10000L)
    }
//    @SuppressLint("MissingPermission")
//    fun scan(): Set<BluetoothDevice> {
//        if (bluetoothAdapter.isDiscovering) {
//            bluetoothAdapter.cancelDiscovery()
//            bluetoothAdapter.startDiscovery()
//        } else {
//            bluetoothAdapter.startDiscovery()
//        }
//        Handler(Looper.getMainLooper()).postDelayed({
//            bluetoothAdapter.cancelDiscovery()
//        }, 10000L)
//        Log.i("Bluetooth", "discoveredDevices: $discoveredDevices")
//        Log.i("Bluetooth", "devices: $devices")
//        return discoveredDevices
//    }

    // Pair with Bluetooth Device

    // Disconnect with Bluetooth Device

    // Forget Bluetooth Device

}