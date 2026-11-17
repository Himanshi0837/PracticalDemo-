package com.example.exam.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Ble : BottomNavItem(
        route = AppRoutes.Ble.route,
        title = "BLE",
        icon = Icons.Default.Bluetooth
    )
    data object Api : BottomNavItem(
        route = AppRoutes.Api.route,
        title = "Dashboard",
        icon = Icons.Default.Home
    )
    companion object {
        val items = listOf(Ble, Api)
    }
}
