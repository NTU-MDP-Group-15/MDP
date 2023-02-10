package com.example.code.ui.states

data class ArenaUiState(
    val gridWidth: Int = 20,
    val gridHeight: Int = 20,

    val taskMode: Int = 0,
    val taskConfig: List<String> = listOf("Image Recognition", "Fastest Car"),

    val obstacles: List<Obstacle> = emptyList(),

    val robotPosX: Int = 0,
    val robotPosY: Int = 0,
    val robotFacing: String = "N"
)

data class Obstacle(
    val id: Int,
    val xPos: Int,
    val yPos: Int,
    val facing: String? = null,
    val value: String? = null
)