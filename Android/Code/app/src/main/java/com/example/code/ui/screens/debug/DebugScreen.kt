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
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun DebugScreen() {
    Column() {
        Row (horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(1f))
        {
            Text(text = "Connected Devices: ", fontSize=50.sp)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(10.dp)
        ) {
            Column {
                Text(text = "Send To Remote", fontSize = 30.sp)
                Card(modifier = Modifier
                    .fillMaxHeight(0.2F)
                    .border(width = 2.dp, color = Color.Black)) {
                    LabelAndPlaceHolder()
                }
                Row(horizontalArrangement=Arrangement.SpaceBetween,
                modifier=Modifier.fillMaxWidth(0.273f)) {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text="Send")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(text="Clear")
                    }
                }
                Button(onClick = { /*TODO*/ }) {
                    
                }
            }
            Column {
                Text(text = "Command Log", fontSize = 30.sp)
                Card(modifier = Modifier
                    .fillMaxHeight(0.5F)
                    .fillMaxWidth(0.5f)
                    .border(width = 2.dp, color = Color.Black)) {
                }
            }
        }
    }
}

@Composable
fun LabelAndPlaceHolder() {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        value = text,
        onValueChange = {
            text = it
        },
        label = { Text(text = "Your Messages") },
        placeholder = { Text(text = "Enter Message Here") },
    )
}


