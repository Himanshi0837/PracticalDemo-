package com.example.exam.core.navigation

sealed class AppRoutes(val route: String) {
    data object Api : AppRoutes("api")
    data object Ble : AppRoutes("ble")
    data object DeviceDetail : AppRoutes("device_detail/{deviceId}") {
        fun createRoute(deviceId: String): String = "device_detail/$deviceId"
    }
}
