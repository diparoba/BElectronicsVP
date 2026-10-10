package com.belectronicsvp.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.RedDisconnect
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeWidget(
    widget: WidgetModel.Gauge,
    currentValue: Double?,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false
) {
    val displayValue = (currentValue ?: widget.minValue).coerceIn(widget.minValue, widget.maxValue)
    val animatedValue by animateFloatAsState(
        targetValue = displayValue.toFloat(),
        animationSpec = tween(durationMillis = 200),
        label = "gauge_needle_anim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${widget.label} (*${widget.telemetryId})",
                color = TextSecondaryDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = minOf(size.width, size.height) * 0.85f
                    val arcSize = Size(diameter, diameter)
                    val arcTopLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = diameter / 2f

                    val startAngle = 135f
                    val sweepAngle = 270f
                    val strokeWidth = 8.dp.toPx()

                    // Background track
                    drawArc(
                        color = Color.White.copy(alpha = 0.15f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active progress arc
                    val ratio = ((animatedValue - widget.minValue) / (widget.maxValue - widget.minValue))
                        .coerceIn(0.0, 1.0).toFloat()
                    val activeSweep = sweepAngle * ratio
                    if (activeSweep > 0f) {
                        drawArc(
                            color = ElectricCyan,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Tick marks
                    val tickCount = 9
                    for (i in 0 until tickCount) {
                        val tickFraction = i.toFloat() / (tickCount - 1)
                        val angleDeg = startAngle + sweepAngle * tickFraction
                        val angleRad = angleDeg * (PI / 180.0)

                        val outerR = radius - strokeWidth / 2f
                        val innerR = outerR - (if (i % 2 == 0) 8.dp.toPx() else 4.dp.toPx())

                        val pOuter = Offset(
                            (center.x + outerR * cos(angleRad)).toFloat(),
                            (center.y + outerR * sin(angleRad)).toFloat()
                        )
                        val pInner = Offset(
                            (center.x + innerR * cos(angleRad)).toFloat(),
                            (center.y + innerR * sin(angleRad)).toFloat()
                        )

                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = pInner,
                            end = pOuter,
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Rotating Needle
                    val needleAngleDeg = startAngle + sweepAngle * ratio
                    val needleAngleRad = needleAngleDeg * (PI / 180.0)
                    val needleLength = radius * 0.75f

                    val needleEnd = Offset(
                        (center.x + needleLength * cos(needleAngleRad)).toFloat(),
                        (center.y + needleLength * sin(needleAngleRad)).toFloat()
                    )

                    drawLine(
                        color = RedDisconnect,
                        start = center,
                        end = needleEnd,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Pivot center circle
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = ElectricCyan,
                        radius = 2.5.dp.toPx(),
                        center = center
                    )
                }
            }

            Text(
                text = String.format(Locale.US, "%.1f %s", displayValue, widget.unit),
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
    }
}
