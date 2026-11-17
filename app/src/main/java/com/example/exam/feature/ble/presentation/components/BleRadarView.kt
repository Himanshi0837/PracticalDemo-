
package com.example.exam.feature.ble.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exam.core.bluetooth.model.BleDeviceModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val RadarBlue = Color(0xFF2563EB)
private val RadarGreen = Color(0xFF10B981)
private val RadarRing = Color(0xFFCBD5E1)

@Composable
fun BleRadarView(
    devices: List<BleDeviceModel>,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar")

    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp),
        contentAlignment = Alignment.Center
    ) {
        val radarSize = min(maxWidth.value, maxHeight.value).dp
        val radius = radarSize / 2f - 24.dp

        Canvas(
            modifier = Modifier.size(radarSize)
        ) {
            val center = Offset(
                x = size.width / 2f,
                y = size.height / 2f
            )

            val maxRadius = min(size.width, size.height) / 2f - 8.dp.toPx()

            // Radar rings
            for (index in 1..4) {
                drawCircle(
                    color = RadarRing,
                    radius = maxRadius * index / 4f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope
                        .Stroke(width = 1.dp.toPx())
                )
            }

            // Horizontal and vertical lines
            drawLine(
                color = RadarRing,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            drawLine(
                color = RadarRing,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Animated radar sweep
            val angleRadians = Math.toRadians(sweepAngle.toDouble())

            val end = Offset(
                x = center.x + maxRadius * cos(angleRadians).toFloat(),
                y = center.y + maxRadius * sin(angleRadians).toFloat()
            )

            drawLine(
                color = RadarGreen.copy(alpha = 0.75f),
                start = center,
                end = end,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Actual discovered BLE devices
        devices.forEach { device ->
            val angle = Math.floorMod(device.id.hashCode(), 360)
            val angleRadians = Math.toRadians(angle.toDouble())

            // Convert estimated distance into a radar radius.
            val distanceRatio =
                (device.distanceMeters / 8.0)
                    .coerceIn(0.15, 0.90)
                    .toFloat()

            val pointRadius = radius * distanceRatio

            val x = pointRadius * cos(angleRadians).toFloat()
            val y = pointRadius * sin(angleRadians).toFloat()

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = x.roundToPx(),
                            y = y.roundToPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((14 * pulse).dp)
                        .background(
                            color = RadarBlue.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(
                                color = RadarBlue,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Center represents the local phone
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(RadarBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ME",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}
