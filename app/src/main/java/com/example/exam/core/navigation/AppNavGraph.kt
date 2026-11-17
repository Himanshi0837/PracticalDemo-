
package com.example.exam.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.exam.feature.api.presentation.ApiScreen
import com.example.exam.feature.ble.presentation.BleScreen
import com.example.exam.feature.ble.presentation.BleViewModel
import com.example.exam.feature.ble.presentation.DeviceDetailScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Shared BLE ViewModel for BLE list and device details.
    val bleViewModel: BleViewModel = hiltViewModel()

    val showBottomBar = BottomNavItem.items.any {
        it.route == currentRoute
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    // BLE first, API second.
                    BottomNavItem.items.forEach { item ->
                        val selected = currentRoute == item.route

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(item.title)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.Ble.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // BLE Screen
            composable(AppRoutes.Ble.route) {
                BleScreen(
                    onDeviceClick = { deviceId ->
                        navController.navigate(
                            AppRoutes.DeviceDetail.createRoute(deviceId)
                        )
                    },
                    viewModel = bleViewModel
                )
            }

            // API Screen
            composable(AppRoutes.Api.route) {
                ApiScreen()
            }

            // Device Detail Screen
            composable(
                route = AppRoutes.DeviceDetail.route,
                arguments = listOf(
                    navArgument("deviceId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val deviceId =
                    backStackEntry.arguments?.getString("deviceId") ?: ""

                DeviceDetailScreen(
                    deviceId = deviceId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    viewModel = bleViewModel
                )
            }
        }
    }
}
