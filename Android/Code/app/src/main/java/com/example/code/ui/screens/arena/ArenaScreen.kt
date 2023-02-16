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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.unit.sp
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
                .fillMaxWidth(0.55f)
                .fillMaxHeight(1f)
        ) {
            ArenaGrid(arenaUiState = arenaUiState, viewModel = viewModel)
        }

        // Config
        Column(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .padding(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            StatusDisplay(arenaUiState = arenaUiState)
            Spacer(modifier = Modifier.padding(bottom = spacerDP))
            TaskModeInput(viewModel = viewModel, arenaUiState = arenaUiState)
            GridSizeInput(viewModel = viewModel, arenaUiState = arenaUiState)
            SetRobotOrientation(viewModel = viewModel, arenaUiState = arenaUiState)
            ObstacleInput(viewModel = viewModel, arenaUiState = arenaUiState)
            Spacer(modifier = Modifier.padding(bottom = spacerDP))
            ClearObstacles(viewModel = viewModel)

            Button(onClick = { println(arenaUiState.obstacles) }) {
                Text(text = "Test")
            }
            Button(onClick = { viewModel.removeObstacle(1) }) {
                Text(text = "Remove")
            }
        }
    }
}

@Composable
fun StatusDisplay(arenaUiState: ArenaUiState) {
    Row(modifier = Modifier.fillMaxWidth(1f)) {
        Text(
            text = "${arenaUiState.taskMode} | " +
                    "${arenaUiState.gridWidth}, ${arenaUiState.gridHeight} | " +
                    "",
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ArenaGrid(arenaUiState: ArenaUiState, viewModel: ArenaViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f)
    ) {
        Row(
        ) {
            for (i in 0 until arenaUiState.gridWidth) {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    for (j in arenaUiState.gridHeight - 1 downTo 0) {
                        if (i == 0) {
                            Row() {
                                Text(
                                    text = "$j ",
                                    fontSize = 13.sp
                                )
                                GridBox(
                                    viewModel = viewModel,
                                    xCoord = i,
                                    yCoord = j,
                                    arenaUiState = arenaUiState
                                )
                            }
                        } else {
                            GridBox(
                                viewModel = viewModel,
                                xCoord = i,
                                yCoord = j,
                                arenaUiState = arenaUiState
                            )
                        }
                    }
                    Text(
                        text = "$i",
                        fontSize = 13.sp
                    )
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
    yCoord: Int,
) {
    DropItem<Obstacle>(
        modifier = Modifier.size(20.dp)
    ) { isInBound, obstacle ->
        val self =
            arenaUiState.obstacles.filter { obs -> (obs.xPos == xCoord) && (obs.yPos == yCoord) }
        if (self.isNotEmpty()) {
            drawObstacle(obstacle = self.first(), viewModel = viewModel)
        } else {
            if (obstacle != null) {
                LaunchedEffect(key1 = obstacle) {
                    if (obstacle.xPos != null) {
                        println("$xCoord, $yCoord: $obstacle")
                        viewModel.removeObstacle(obstacleID = obstacle.id)
                        println("$xCoord, $yCoord: After removing ${obstacle.id}: ${arenaUiState.obstacles}")
                        viewModel.addObstacle(
                            Obstacle(
                                id = obstacle.id,
                                xPos = xCoord,
                                yPos = yCoord,
                                facing = obstacle.facing
                            )
                        )
                        println("$xCoord, $yCoord: After Adding ${obstacle.id}: ${arenaUiState.obstacles}")
                    } else {
                        viewModel.addObstacle(
                            Obstacle(
                                id = arenaUiState.nextObsID,
                                xPos = xCoord,
                                yPos = yCoord,
                                facing = "N"
                            )
                        )
                    }

                }
            }
            if (xCoord == arenaUiState.robotPosX && yCoord == arenaUiState.robotPosY) {
                drawRobot(arenaUiState.robotFacing)
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
fun drawObstacle(
    obstacle: Obstacle,
    viewModel: ArenaViewModel,
) {
    if (obstacle.facing == "N") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Black)
                .aspectRatio(1f)
                .background(Color.Black)
                .clickable {
                    viewModel.changeObstacleFacing(obstacle)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Red)
            ) {}
            if (obstacle.value!=null) {
                Text(
                    text = obstacle.value,
                    color = Color.Green
                )
            }
            else {
                Text(
                    text = obstacle.id.toString(),
                    color = Color.Green
                )
            }
            DragTarget(dataToDrop = obstacle) {

            }
        }
    }
    if (obstacle.facing == "S") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Black)
                .aspectRatio(1f)
                .background(Color.Black)
                .clickable {
                    viewModel.changeObstacleFacing(obstacle)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Red)
            ) {}
            if (obstacle.value!=null) {
                Text(
                    text = obstacle.value,
                    color = Color.Green
                )
            }
            else {
                Text(
                    text = obstacle.id.toString(),
                    color = Color.Green
                )
            }
            DragTarget(dataToDrop = obstacle) {}
        }
    }
    if (obstacle.facing == "E") {
        Row {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .aspectRatio(1f)
                    .background(Color.Black)
                    .clickable {
                        viewModel.changeObstacleFacing(obstacle)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (obstacle.value!=null) {
                    Text(
                        text = obstacle.value,
                        color = Color.Green
                    )
                }
                else {
                    Text(
                        text = obstacle.id.toString(),
                        color = Color.Green
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.Red)
                )
                DragTarget(dataToDrop = obstacle) {}
            }
        }
    }
    if (obstacle.facing == "W") {
        Row {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .aspectRatio(1f)
                    .background(Color.Black)
                    .clickable {
                        viewModel.changeObstacleFacing(obstacle)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (obstacle.value!=null) {
                    Text(
                        text = obstacle.value,
                        color = Color.Green
                    )
                }
                else {
                    Text(
                        text = obstacle.id.toString(),
                        color = Color.Green
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Red)
                )
                DragTarget(dataToDrop = obstacle) {}
            }
        }
    }
}

@Composable
fun drawRobot(
    Orientation: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black)
            .aspectRatio(1f)
            .background(Color.Red.copy(0.4f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
        ) {
            if (Orientation == "N") {
                val trianglePath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width, size.height)
                    lineTo(size.width / 2, 0f)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color.Blue
                )
            }
            if (Orientation == "S") {
                val trianglePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2, size.height)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color.Blue
                )
            }
            if (Orientation == "E") {
                val trianglePath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, size.height / 2)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color.Blue
                )
            }
            if (Orientation == "W") {
                val trianglePath = Path().apply {
                    moveTo(size.width, size.height)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height / 2)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color.Blue
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
fun ObstacleInput(viewModel: ArenaViewModel, arenaUiState: ArenaUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Drag Obstacle onto Arena: ", fontWeight = FontWeight.Bold)
        DragTarget(
            dataToDrop = Obstacle(id = arenaUiState.nextObsID, facing = "N")
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .shadow(5.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (arenaUiState.nextObsID).toString(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.padding(10.dp))
        DropItem<Obstacle>(
            modifier = Modifier.size(55.dp) // TODO: Change dynamic size?
        ) { isInBound, obstacle ->
            if (obstacle != null) {
                LaunchedEffect(key1 = obstacle) {
                    viewModel.removeObstacle(obstacleID = obstacle.id)
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

@Composable
fun ClearObstacles(viewModel: ArenaViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Remove all obstacles: ", fontWeight = FontWeight.Bold)
        Button(
            onClick = { viewModel.removeAllObstacles() },
            modifier = Modifier
                .size(100.dp)
                .shadow(5.dp)
                .background(Color.Black),
        ) {
            Text(text = "Clear all")
        }
    }
}

@Composable
fun SetRobotOrientation(viewModel: ArenaViewModel, arenaUiState: ArenaUiState) {
    var robotX by remember { mutableStateOf("0") }
    var robotY by remember { mutableStateOf("0") }

    Spacer(modifier = Modifier.padding(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(0.dp, 0.dp, 0.dp, spacerDP),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Robot Start Point", fontWeight = FontWeight.Bold)
        Text(text = "X: ")
        TextField(
            modifier = Modifier.width(70.dp),
            value = robotX,
            onValueChange = { newX: String ->
                if (newX != "") {
                    val w = newX.toInt()
                    if (w in 0..20) {
                        viewModel.setRobotPosFacing(
                            x = w,
                            y = arenaUiState.robotPosY,
                            facing = "N"
                        )
                    }
                }
                if (newX.length <= 2) {
                    robotX = newX
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            )
        )
        Text(text = "Y: ")
        TextField(
            modifier = Modifier.width(70.dp),
            value = robotY,
            onValueChange = { newY: String ->
                if (newY != "") {
                    val h = newY.toInt()
                    if (h in 0..20) {
                        viewModel.setRobotPosFacing(
                            x = arenaUiState.robotPosX,
                            y = h,
                            facing = "N"
                        )
                    }
                }
                if (newY.length <= 2) {
                    robotY = newY
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            )
        )
    }
}