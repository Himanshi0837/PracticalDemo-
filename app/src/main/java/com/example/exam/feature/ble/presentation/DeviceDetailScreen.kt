package com.example.exam.feature.ble.presentation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.exam.core.bluetooth.BleState
import com.example.exam.core.bluetooth.signal.DistanceEstimator
import com.example.exam.feature.ble.presentation.components.DeviceGaugeMeter
import com.example.exam.ui.theme.AppBackground
import com.example.exam.ui.theme.AppSurface
import com.example.exam.ui.theme.OnPrimaryContainer
import com.example.exam.ui.theme.PrimaryContainer
import com.example.exam.ui.theme.SecondaryRose
import com.example.exam.ui.theme.SignalAmber
import com.example.exam.ui.theme.SignalAmberBg
import com.example.exam.ui.theme.SignalGreen
import com.example.exam.ui.theme.SignalGreenBg
import com.example.exam.ui.theme.SignalRed
import com.example.exam.ui.theme.SignalRedBg
import com.example.exam.ui.theme.TextPrimary
import com.example.exam.ui.theme.TextSecondary
import com.example.exam.ui.theme.Wine

/**
 * Device Detail / Proximity Speedometer Screen matching the exact design and logic of BLEDemo.
 * Continuously fetches and updates RSSI, distance, speedometer gauge, and GATT connection state.
 */
@SuppressLint("DefaultLocale")
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BleViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isConnected = state.connectionState is BleState.Connected ||
            state.connectionState is BleState.DiscoveringServices
    val isConnecting = state.connectionState is BleState.Connecting

    // Find the device from the discovered list or fallback
    val device = state.devices.find { it.id == deviceId }
    val deviceName = device?.name ?: "BLE Device"

    // Connect to device via GATT upon opening this screen, track continuously, disconnect on exit
    DisposableEffect(deviceId) {
        viewModel.connectDevice(deviceId)
        onDispose {
            viewModel.stopTracking()
        }
    }

    // Intercept system back gesture
    BackHandler(enabled = true) {
        onBackClick()
    }

    // Live continuous distance calculation from real stable filtered RSSI
    val currentRssi = state.trackedDeviceRssi ?: (device?.filteredRssi ?: -70.0)
    val currentDistance = DistanceEstimator.calculateDistance(currentRssi)
    val proximityLevel = DistanceEstimator.getProximityLevel(currentDistance)


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Bar: Back button + Device Title + Info Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppSurface)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Wine,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Text(
                text = deviceName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Speedometer Gauge with semi-circular gradient arc and animated needle
        DeviceGaugeMeter(
            distanceMeters = currentDistance,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Proximity Banner Pill matching BLEDemo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryContainer)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = proximityLevel.guidanceMessage,
                fontSize = 14.sp,
                color = OnPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Live Tracking Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Tracking Direction",
                        tint = Wine,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Live Tracking",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Move toward the stronger indicator",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Real Live Signal Metrics Card matching BLEDemo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Signal Metrics",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Device MAC:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = deviceId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Raw Antenna Signal:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${device?.rawRssi ?: currentRssi.toInt()} dBm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryRose
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kalman Smoothed RSSI:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format("%.1f", currentRssi)} dBm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SignalGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. GATT Connection Status & Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val (statusText, statusColor, statusBg) = when (val conn = state.connectionState) {
                    is BleState.Connecting -> Triple("Connecting...", SignalAmber, SignalAmberBg)
                    is BleState.DiscoveringServices -> Triple("Discovering Services...", SignalAmber, SignalAmberBg)
                    is BleState.Connected -> Triple("Connected", SignalGreen, SignalGreenBg)
                    is BleState.Disconnected -> Triple("Disconnected", Color(0xFF64748B), Color(0xFFF1F5F9))
                    is BleState.Error -> Triple("Error: ${conn.error}", SignalRed, SignalRedBg)
                    else -> Triple("Idle", Color(0xFF64748B), Color(0xFFF1F5F9))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GATT Connection",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    color = statusColor,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.connectDevice(deviceId) },
                        enabled = !isConnected && !isConnecting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (isConnecting) "Connecting..." else "Connect",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.disconnectDevice() },
                        enabled = isConnected || isConnecting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Disconnect",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
