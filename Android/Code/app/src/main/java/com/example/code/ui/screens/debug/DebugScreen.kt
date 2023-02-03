package com.example.code.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.service.BluetoothService
import com.example.code.ui.viewmodels.BluetoothViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DebugScreen(
    viewModel: BluetoothViewModel,
    bluetoothService: BluetoothService
    ) {
    val bluetoothUiState by viewModel.uiState.collectAsState()
    var textMsg by remember { mutableStateOf(TextFieldValue("")) }

    Column() {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(10.dp)
        ) {
            Column {
                Text(text = "Send To Remote", fontSize = 20.sp)
                Card(
                    modifier = Modifier
                        .fillMaxHeight(0.2F)
                        .border(width = 2.dp, color = Color.Black)
                ) {
                    TextField(
                        value = textMsg,
                        onValueChange = {
                            textMsg = it
                        },
                        placeholder = { Text(text = "Enter Message Here") },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.273f)
                ) {
                    Button(onClick = {
                        bluetoothService.write(textMsg.text.toByteArray(Charsets.UTF_8))
                        textMsg = TextFieldValue("")
                    }) {
                        Text(text = "Send")
                    }
                    Button(onClick = { textMsg = TextFieldValue("") }) {
                        Text(text = "Clear")
                    }
                }
            }
            Column {
                Text(text = "Received Messages", fontSize = 20.sp)
                Card(
                    modifier = Modifier
                        .fillMaxHeight(0.5F)
                        .fillMaxWidth(0.5f)
                        .border(width = 2.dp, color = Color.Black)
                ) {
                    TextField(value = bluetoothUiState.receivedMessages, onValueChange = {}, readOnly = true)
                }
            }
        }
    }
}

