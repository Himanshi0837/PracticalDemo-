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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.exam.ui.theme.PrimaryBlue
import com.example.exam.ui.theme.Wine

/**
 * Animated radar wave visual used in the 'Ready to Scan' banner card matching BLEDemo.
 */
@Composable
fun BleRadarVisual(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseFraction"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerOffset = center
            val maxRadius = size.minDimension / 1.8f

            // Static background concentric rings
            val ringCount = 3
            for (i in 1..ringCount) {
                val radius = maxRadius * (i.toFloat() / ringCount)
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Animated pulsing wave when scanning
            if (isScanning) {
                val animatedRadius = maxRadius * pulseFraction
                val animatedAlpha = (1f - pulseFraction).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = Color.White.copy(alpha = animatedAlpha),
                    radius = animatedRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        // Center glowing Bluetooth beacon hub
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth Active",
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

