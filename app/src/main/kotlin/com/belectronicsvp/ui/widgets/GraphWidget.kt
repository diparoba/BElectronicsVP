package com.belectronicsvp.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.TextSecondaryDark
import java.util.Locale

@Composable
fun GraphWidget(
    widget: WidgetModel.Graph,
    points: List<Double>,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false
) {
    val latestValue = points.lastOrNull() ?: widget.minValue

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${widget.label} (*${widget.telemetryId})",
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    text = String.format(Locale.US, "%.1f", latestValue),
                    color = ElectricCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkBackground)
                    .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val w = size.width
                    val h = size.height

                    // Grid lines (horizontal)
                    val hLines = 4
                    for (i in 0..hLines) {
                        val y = h * (i.toFloat() / hLines)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Grid lines (vertical)
                    val vLines = 6
                    for (i in 0..vLines) {
                        val x = w * (i.toFloat() / vLines)
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw waveform if points available
                    if (points.isNotEmpty()) {
                        val min = widget.minValue
                        val max = widget.maxValue
                        val range = if (max > min) max - min else 1.0

                        val path = Path()
                        val stepX = if (points.size > 1) w / (points.size - 1) else w

                        points.forEachIndexed { index, value ->
                            val clamped = value.coerceIn(min, max)
                            val normY = 1.0f - ((clamped - min) / range).toFloat()
                            val px = index * stepX
                            val py = normY * h

                            if (index == 0) {
                                path.moveTo(px, py)
                            } else {
                                path.lineTo(px, py)
                            }
                        }

                        drawPath(
                            path = path,
                            color = ElectricCyan,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}
