package com.example.code.ui.screens.arena

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.R
import com.example.code.service.BluetoothService
import com.example.code.service.MessageService
import com.example.code.ui.states.ArenaUiState
import com.example.code.ui.states.Obstacle
import com.example.code.ui.viewmodels.ArenaViewModel

val spacerDP = 10.dp

//Load arena screen
@Composable
fun ArenaScreen(
    viewModel: ArenaViewModel,
    bluetoothService: BluetoothService
) {
    val arenaUiState by viewModel.uiState.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Arena Grid
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(1f)
        ) {
            ArenaGrid(
                arenaUiState = arenaUiState,
                viewModel = viewModel
            )
        }
        // Configuration Settings
        Column(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
        ) {
            if (arenaUiState.taskMode=="Image Recognition") {
                ImageRecScreen(
                    arenaUiState = arenaUiState,
                    viewModel = viewModel,
                    bluetoothService = bluetoothService
                )
            }
            else {
                StatusDisplay(arenaUiState = arenaUiState)
                Spacer(modifier = Modifier.padding(bottom = spacerDP))
                TaskModeInput(viewModel = viewModel)
                Spacer(modifier = Modifier.padding(bottom = spacerDP))
                StartRobot(bluetoothService = bluetoothService)
            }
        }
    }
}

//To display if task selected if Image Recognition
@Composable
fun ImageRecScreen(arenaUiState: ArenaUiState, viewModel: ArenaViewModel, bluetoothService: BluetoothService) {
    StatusDisplay(arenaUiState = arenaUiState)
    Spacer(modifier = Modifier.padding(bottom = spacerDP))
    TaskModeInput(viewModel = viewModel)
    Spacer(modifier = Modifier.padding(bottom = spacerDP))
    Text(text = "Robot Status", fontSize = 20.sp)
    TextField(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.3f),
        value = arenaUiState.robotStatusMessage,
        onValueChange = {},
        readOnly = true
    )
    Spacer(modifier = Modifier.padding(bottom = spacerDP))

    ObstacleInput(
        viewModel = viewModel,
        arenaUiState = arenaUiState
    )
    Spacer(modifier = Modifier.padding(bottom = 15.dp))
    Row(){
        ClearObstacles(viewModel = viewModel)
        Spacer(modifier = Modifier.padding(5.dp))
        SendObstacles(bluetoothService = bluetoothService, arenaUiState = arenaUiState)
        Spacer(modifier = Modifier.padding(5.dp))
        StartRobot(bluetoothService = bluetoothService)
    }
    RestartRobot(viewModel = viewModel)
}

//Task & connectivity display
@Composable
fun StatusDisplay(arenaUiState: ArenaUiState) {
    Row(modifier = Modifier.fillMaxWidth(1f)) {
        var btStatus = "Not connected"
        if (arenaUiState.bluetoothConnectionStatus) {
            btStatus = "Connected"
        }
        Text(
            text = "${arenaUiState.taskMode} | $btStatus",
            fontWeight = FontWeight.ExtraBold
        )
    }
}

//Drawing 20x20 arena grid
@Composable
fun ArenaGrid(
    arenaUiState: ArenaUiState,
    viewModel: ArenaViewModel
) {
    val coordinateFontSize = 15.sp
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
                                )
                            }
                        } else {
                            GridBox(
                                viewModel = viewModel,
                                xCoordinate = i,
                                yCoordinate = j,
                                arenaUiState = arenaUiState,
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

//Individual grid boxes
@Composable
fun GridBox(
    viewModel: ArenaViewModel,
    arenaUiState: ArenaUiState,
    xCoordinate: Int,
    yCoordinate: Int,
) {
    DropItem<Obstacle>(
        modifier = Modifier.size(26.dp)
    ) { isInBound, obstacle ->
        val self =
            arenaUiState.obstacles.filter { (it.xPos == xCoordinate) && (it.yPos == yCoordinate) }
        if (self.isNotEmpty()) {
            DrawObstacle(
                obstacle = self.first(),
                viewModel = viewModel
            )
        } else {
            if (obstacle != null) {
                LaunchedEffect(key1 = obstacle) {
                    if (obstacle.xPos != null && obstacle.xPos != xCoordinate) {
                        viewModel.removeObstacle(obstacleID = obstacle.id)
                        viewModel.repositionObstacle(
                            Obstacle(
                                id = obstacle.id,
                                xPos = xCoordinate,
                                yPos = yCoordinate,
                                facing = obstacle.facing
                            )
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
                    }
                }
            }
            var bgColor = Color.White
            if (isInBound) {
                bgColor = Color.Gray.copy(0.5f)
            }
            if (inRobotBound(xCoordinate,yCoordinate,arenaUiState)) {
                bgColor = Color.Red.copy(0.4f)
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

//To check if robot has 'left' the grid;
//Valid coordinates: True, draw robot onto grid box
//Invalid coordinates: False, do not draw robot onto grid box
private fun inRobotBound(
    x: Int,
    y: Int,
    arenaUiState: ArenaUiState
) : Boolean {
    var inXBound = false
    var inYBound = false

    if (x in arenaUiState.robotPosX-1 ..  arenaUiState.robotPosX+1) {
        inXBound = true
    }
    if (y in arenaUiState.robotPosY-1 ..  arenaUiState.robotPosY+1) {
        inYBound = true
    }
    return inXBound && inYBound
}

//If obstacle is occupying a particular grid box, draw obstacle on the grid
//Tap gestures captured to change orientation of image facing
@Composable
fun DrawObstacle(
    obstacle: Obstacle,
    viewModel: ArenaViewModel,
) {
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

//If grid box is occupied by robot, draw robot on the grid
@Composable
fun DrawRobot(
    Orientation: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black)
            .aspectRatio(1f)
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

//To switch between Fastest Car task and Image Recognition task
@Composable
fun TaskModeInput(viewModel: ArenaViewModel) {
    val imgReg = "Image Recognition"
    val fastCar = "Fastest Car"
    val task = viewModel.getTaskMode()

    val selectedTask = remember { mutableStateOf(task) }
    val taskModes = listOf(imgReg, fastCar)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Task:",
            fontWeight = FontWeight.Bold,
        )
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

//Drag and drop of obstacles
@Composable
fun ObstacleInput(
    viewModel: ArenaViewModel,
    arenaUiState: ArenaUiState,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Obstacles:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 5.dp)
        )
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
                        .border(1.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.delete),
                        modifier = Modifier.fillMaxSize(0.8f),
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

//Clear out the grid
@Composable
fun ClearObstacles(viewModel: ArenaViewModel) {
    Button(
        onClick = { viewModel.removeAllObstacles() },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
    ) {
        Text(
            text = "Clear All",
            color = Color.White
        )
    }
}

//Start button to begin tasks
@Composable
fun StartRobot(
    bluetoothService: BluetoothService
) {
    Button(
        onClick = {
        MessageService.sendStartSignal(bts=bluetoothService)},
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF15AB13))
    ) {
        Text(
            text="Start",
            color = Color.White
        )
    }
}

//Send button to send list of obstacles to algorithm for path planning
@Composable
fun SendObstacles(
    bluetoothService: BluetoothService,
    arenaUiState: ArenaUiState
) {
    Button(onClick = {
        MessageService.sendObstacles(
            bts=bluetoothService,
            obstacleList = arenaUiState.obstacles,
            robotX = arenaUiState.robotPosX,
            robotY = arenaUiState.robotPosY,
            robotFacing = arenaUiState.robotFacing
        )
    }) {
        Text(text="Send Obstacles")
    }
}

//Reset robot position to starting point
@Composable
fun RestartRobot(viewModel: ArenaViewModel) {
    Button(
        onClick = { viewModel.resetRobot() },
    ) {
        Text(
            text = "Reset Robot",
            color = Color.White
        )
    }
}



