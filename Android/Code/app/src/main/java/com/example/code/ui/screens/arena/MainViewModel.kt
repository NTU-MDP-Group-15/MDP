package com.example.code.ui.viewmodels

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.code.ui.screens.arena.PersonUiItem

class MainViewModel :ViewModel() {

    var isCurrentlyDragging by mutableStateOf(false)
        private set

    var items by mutableStateOf(emptyList<PersonUiItem>())
        private set

    var addedPersons = mutableStateListOf<PersonUiItem>()
        private set

    init {
        items = listOf(
            PersonUiItem("1", Color.Gray),
            PersonUiItem("2", Color.Blue),
            PersonUiItem("3", Color.Green),
        )
    }

    fun startDragging(){
        isCurrentlyDragging = true
    }
    fun stopDragging(){
        isCurrentlyDragging = false
    }

    fun addPerson(personUiItem: PersonUiItem){
        println("Added Obstacle")
        addedPersons.add(personUiItem)
    }

}