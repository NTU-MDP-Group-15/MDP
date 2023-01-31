package com.example.code.ui.screens.arena

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ArenaScreen() {
    Column() {
        Text(text = "Arena Screen")
        Primes()
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Primes() {
    LazyVerticalGrid(
        modifier = Modifier // 1
            .fillMaxWidth(0.4F)
            .background(Color(0xFFE53935))
            .padding(8.dp),
        columns = GridCells.Fixed(10), // 2
    ) {
        items(count = 100) { // 3
            Box(
                Modifier // 4
                    .aspectRatio(1f)
                    .padding(1.dp)
                    .background(Color.Cyan)
            )
        }
    }
}

