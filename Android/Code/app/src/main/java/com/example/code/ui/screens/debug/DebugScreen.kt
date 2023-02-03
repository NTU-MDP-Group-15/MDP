package com.example.code.ui.screens.debug

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.service.BluetoothService
import com.example.code.ui.viewmodels.BluetoothViewModel

@Composable
fun DebugScreen(
    viewModel: BluetoothViewModel,
    bluetoothService: BluetoothService
) {
    val bluetoothUiState by viewModel.uiState.collectAsState()
    var textMsg by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Send To Remote", fontSize = 20.sp)
            Card(
                modifier = Modifier
                    .fillMaxWidth(1f)
            ) {
                TextField(
                    value = textMsg,
                    onValueChange = { textMsg = it },
                    placeholder = { Text(text = "Enter Message Here") },
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(1f)
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
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                Button(onClick = {
                    bluetoothService.write("f".toByteArray(Charsets.UTF_8))
                    textMsg = TextFieldValue("")
                }) {
                    Text(text = "Forward")
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Button(onClick = {
                        bluetoothService.write("l".toByteArray(Charsets.UTF_8))
                        textMsg = TextFieldValue("")
                    }) {
                        Text(text = "Left")
                    }
                    Button(onClick = {
                        bluetoothService.write("r".toByteArray(Charsets.UTF_8))
                        textMsg = TextFieldValue("")
                    }) {
                        Text(text = "Right")
                    }
                }
                Button(onClick = {
                    bluetoothService.write("b".toByteArray(Charsets.UTF_8))
                    textMsg = TextFieldValue("")
                }) {
                    Text(text = "Backward")
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(text = "Received Messages", fontSize = 20.sp)
            Card(
                modifier = Modifier
                    .fillMaxHeight(1f)
                    .fillMaxWidth(1f)
            ) {
                TextField(
                    value = bluetoothUiState.receivedMessages,
                    onValueChange = {},
                    readOnly = true
                )
            }
        }
    }
}
