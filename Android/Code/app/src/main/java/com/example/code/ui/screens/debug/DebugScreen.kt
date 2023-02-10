package com.example.code.ui.screens.debug

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.R
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
            Spacer(modifier = Modifier.height(30.dp))
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
            RobotMovementButtons(bluetoothService)
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(text = "Received Messages", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(30.dp))
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

@Composable
fun RobotMovementButtons(btService: BluetoothService) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(1f)
    ) {
        MovementButton(
            btService = btService,
            msg = "f",
            resID = R.drawable.arrow_up,
            description = "Up"
        )
        Row {
            MovementButton(
                btService = btService,
                msg = "l",
                resID = R.drawable.arrow_left,
                description = "Left"
            )
            Spacer(modifier = Modifier.width(60.dp))
            MovementButton(
                btService = btService,
                msg = "r",
                resID = R.drawable.arrow_right,
                description = "Right"
            )
        }
        MovementButton(
            btService = btService,
            msg = "b",
            resID = R.drawable.arrow_down,
            description = "Down"
        )
    }
}

@Composable
fun MovementButton(btService: BluetoothService, msg: String, resID: Int, description: String) {
    Button(
        onClick = {
            btService.write(msg.toByteArray(Charsets.UTF_8))
        },
        modifier = Modifier
            .height(60.dp)
            .width(60.dp)
    ) {
        Image(
            painter = painterResource(id = resID),
            contentDescription = description
        )
    }
}