package com.example.code

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.code.ui.theme.CodeTheme
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.code.ui.navigation.NavRailItems
import com.example.code.ui.screens.arena.ArenaScreen
import com.example.code.ui.screens.bluetooth.BluetoothScreen
import com.example.code.ui.screens.debug.DebugScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Set Landscape Orientation
            val activity = (LocalContext.current as Activity)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // Navigation Rail
            val navController = rememberNavController()
            val items = listOf("Bluetooth", "Arena", "Debug")

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
                        NavHost(navController = navController, startDestination = "Bluetooth") {
                            composable(route = "Bluetooth") { BluetoothScreen() }
                            composable(route = "Arena") { ArenaScreen() }
                            composable(route = "Debug") { DebugScreen() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CodeTheme {
        Greeting("World")
    }
}