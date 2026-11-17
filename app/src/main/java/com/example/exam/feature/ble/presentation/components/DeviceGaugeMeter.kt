package com.example.exam.feature.ble.presentation.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exam.core.bluetooth.signal.DistanceEstimator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Semi-circular Speedometer / Proximity Gauge Meter matching the design in Image 2.
 * Includes three segments (Near, Closer, Far), smooth gradient arc, and animated needle.
 */
@Composable
fun DeviceGaugeMeter(
    distanceMeters: Double,
    modifier: Modifier = Modifier
) {
    val fraction = DistanceEstimator.calculateGaugeFraction(distanceMeters)

    // Needle rotates from -165° (Near) to -15° (Far), with straight up being -90° (Closer)
    val targetAngle = -165f + (fraction * 150f)
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NeedleRotation"
    )

    // Active zone: 0 = Near, 1 = Closer, 2 = Far
    val activeZone = when {
        distanceMeters < 2.0 -> 0
        distanceMeters <= 5.0 -> 1
        else -> 2
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 180.dp)
                .padding(top = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val strokeWidth = 36.dp.toPx()
                val diameter = size.width - strokeWidth
                val radius = diameter / 2f

                // Center point at bottom middle of arc
                val centerOffset = Offset(size.width / 2f, size.height - 10.dp.toPx())
                val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                // 1. Draw gradient semi-circular track (Near: Deep Blue -> Far: Pale Blue)
                val arcBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF2563EB), // Deep Blue (Near)
                        Color(0xFF38BDF8), // Light Blue (Closer)
                        Color(0xFFBAE6FD)  // Pale Blue (Far)
                    ),
                    startX = arcTopLeft.x,
                    endX = arcTopLeft.x + arcSize.width
                )

                drawArc(
                    brush = arcBrush,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Draw subtle white separator tick marks at -120° and -60°
                val separatorAngles = listOf(-120.0, -60.0)
                for (deg in separatorAngles) {
                    val rad = Math.toRadians(deg)
                    val innerR = radius - (strokeWidth / 2f)
                    val outerR = radius + (strokeWidth / 2f)
                    val start = Offset(
                        centerOffset.x + (innerR * cos(rad)).toFloat(),
                        centerOffset.y + (innerR * sin(rad)).toFloat()
                    )
                    val end = Offset(
                        centerOffset.x + (outerR * cos(rad)).toFloat(),
                        centerOffset.y + (outerR * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = Color.White,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // 3. Draw text labels: Near, Closer, Far with dynamic highlight
                val createPaint = { isActive: Boolean ->
                    Paint().apply {
                        color = if (isActive) android.graphics.Color.parseColor("#2563EB") else android.graphics.Color.DKGRAY
                        textSize = (if (isActive) 13.sp else 11.5.sp).toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = Typeface.create(
                            Typeface.DEFAULT,
                            if (isActive) Typeface.BOLD else Typeface.NORMAL
                        )
                    }
                }

                val textRadius = radius - (strokeWidth / 1.1f) - 6.dp.toPx()
                // Near label (~ -155°)
                val nearRad = Math.toRadians(-150.0)
                drawContext.canvas.nativeCanvas.drawText(
                    "◀ Near",
                    centerOffset.x + (textRadius * cos(nearRad)).toFloat(),
                    centerOffset.y + (textRadius * sin(nearRad)).toFloat(),
                    createPaint(activeZone == 0)
                )

                // Closer label (~ -90°)
                val closerRad = Math.toRadians(-90.0)
                drawContext.canvas.nativeCanvas.drawText(
                    "▲ Closer",
                    centerOffset.x + (textRadius * cos(closerRad)).toFloat(),
                    centerOffset.y + (textRadius * sin(closerRad)).toFloat() - 4.dp.toPx(),
                    createPaint(activeZone == 1)
                )

                // Far label (~ -30°)
                val farRad = Math.toRadians(-30.0)
                drawContext.canvas.nativeCanvas.drawText(
                    "Far ▶",
                    centerOffset.x + (textRadius * cos(farRad)).toFloat(),
                    centerOffset.y + (textRadius * sin(farRad)).toFloat(),
                    createPaint(activeZone == 2)
                )

                // 4. Draw Animated Needle
                val needleLength = radius - 8.dp.toPx()
                val needleRad = Math.toRadians(animatedAngle.toDouble())
                val needleTip = Offset(
                    centerOffset.x + (needleLength * cos(needleRad)).toFloat(),
                    centerOffset.y + (needleLength * sin(needleRad)).toFloat()
                )

                // Tapered needle path
                val needleBaseWidth = 6.dp.toPx()
                val perpRad = needleRad + Math.PI / 2
                val p1 = Offset(
                    centerOffset.x + (needleBaseWidth * cos(perpRad)).toFloat(),
                    centerOffset.y + (needleBaseWidth * sin(perpRad)).toFloat()
                )
                val p2 = Offset(
                    centerOffset.x - (needleBaseWidth * cos(perpRad)).toFloat(),
                    centerOffset.y - (needleBaseWidth * sin(perpRad)).toFloat()
                )

                val needlePath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(needleTip.x, needleTip.y)
                    lineTo(p2.x, p2.y)
                    close()
                }

                drawPath(needlePath, color = Color(0xFF111827))

                // Center black hub knob
                drawCircle(
                    color = Color(0xFF111827),
                    radius = 18.dp.toPx(),
                    center = centerOffset
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large bold distance readout e.g. "5.2 m"
        Text(
            text = String.format("%.1f m", distanceMeters),
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}
