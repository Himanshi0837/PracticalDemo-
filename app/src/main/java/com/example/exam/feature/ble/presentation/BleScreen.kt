package com.example.exam.feature.ble.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.exam.core.bluetooth.BleError
import com.example.exam.core.bluetooth.BlePermissionHelper
import com.example.exam.feature.ble.presentation.components.BleRadarVisual
import com.example.exam.feature.ble.presentation.components.DeviceCardItem
import com.example.exam.ui.theme.AppBackground
import com.example.exam.ui.theme.PrimaryBlue
import com.example.exam.ui.theme.PrimaryBlueDark
import com.example.exam.ui.theme.PrimaryContainer
import com.example.exam.ui.theme.SecondaryRose
import com.example.exam.ui.theme.SignalRed
import com.example.exam.ui.theme.SignalRedBg
import com.example.exam.ui.theme.TextPrimary
import com.example.exam.ui.theme.TextSecondary
import com.example.exam.ui.theme.Wine

/**
 * Main BLE Scanner Screen following the exact concept and UI of BLEDemo:
 * - Top Bar with "BLE Tracker" and refresh.
 * - Dynamic STOP/SCAN pill appearing in the Top Bar ONLY when the user scrolls past the top card.
 * - Top Blue Gradient Card with animated BleRadarVisual, status, and scan toggle.
 * - Clean Nearby Devices list with DeviceCardItem (category icon, distance, proximity badge, Find button).
 */
@Composable
fun BleScreen(
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BleViewModel
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var permissionRequestAttempted by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.isNotEmpty() && results.values.all { it }) {
            permissionDenied = false
            viewModel.startScan()
        } else {
            permissionDenied = true
        }
    }

    // Automatically request permissions and start scanning on first launch
    LaunchedEffect(Unit) {
        if (BlePermissionHelper.hasRequiredPermissions(context)) {
            viewModel.startScan()
        } else if (!permissionRequestAttempted) {
            permissionRequestAttempted = true
            permissionLauncher.launch(BlePermissionHelper.getRequiredPermissions())
        }
    }

    val errorMessage = state.error?.let { error ->
        when (error) {
            BleError.BluetoothNotSupported -> "Bluetooth LE is not supported on this device."
            BleError.BluetoothDisabled -> "Bluetooth is turned off. Please enable Bluetooth to scan."
            is BleError.PermissionDenied -> "Nearby Devices permission is required to scan."
            is BleError.ScanFailed -> error.message
            is BleError.ConnectionFailed -> error.reason
            is BleError.DeviceNotFound -> "Device not found: ${error.address}"
            is BleError.OperationFailed -> error.message
        }
    }

    val statusMessage = when {
        permissionDenied -> "Nearby Devices permission is required."
        state.userMessage != null -> state.userMessage
        else -> errorMessage
    }

    // Determine if user has scrolled past the top banner card
    val bannerCardIndex = if (statusMessage != null) 1 else 0
    val isScrolledPastCard by remember(statusMessage) {
        derivedStateOf {
            listState.firstVisibleItemIndex > bannerCardIndex ||
                    (listState.firstVisibleItemIndex == bannerCardIndex && listState.firstVisibleItemScrollOffset > 320)
        }
    }

    val onToggleScan = {
        if (state.isScanning) {
            viewModel.stopScan()
        } else {
            if (BlePermissionHelper.hasRequiredPermissions(context)) {
                permissionDenied = false
                viewModel.startScan()
            } else {
                permissionRequestAttempted = true
                permissionLauncher.launch(BlePermissionHelper.getRequiredPermissions())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Bar: "BLE Tracker" + STOP/SCAN pill (visible ONLY on scroll) + Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                        append("BLE ")
                    }
                    withStyle(style = SpanStyle(color = SecondaryRose, fontWeight = FontWeight.Bold)) {
                        append("Tracker")
                    }
                },
                fontSize = 22.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // STOP/SCAN pill button in AppBar (visible when top card scrolls off)
                AnimatedVisibility(
                    visible = isScrolledPastCard,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PrimaryContainer)
                            .clickable { onToggleScan() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (state.isScanning) "Stop" else "Scan",
                                tint = Wine,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isScanning) "STOP" else "SCAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Wine
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Scrollable content: Top Blue Card scrolls away naturally with the list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Optional status / warning banner (e.g. Bluetooth turned off)
            if (statusMessage != null) {
                item(key = "status_banner") {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SignalRedBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Status",
                                tint = SignalRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusMessage,
                                fontSize = 13.sp,
                                color = SignalRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Top Banner Card: "Ready to Scan" with theme gradient & BleRadarVisual animation
            item(key = "banner_card") {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryBlue,
                                        Wine,
                                        PrimaryBlueDark
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.isScanning) "Searching Nearby..." else "Ready to Scan",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Discover nearby BLE devices instantly with live signal updates.",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.88f),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onToggleScan,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = PrimaryBlue
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (state.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (state.isScanning) "Stop Scanning" else "Start Scanning",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Animated Radar Wave Graphic from BLEDemo
                            BleRadarVisual(
                                isScanning = state.isScanning,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Section Header: "Nearby Devices"
            item(key = "section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Devices",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (state.devices.isNotEmpty()) {
                        Text(
                            text = "${state.devices.size} found",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Empty State or List of Devices
            if (state.devices.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.isScanning) {
                                CircularProgressIndicator(
                                    color = SecondaryRose,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Searching for nearby devices...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Keep your peripherals powered on and advertising",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            } else {
                                Text(
                                    text = "No devices found",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap 'Start Scanning' to discover nearby Bluetooth devices.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = state.devices,
                    key = { it.id }
                ) { device ->
                    DeviceCardItem(
                        device = device,
                        onFindClick = { selected ->
                            onDeviceClick(selected.id)
                        }
                    )
                }
            }
        }
    }
}
