package com.example.code.ui.screens.arena

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ArenaScreen() {

    var width by remember { mutableStateOf("20") }

    Row(){
        Column() {
            MyCanvas(width.toInt())
        }
        Column(
            modifier= Modifier.padding(start=80.dp)
        ) {
            Text(text = "Choose Grid Size")
            Spacer(modifier=Modifier.padding(10.dp))
            TextField(
                value=width,
                onValueChange =   {newSize ->
                    if (newSize=="") {
                        width="0"
                    }
                    else {
                        width=newSize
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )
        }
    }
}

@Composable
fun MyCanvas(width:Int) {
    var gridSize=width
    if (width>=20) {
        gridSize=20
    }
    Canvas(
        modifier = Modifier
            .size(700.dp),
    ) {
        drawRect(
            color=Color.White,
            size=size,
        )
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val left = i * 36
                val top = j * 36

                drawRect(
                    color=Color.Red,
                    topLeft= Offset(left.toFloat(),top.toFloat()),
                    size= Size(36f,36f),
                    style= Stroke(
                        width=3.dp.toPx()
                    )
                )
            }
        }
    }

}


