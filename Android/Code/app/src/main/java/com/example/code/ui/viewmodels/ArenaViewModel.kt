package com.example.code.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
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

    // Add Obstacle
    fun addObstacle(obstacle: Obstacle) {
        _uiState.update { currentState ->
            currentState.copy(
                obstacles = _uiState.value.obstacles.plus(obstacle),
                nextObsID = _uiState.value.nextObsID + 1
            )
        }
    }

    fun changeObstacleFacing(obstacle: Obstacle) {
        val obstacles = _uiState.value.obstacles.filter { obs -> obs.id != obstacle.id }
        if (obstacle.facing == "N") {
            val newObstacle =
                Obstacle(obstacle.id, obstacle.xPos, obstacle.yPos, "E", obstacle.value)
            _uiState.update { currentState ->
                currentState.copy(
                    obstacles = obstacles.plus(
                        newObstacle
                    )
                )
            }
        } else if (obstacle.facing == "E") {
            val newObstacle =
                Obstacle(obstacle.id, obstacle.xPos, obstacle.yPos, "S", obstacle.value)
            _uiState.update { currentState ->
                currentState.copy(
                    obstacles = obstacles.plus(
                        newObstacle
                    )
                )
            }
        } else if (obstacle.facing == "S") {
            val newObstacle =
                Obstacle(obstacle.id, obstacle.xPos, obstacle.yPos, "W", obstacle.value)
            _uiState.update { currentState ->
                currentState.copy(
                    obstacles = obstacles.plus(
                        newObstacle
                    )
                )
            }
        } else if (obstacle.facing == "W") {
            val newObstacle =
                Obstacle(obstacle.id, obstacle.xPos, obstacle.yPos, "N", obstacle.value)
            _uiState.update { currentState ->
                currentState.copy(
                    obstacles = obstacles.plus(
                        newObstacle
                    )
                )
            }
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