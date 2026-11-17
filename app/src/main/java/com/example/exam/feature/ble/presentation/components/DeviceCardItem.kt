package com.example.exam.feature.ble.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exam.core.bluetooth.model.BleDeviceCategory
import com.example.exam.core.bluetooth.model.BleDeviceModel
import com.example.exam.core.bluetooth.signal.ProximityLevel
import com.example.exam.ui.theme.AppBorder
import com.example.exam.ui.theme.AppSurface
import com.example.exam.ui.theme.PrimaryContainer
import com.example.exam.ui.theme.SignalAmber
import com.example.exam.ui.theme.SignalGreen
import com.example.exam.ui.theme.SignalRed
import com.example.exam.ui.theme.TextPrimary
import com.example.exam.ui.theme.TextSecondary
import com.example.exam.ui.theme.Wine

/**
 * Device Card Item matching BLEDemo's Nearby Devices list.
 */
@SuppressLint("DefaultLocale")
@Composable
fun DeviceCardItem(
    device: BleDeviceModel,
    onFindClick: (BleDeviceModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onFindClick(device) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Container
            val iconVector: ImageVector = when (device.category) {
                BleDeviceCategory.EARBUDS -> Icons.Default.Headphones
                BleDeviceCategory.WATCH -> Icons.Default.Watch
                BleDeviceCategory.FITNESS_BAND -> Icons.AutoMirrored.Filled.DirectionsRun
                BleDeviceCategory.BEACON -> Icons.Default.CellTower
                BleDeviceCategory.PHONE -> Icons.Default.Smartphone
                else -> Icons.Default.Bluetooth
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = device.name,
                    tint = Wine,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                val dotColor = when (device.proximity) {
                    ProximityLevel.VERY_CLOSE -> SignalGreen
                    ProximityLevel.GETTING_CLOSER -> SignalAmber
                    ProximityLevel.FAR_AWAY -> SignalRed
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${device.proximity.label} • ${String.format("%.1f", device.distanceMeters)}m",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Find Button
            OutlinedButton(
                onClick = { onFindClick(device) },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AppBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PrimaryContainer,
                    contentColor = Wine
                ),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 6.dp
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "Find",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

