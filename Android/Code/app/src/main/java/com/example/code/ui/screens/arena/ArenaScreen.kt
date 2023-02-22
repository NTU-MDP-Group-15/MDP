package com.example.code.ui.screens.arena

import android.os.Message
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.service.BluetoothService
import com.example.code.service.MessageService
import com.example.code.ui.states.ArenaUiState
import com.example.code.ui.states.BluetoothUiState
import com.example.code.ui.states.Obstacle
import com.example.code.ui.viewmodels.ArenaViewModel

val spacerDP = 10.dp

@Composable
fun ArenaScreen(
    viewModel: ArenaViewModel,
    bluetoothService: BluetoothService
) {
    val arenaUiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Arena Grid
        Column(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .fillMaxHeight(1f)
        ) {
            ArenaGrid(
                arenaUiState = arenaUiState,
                viewModel = viewModel,
                bluetoothService = bluetoothService
            )
        }
        // Configuration Settings
        Column(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .padding(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            StatusDisplay(arenaUiState = arenaUiState)
            Spacer(modifier = Modifier.padding(bottom = spacerDP))
            TaskModeInput(viewModel = viewModel)
            //GridSizeInput(viewModel = viewModel, arenaUiState = arenaUiState)
            //SetRobotOrientation(viewModel = viewModel, arenaUiState = arenaUiState)
            ObstacleInput(
                viewModel = viewModel,
                arenaUiState = arenaUiState,
                bluetoothService = bluetoothService
            )
            Spacer(modifier = Modifier.padding(bottom = spacerDP))
            ClearObstacles(viewModel = viewModel)
            Spacer(modifier = Modifier.padding(bottom = spacerDP))
            Text(text = "Robot Status", fontSize = 20.sp)
            TextField(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .fillMaxWidth(1f),
                value = arenaUiState.robotStatusMessage,
                onValueChange = {},
                readOnly = true
            )
            Row(){
                StartRobot(bluetoothService = bluetoothService)
            }
        }
    }
}

@Composable
fun StatusDisplay(arenaUiState: ArenaUiState) {
    Row(modifier = Modifier.fillMaxWidth(1f)) {
        Text(
            text = "${arenaUiState.taskMode} | " +
                    "${arenaUiState.gridWidth}, ${arenaUiState.gridHeight} | ",
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun ArenaGrid(
    arenaUiState: ArenaUiState,
    viewModel: ArenaViewModel,
    bluetoothService: BluetoothService
) {
    val coordinateFontSize = 13.sp
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight(1f)
    ) {
        Row {
            for (i in 0 until arenaUiState.gridWidth) {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    for (j in arenaUiState.gridHeight - 1 downTo 0) {
                        if (i == 0) {
                            Row {
                                Text(
                                    text = "$j ",
                                    fontSize = coordinateFontSize
                                )
                                GridBox(
                                    viewModel = viewModel,
                                    xCoordinate = i,
                                    yCoordinate = j,
                                    arenaUiState = arenaUiState,
                                    bluetoothService = bluetoothService
                                )
                            }
                        } else {
                            GridBox(
                                viewModel = viewModel,
                                xCoordinate = i,
                                yCoordinate = j,
                                arenaUiState = arenaUiState,
                                bluetoothService = bluetoothService
                            )
                        }
                    }
                    Text(
                        text = "$i",
                        fontSize = coordinateFontSize
                    )
                }
            }
        }
    }
}

@Composable
fun GridBox(
    viewModel: ArenaViewModel,
    bluetoothService: BluetoothService,
    arenaUiState: ArenaUiState,
    xCoordinate: Int,
    yCoordinate: Int,
) {
    DropItem<Obstacle>(
        modifier = Modifier.size(20.dp)
    ) { isInBound, obstacle ->
        val self =
            arenaUiState.obstacles.filter { (it.xPos == xCoordinate) && (it.yPos == yCoordinate) }
        if (self.isNotEmpty()) {
            DrawObstacle(
                obstacle = self.first(),
                viewModel = viewModel,
                bluetoothService = bluetoothService
            )
        } else {
            if (obstacle != null) {
                LaunchedEffect(key1 = obstacle) {
                    if (obstacle.xPos != null && obstacle.xPos != xCoordinate) {
                        viewModel.removeObstacle(obstacleID = obstacle.id)
                        MessageService.sendObstaclePlacement(
                            bts = bluetoothService,
                            action = "SUB",
                            id = obstacle.id,
                            x = obstacle.xPos,
                            y = obstacle.yPos!!
                        )
                        viewModel.repositionObstacle(
                            Obstacle(
                                id = obstacle.id,
                                xPos = xCoordinate,
                                yPos = yCoordinate,
                                facing = obstacle.facing
                            )
                        )
                        MessageService.sendObstaclePlacement(
                            bts = bluetoothService,
                            action = "ADD",
                            id = obstacle.id,
                            x = xCoordinate,
                            y = yCoordinate
                        )
                        MessageService.sendObstacleFacing(
                            bts = bluetoothService,
                            id = obstacle.id,
                            facing = obstacle.facing!!
                        )
                    } else if (obstacle.xPos != null) {
                        // do nothing
                    } else {
                        val id = arenaUiState.nextObsID
                        viewModel.addObstacle(
                            Obstacle(
                                id = id,
                                xPos = xCoordinate,
                                yPos = yCoordinate,
                                facing = "N"
                            )
                        )
                        MessageService.sendObstaclePlacement(
                            bts = bluetoothService,
                            action = "ADD",
                            id = id,
                            x = xCoordinate,
                            y = yCoordinate
                        )
                        MessageService.sendObstacleFacing(
                            bts = bluetoothService,
                            id = id,
                            facing = "N"
                        )
                    }
                }
            }
            var bgColor = Color.White
            if (isInBound) {
                bgColor = Color.Gray.copy(0.5f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Black)
                    .background(bgColor)
            )
            if (xCoordinate == arenaUiState.robotPosX && yCoordinate == arenaUiState.robotPosY) {
                DrawRobot(arenaUiState.robotFacing)
            }
        }
    }
}

@Composable
fun DrawObstacle(
    obstacle: Obstacle,
    viewModel: ArenaViewModel,
    bluetoothService: BluetoothService
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black)
            .aspectRatio(1f)
            .background(Color.Black)
            .clickable {
                viewModel.changeObstacleFacing(obstacle, bluetoothService)
            },
        contentAlignment = Alignment.Center
    ) {
        when (obstacle.facing) {
            "N" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .background(Color.Red)
                )
            }
            "E" -> {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .align(Alignment.CenterEnd)
                        .background(Color.Red)
                )
            }
            "S" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.Red)
                )
            }
            "W" -> {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .align(Alignment.CenterStart)
                        .background(Color.Red)
                )
            }
        }
        var obstacleValue = obstacle.id.toString()
        if (obstacle.value != null) {
            obstacleValue = obstacle.value.toString()
        }
        Text(
            text = obstacleValue,
            color = Color.Green
        )
        DragTarget(dataToDrop = obstacle) {}
    }
}

@Composable
fun DrawRobot(
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
            var trianglePath = Path().apply {
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width / 2, 0f)
                close()
            }
            if (Orientation == "S") {
                trianglePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2, size.height)
                    close()
                }
            }
            if (Orientation == "E") {
                trianglePath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, size.height / 2)
                    close()
                }
            }
            if (Orientation == "W") {
                trianglePath = Path().apply {
                    moveTo(size.width, size.height)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height / 2)
                    close()
                }
            }
            drawPath(
                path = trianglePath,
                color = Color.Blue
            )
        }
    }
}

@Composable
fun TaskModeInput(viewModel: ArenaViewModel) {
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
fun ObstacleInput(
    viewModel: ArenaViewModel,
    arenaUiState: ArenaUiState,
    bluetoothService: BluetoothService
) {
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
            modifier = Modifier.size(55.dp)
        ) { isInBound, obstacle ->
            if (obstacle != null) {
                LaunchedEffect(key1 = obstacle) {
                    viewModel.removeObstacle(obstacleID = obstacle.id)
                    MessageService.sendObstaclePlacement(
                        bts = bluetoothService,
                        action = "SUB",
                        id = obstacle.id,
                        x = obstacle.xPos!!,
                        y = obstacle.yPos!!
                    )
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
                    if (w in 0..19) {
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

@Composable
fun StartRobot(
    bluetoothService: BluetoothService
) {
    Button(onClick = {
        MessageService.sendStartSignal(bts=bluetoothService)
    }) {
        Text(text="Start Task")
    }
}

