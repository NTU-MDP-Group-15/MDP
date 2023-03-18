package com.example.code.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.code.service.BluetoothService
import com.example.code.ui.states.ArenaUiState
import com.example.code.ui.states.Obstacle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//Methods to change arena states
class ArenaViewModel : ViewModel() {
    // Arena UI State
    private val _uiState = MutableStateFlow(ArenaUiState())
    val uiState: StateFlow<ArenaUiState> = _uiState.asStateFlow()

    // Get Task Configuration mode (Image Reg/Fastest Car)
    fun getTaskMode() : String{
        return _uiState.value.taskMode
    }

    // Set Task Configuration Mode (Image Reg/Fastest Car)
    fun setTaskMode(taskMode: String) {
        _uiState.update { currentState -> currentState.copy(taskMode = taskMode) }
    }

    // Reposition Obstacle
    fun repositionObstacle(obstacle: Obstacle) {
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = _uiState.value.obstacles.plus(obstacle),
            )
        }
    }

    // Add Obstacle
    fun addObstacle(obstacle: Obstacle) {
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = _uiState.value.obstacles.plus(obstacle),
                nextObsID = _uiState.value.nextObsID + 1
            )
        }
    }

    // Change orientation of obstacle image facing
    fun changeObstacleFacing(obstacle: Obstacle) {
        val obstacles = _uiState.value.obstacles.filter { obs -> obs.id != obstacle.id }
        var facing = ""
        when (obstacle.facing) {
            "N" -> facing = "E"
            "E" -> facing = "S"
            "S" -> facing = "W"
            "W" -> facing = "N"
        }
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = obstacles.plus(
                    Obstacle(
                        id = obstacle.id,
                        xPos = obstacle.xPos,
                        yPos = obstacle.yPos,
                        facing = facing,
                        value = obstacle.value
                    )
                )
            )
        }
    }

    // Remove Obstacle
    fun removeObstacle(obstacleID: Int) {
        val obstacles = _uiState.value.obstacles.filter { obs -> obs.id != obstacleID }
        _uiState.update { currentState -> currentState.copy(obstacles = obstacles) }
    }

    // Remove all Obstacles
    fun removeAllObstacles() {
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = emptyList(),
                nextObsID = 1
            )
        }
    }

    // Set Obstacle Value
    fun setObstacleValue(id: Int, value: Int) {
        val tmp = _uiState.value.obstacles.filter { it.id == id }
        if (tmp.isEmpty())
            return
        val obstacle = tmp.first()
        val restObstacles = _uiState.value.obstacles.filter { it.id != id }
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = restObstacles.plus(
                    Obstacle(
                        id = id,
                        xPos = obstacle.xPos,
                        yPos = obstacle.yPos,
                        facing = obstacle.facing,
                        value = value
                    )
                ),
            )
        }
    }

    // Store Robot Positions and Facings
    fun storeRobotPosFacing(payload: String) {
        val coordinatesAndFacingList = payload.split(",")
        _uiState.update { currentState ->
            currentState.copy(
                storedCoordinates = coordinatesAndFacingList,
                coordinateCounter = 3
            )
        }
    }

    //To check if orientation of robot and obstacle image facings are valid
    private fun toFacing(degree: String) : String {
        var facing = ""

        when (degree) {
            "0" -> facing = "N"
            "180" -> facing = "S"
            "-90" -> facing = "E"
            "90" -> facing = "W"
        }
        return facing
    }

    //To check if coordinates received are within the grid(10cm buffer from edge of arena)
    private fun validCoordinates(coordinate: Int):Boolean {
        val list = (1..18).toList()
        if (list.contains(coordinate)) {
            return true
        }
        return false
    }

    // Set Robot Position and Facing
    fun setRobotPosFacing() {
        var count=0
        if (_uiState.value.coordinateCounter <= _uiState.value.storedCoordinates.size-4) {
            count = _uiState.value.coordinateCounter
        }
        else {
            return
        }

        val x = _uiState.value.storedCoordinates[count].toInt()
        val y = _uiState.value.storedCoordinates[count + 1].toInt()
        val facing = toFacing(_uiState.value.storedCoordinates[count + 2])

        if (!validCoordinates(x) || !validCoordinates(y) || facing == "") {
            _uiState.update { currentState -> currentState.copy(coordinateCounter = count+3)}
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                robotPosX = x,
                robotPosY = y,
                robotFacing = facing,
                coordinateCounter = count + 3
            )
        }
    }

    //Reset robot back to start position, facing north
    fun resetRobot() {
        _uiState.update { currentState ->
            currentState.copy(
                robotPosX = 1,
                robotPosY = 1,
                robotFacing = "N"
            )
        }
    }

    // Set Robot Status Message
    fun setRobotStatusMessage(message: String) {
        _uiState.update { currentState ->
            currentState.copy(robotStatusMessage = message)
        }
    }

    // Set connection status
    fun updateBTConnectionStatus(status: Int) {
        if (status == 3) {
            _uiState.update { currentState ->
                currentState.copy(bluetoothConnectionStatus = true)
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(bluetoothConnectionStatus = false)
            }
        }
    }
}