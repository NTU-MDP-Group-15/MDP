package com.example.code.ui.viewmodels

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
        _uiState.update { currentState ->
            currentState.copy(
                gridWidth = width,
                gridHeight = height
            )
        }
    }

    // Set Task Configuration Mode (Image Reg/Fastest Track)
    fun setTaskMode(taskMode: Int) {
        _uiState.update { currentState -> currentState.copy(taskMode = taskMode) }
    }

    // Add Obstacle
    fun addObstacle(obstacle: Map<K, V>) {

    }

    // Remove Obstacle

    // Remove all Obstacles
    fun removeAllObstacles() {
        _uiState.update { currentState -> currentState.copy(obstacles = emptyList()) }
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