
package com.example.exam.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.exam.feature.api.presentation.ApiScreen

import com.example.exam.ui.theme.AppBackground
import com.example.exam.ui.theme.AppBorder
import com.example.exam.ui.theme.AppSurface
import com.example.exam.ui.theme.PrimaryBlue
import com.example.exam.ui.theme.TextMuted

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route



    // Movie detail visibility
    var isMovieDetailOpen by remember {
        mutableStateOf(false)
    }

    // Reset detail visibility when navigating away from API
    LaunchedEffect(currentRoute) {
        if (currentRoute != AppRoutes.Api.route) {
            isMovieDetailOpen = false
        }
    }

    // Show bottom navigation only on main screens
    val showBottomBar =
        !isMovieDetailOpen &&
                BottomNavItem.items.any { item ->
                    item.route == currentRoute
                }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppBackground,
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomNavigation(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(
                                navController.graph.findStartDestination().id
                            ) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.Ble.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // API / MOVIE EXPLORER SCREEN
            composable(AppRoutes.Api.route) {
                ApiScreen(
                    onMovieDetailVisibilityChanged = { isOpen ->
                        isMovieDetailOpen = isOpen
                    }
                )
            }

            // BLE DEVICE DETAIL SCREEN
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
//
//                DeviceDetailScreen(
//                    deviceId = deviceId,
//                    onBackClick = {
//                        navController.popBackStack()
//                    },
//                    viewModel = bleViewModel
//                )
            }
        }
    }
}

@Composable
private fun FloatingBottomNavigation(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = AppSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = AppBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 7.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            BottomNavItem.items.forEach { item ->

                val selected = currentRoute == item.route

                val animatedWeight by animateFloatAsState(
                    targetValue = if (selected) 1.65f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 350f
                    ),
                    label = "navigationWeight"
                )

                val backgroundColor by animateColorAsState(
                    targetValue = if (selected) {
                        PrimaryBlue
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(250),
                    label = "navigationBackground"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (selected) {
                        Color.White
                    } else {
                        TextMuted
                    },
                    animationSpec = tween(250),
                    label = "navigationIconColor"
                )

                Box(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(backgroundColor)
                        .clickable {
                            onItemClick(item.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )

                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn(
                                animationSpec = tween(180)
                            ) + expandHorizontally(
                                animationSpec = tween(250)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(100)
                            ) + shrinkHorizontally(
                                animationSpec = tween(180)
                            )
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
