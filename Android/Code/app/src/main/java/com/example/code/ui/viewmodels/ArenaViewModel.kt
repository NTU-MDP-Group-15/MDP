package com.example.code.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.code.service.BluetoothService
import com.example.code.service.MessageService
import com.example.code.ui.states.ArenaUiState
import com.example.code.ui.states.Obstacle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ArenaViewModel : ViewModel() {
    // Arena UI State
    private val _uiState = MutableStateFlow(ArenaUiState())
    val uiState: StateFlow<ArenaUiState> = _uiState.asStateFlow()

    // Set Arena Grid Size
    fun setGridSize(width: Int, height: Int) {
        var w = 0
        var h = 0
        if (width in 0..20) {
            w = width
        }
        if (height in 0..20) {
            h = height
        }
        _uiState.update { currentState ->
            currentState.copy(
                gridWidth = w,
                gridHeight = h
            )
        }
    }

    // Set Task Configuration Mode (Image Reg/Fastest Track)
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

    fun changeObstacleFacing(obstacle: Obstacle, bluetoothService: BluetoothService) {
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
        MessageService.sendObstacleFacing(
            bts = bluetoothService,
            id = obstacle.id,
            facing = facing
        )
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
        val obs = _uiState.value.obstacles.filter { obs -> obs.id == id }
        val ob = obs.first()
        val obstacles = _uiState.value.obstacles.filter { obs -> obs.id != id }
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = obstacles.plus(
                    Obstacle(
                        id = id,
                        xPos = ob.xPos,
                        yPos = ob.yPos,
                        facing = ob.facing,
                        value = value
                    )
                ),
            )
        }
    }

    // Set Robot Position and Facing
    fun setRobotPosFacing(x: Int, y: Int, facing: String) {
        _uiState.update { currentState ->
            currentState.copy(
                robotPosX = x,
                robotPosY = y,
                robotFacing = facing
            )
        }
    }
}