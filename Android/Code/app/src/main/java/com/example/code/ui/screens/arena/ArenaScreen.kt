package com.example.code.ui.screens.arena

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ArenaScreen() {
    var width by remember { mutableStateOf("20") }
    var obstacles by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var x by remember { mutableStateOf("")}
    var y by remember { mutableStateOf("")}

    Row(){
        Column() {
            MyCanvas(width.toInt(), obstacles, Pair(0,0), "N")
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
            Spacer(modifier=Modifier.padding(10.dp))
            Text("Add new obstacles")
            Spacer(modifier=Modifier.padding(10.dp))

            TextField(
                value=x,
                onValueChange =   {newX ->
                    x=newX
                },
                placeholder = {
                    Text("X-coordinate")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )
            Spacer(modifier=Modifier.padding(10.dp))
            TextField(
                value=y,
                onValueChange =   {newY ->
                    y=newY
                },
                placeholder = {
                    Text("Y-coordinate")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )

            Button(
                onClick = {
                    if (x=="" || y=="") {
                        obstacles = obstacles.union(setOf(Pair(0, 0)))
                        obstacles = obstacles.minus(setOf(Pair(0, 0)))
                    }
                    else {
                        obstacles = obstacles.union(setOf(Pair(x.toInt(), y.toInt())))
                    }
                },
                content = {
                    Text("Add obstacle")
                }
            )
            Button(

                onClick = {
                    if (x=="" || y=="") {
                        obstacles = obstacles.union(setOf(Pair(0, 0)))
                        obstacles = obstacles.minus(setOf(Pair(0, 0)))
                    }
                    else {
                        obstacles = obstacles.minus(setOf(Pair(x.toInt(), y.toInt())))
                    }
                },
                content = {
                    Text("Remove obstacle")
                }
            )
        }
    }
}

@Composable
fun MyCanvas(width: Int, obstacles: Set<Pair<Int, Int>>, robotLocation: Pair<Int, Int>, robotOrientation: String) {
    var gridSize = width
    if (width >= 20) {
        gridSize = 20
    }
    Canvas(
        modifier = Modifier.size(400.dp)
    ) {
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val left = i * 30
                val top = (gridSize-j-1) * 30

                if (Pair(i, j) in obstacles) {
                    drawRect(
                        color=Color.Green,
                        topLeft= Offset(left.toFloat(), top.toFloat()),
                        size=Size(30f, 30f),
                    )
                } else {
                    drawRect(
                        color=Color.Red,
                        topLeft= Offset(left.toFloat(), top.toFloat()),
                        size=Size(30f, 30f),
                        style= Stroke(
                            width=3.dp.toPx()
                        )
                    )
                }
                if (Pair(i, j) == robotLocation) {
                    if (robotOrientation=="N") {
                        drawPath(
                            path = Path().apply {
                                moveTo(left + 15f, top.toFloat())
                                lineTo(left + 5f, top + 30f)
                                lineTo(left + 25f, top + 30f)
                                close()
                            },
                            color = Color.Blue,
                        )
                    }
                    if (robotOrientation=="S") {
                        drawPath(
                            path = Path().apply {
                                moveTo(left + 15f, top+30f)
                                lineTo(left + 5f, top.toFloat())
                                lineTo(left + 25f, top.toFloat())
                                close()
                            },
                            color = Color.Blue,
                        )
                    }
                    if (robotOrientation=="E") {
                        drawPath(
                            path = Path().apply {
                                moveTo(left + 30f, top + 15f)
                                lineTo(left.toFloat(), top + 5f)
                                lineTo(left.toFloat(), top + 25f)
                                close()
                            },
                            color = Color.Blue,
                        )
                    }
                    if (robotOrientation=="W") {
                        drawPath(
                            path = Path().apply {
                                moveTo(left.toFloat(), top + 15f)
                                lineTo(left + 30f, top + 5f)
                                lineTo(left + 30f, top + 25f)
                                close()
                            },
                            color = Color.Blue,
                        )
                    }
                }
            }
        }
    }
}














