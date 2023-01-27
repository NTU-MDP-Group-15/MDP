package com.example.code.ui.navigation

import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.code.R

@Composable
fun NavRailItems(navController: NavHostController, items: List<String>) {
    val icons = listOf(
        painterResource(id = R.drawable.bluetooth),
        painterResource(id = R.drawable.home),
        painterResource(id = R.drawable.debug)
    )

    NavigationRail {
        items.forEachIndexed { index, item ->
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination
            NavigationRailItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = currentDestination?.route?.let { it == item } ?: false,
                onClick = { navController.navigate(item) }
            )
        }
    }
}