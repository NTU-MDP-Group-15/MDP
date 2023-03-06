package com.example.code.ui.states

data class ArenaUiState(
    val gridWidth: Int = 20,
    val gridHeight: Int = 20,

    val taskMode: String = "Image Recognition",

    val obstacles: List<Obstacle> = emptyList(),
    val nextObsID: Int = 1,

    val robotPosX: Int = 1,
    val robotPosY: Int = 1,
    val robotFacing: String = "N",

    val robotStatusMessage: String = "",

    val bluetoothConnectionStatus: Boolean = false,

    val storedCoordinates: List<String> = emptyList(),
    val coordinateCounter: Int = 3
)

data class Obstacle(
    val id: Int,
    val xPos: Int? = null,
    val yPos: Int? = null,
    val facing: String? = null,
    val value: Int? = null
)