
package com.example.exam.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BubbleBackground(modifier: Modifier = Modifier) {

    val transition = rememberInfiniteTransition(label = "bubble_animation")

    val movement1 by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movement1"
    )

    val movement2 by transition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movement2"
    )

    val movement3 by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movement3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        fun drawBubble(
            x: Float,
            y: Float,
            radius: Float,
            movement: Float
        ) {
            val center = Offset(x, y + movement)

            // Soft bubble fill
            drawCircle(
                color = Color(0xFF78A9A1).copy(alpha = 0.16f),
                radius = radius,
                center = center
            )

            // Bubble outline
            drawCircle(
                color = Color(0xFF247477).copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Glass highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = radius * 0.13f,
                center = Offset(
                    center.x - radius * 0.35f,
                    center.y - radius * 0.35f
                )
            )
        }

        // Top section
        drawBubble(w * 0.88f, h * 0.20f, 28.dp.toPx(), movement1)
        drawBubble(w * 0.50f, h * 0.08f, 12.dp.toPx(), movement2)
        drawBubble(w * 0.08f, h * 0.30f, 15.dp.toPx(), movement3)

        // Around search field edges
        drawBubble(w * 0.07f, h * 0.59f, 38.dp.toPx(), movement2)
        drawBubble(w * 0.94f, h * 0.58f, 48.dp.toPx(), movement1)
        drawBubble(w * 0.83f, h * 0.43f, 13.dp.toPx(), movement3)

        // Category section
        drawBubble(w * 0.12f, h * 0.83f, 20.dp.toPx(), movement1)
        drawBubble(w * 0.75f, h * 0.88f, 30.dp.toPx(), movement2)
        drawBubble(w * 0.96f, h * 0.94f, 17.dp.toPx(), movement3)
        drawBubble(w * 0.42f, h * 0.96f, 11.dp.toPx(), movement1)
    }
}
