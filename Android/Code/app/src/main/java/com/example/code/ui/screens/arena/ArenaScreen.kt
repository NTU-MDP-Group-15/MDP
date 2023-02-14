package com.example.code.ui.screens.arena

import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.code.ui.states.ArenaUiState
import com.example.code.ui.states.Obstacle
import com.example.code.ui.viewmodels.ArenaViewModel
import com.example.code.ui.viewmodels.BluetoothViewModel

val spacerDP = 10.dp

@Composable
fun ArenaScreen(
    viewModel: ArenaViewModel
) {
    val arenaUiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(1f)
        ) {
            StatusDisplay(arenaUiState = arenaUiState)
            ArenaGrid(arenaUiState = arenaUiState, viewModel = viewModel)
        }

        // Config
        Column(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .padding(25.dp, 0.dp, 0.dp, 0.dp)
        ) {
            TaskModeInput(viewModel = viewModel, arenaUiState = arenaUiState)
            GridSizeInput(viewModel = viewModel, arenaUiState = arenaUiState)
            ObstacleInput(viewModel = viewModel)
        }

        //        Column(
        //            modifier = Modifier.padding(start = 80.dp)
        //        ) {
        //            Button(
        //                onClick = {
        //                    if (x == "" || y == "") {
        //                        obstacles = obstacles.union(setOf(Pair(0, 0)))
        //                        obstacles = obstacles.minus(setOf(Pair(0, 0)))
        //                    } else {
        //                        obstacles = obstacles.union(setOf(Pair(x.toInt(), y.toInt())))
        //                    }
        //                },
        //                content = {
        //                    Text("Add obstacle")
        //                }
        //            )
        //            Button(
        //                onClick = {
        //                    if (x == "" || y == "") {
        //                        obstacles = obstacles.union(setOf(Pair(0, 0)))
        //                        obstacles = obstacles.minus(setOf(Pair(0, 0)))
        //                    } else {
        //                        obstacles = obstacles.minus(setOf(Pair(x.toInt(), y.toInt())))
        //                    }
        //                },
        //                content = {
        //                    Text("Remove obstacle")
        //                }
        //            )
        //        }
    }
}

@Composable
fun StatusDisplay(arenaUiState: ArenaUiState) {
    Row(modifier = Modifier.fillMaxWidth(1f)) {
        Text(
            text = "${arenaUiState.taskMode} | " +
                    "${arenaUiState.gridWidth}, ${arenaUiState.gridHeight} | " +
                    ""
        )
    }
}

@Composable
fun ArenaGrid(arenaUiState: ArenaUiState, viewModel: ArenaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f),
//        contentAlignment = Alignment.Center
    ) {
        Row {
            for (i in 0 until arenaUiState.gridWidth) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "$i")
                    for (j in 0 until arenaUiState.gridHeight) {
                        if (i == 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "$j")
                                GridBox(
                                    viewModel = viewModel,
                                    xCoord = i,
                                    yCoord = j,
                                    arenaUiState = arenaUiState
                                )
                            }
                        } else
                            GridBox(
                                viewModel = viewModel,
                                xCoord = i,
                                yCoord = j,
                                arenaUiState = arenaUiState
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun GridBox(
    viewModel: ArenaViewModel,
    arenaUiState: ArenaUiState,
    xCoord: Int,
    yCoord: Int
) {
    DropItem<HashMap<String, Any>>(
        modifier = Modifier.size(30.dp) // TODO: Change dynamic size?
    ) { isInBound, obsMap ->
        val obstacles =
            arenaUiState.obstacles.filter { obs -> (obs.xPos == xCoord) && (obs.yPos == yCoord) }
        if (obstacles.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .background(Color.Black)
            ) {
                // drag itme
            }
        } else {
            if (obsMap != null) {
                LaunchedEffect(key1 = obsMap) {
                    obsMap["xPos"] = xCoord
                    obsMap["yPos"] = yCoord
                    viewModel.addObstacle(obsMap)
                }
            }
            if (isInBound) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black)
                        .background(Color.Gray.copy(0.5f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black),
                )
            }
        }
    }
}

@Composable
fun TaskModeInput(viewModel: ArenaViewModel, arenaUiState: ArenaUiState) {
    val imgReg = "Image Recognition"
    val fastCar = "Fastest Car"

    val selectedTask = remember { mutableStateOf(imgReg) }
    val taskModes = listOf(imgReg, fastCar)

    Row(
        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, spacerDP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Task Mode:", fontWeight = FontWeight.Bold)
        taskModes.forEach { task ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTask.value == task,
                    onClick = {
                        selectedTask.value = task
                        viewModel.setTaskMode(task)
                    },
                    enabled = true,
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Blue)
                )
                Text(text = task)
            }
        }
    }
}

@Composable
fun GridSizeInput(viewModel: ArenaViewModel, arenaUiState: ArenaUiState) {
    var width by remember { mutableStateOf("20") }
    var height by remember { mutableStateOf("20") }

    Row(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(0.dp, 0.dp, 0.dp, spacerDP),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Enter Grid Size", fontWeight = FontWeight.Bold)
        Text(text = "X: ")
        TextField(
            modifier = Modifier.width(70.dp),
            value = width,
            onValueChange = { newX: String ->
                if (newX != "") {
                    val w = newX.toInt()
                    if (w in 0..20) {
                        viewModel.setGridSize(
                            width = w,
                            height = arenaUiState.gridHeight
                        )
                    }
                }
                if (newX.length <= 2) {
                    width = newX
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            )
        )
        Text(text = "Y: ")
        TextField(
            modifier = Modifier.width(70.dp),
            value = height,
            onValueChange = { newY: String ->
                if (newY != "") {
                    val h = newY.toInt()
                    if (h in 0..20) {
                        viewModel.setGridSize(
                            width = arenaUiState.gridWidth,
                            height = h
                        )
                    }
                }
                if (newY.length <= 2) {
                    height = newY
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            )
        )
    }
}

@Composable
fun ObstacleInput(viewModel: ArenaViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Drag Obstacle onto Arena: ")
        DragTarget(
            dataToDrop = hashMapOf("id" to 1),
            viewModel = viewModel
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .shadow(5.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Next ID",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        DropItem<HashMap<String, Any>>(
            modifier = Modifier.size(55.dp) // TODO: Change dynamic size?
        ) { isInBound, obsMap ->
            if (obsMap != null) {
                LaunchedEffect(key1 = obsMap) {

                }
            }
            if (isInBound) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black)
                        .background(Color.Gray.copy(0.5f))
                ) {
                    Text(text = "Delete")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black)
                ) {
                    Text(text = "Drop to Delete")
                }
            }
        }
    }
}