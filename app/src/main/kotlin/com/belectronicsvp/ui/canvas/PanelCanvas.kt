package com.belectronicsvp.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.belectronicsvp.model.PanelModel
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.widgets.ButtonWidget
import com.belectronicsvp.ui.widgets.GaugeWidget
import com.belectronicsvp.ui.widgets.GraphWidget
import com.belectronicsvp.ui.widgets.SliderWidget
import com.belectronicsvp.ui.widgets.SwitchWidget
import com.belectronicsvp.ui.widgets.TextDisplayWidget
import kotlin.math.roundToInt

@Composable
fun PanelCanvas(
    panel: PanelModel,
    isEditMode: Boolean,
    telemetryNumeric: Map<Char, Double>,
    telemetryText: Map<Char, String>,
    telemetryGraphs: Map<Char, List<Double>>,
    modifier: Modifier = Modifier,
    onSendCommand: (String) -> Unit,
    onWidgetMoved: (widgetId: String, newX: Float, newY: Float) -> Unit = { _, _, _ -> },
    onWidgetSelected: (WidgetModel) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val cols = if (panel.gridColumns > 0) panel.gridColumns else 12
        val rows = if (panel.gridRows > 0) panel.gridRows else 8

        val cellWidth = availableWidth / cols
        val cellHeight = availableHeight / rows

        // Draw grid overlay when in Edit Mode
        if (isEditMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                for (c in 0..cols) {
                    val x = c * (w / cols)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                for (r in 0..rows) {
                    val y = r * (h / rows)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // Render each widget positioned dynamically according to grid coordinates
        panel.widgets.forEach { widget ->
            val initialX = widget.x * cellWidth.value
            val initialY = widget.y * cellHeight.value
            val widgetW = (widget.width * cellWidth.value).dp
            val widgetH = (widget.height * cellHeight.value).dp

            var dragOffsetX by remember(widget.id, widget.x) { mutableFloatStateOf(0f) }
            var dragOffsetY by remember(widget.id, widget.y) { mutableFloatStateOf(0f) }

            val totalX = initialX + dragOffsetX
            val totalY = initialY + dragOffsetY

            Box(
                modifier = Modifier
                    .offset { IntOffset(totalX.roundToInt(), totalY.roundToInt()) }
                    .size(widgetW, widgetH)
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .pointerInput(widget.id) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetX += dragAmount.x
                                            dragOffsetY += dragAmount.y
                                        },
                                        onDragEnd = {
                                            val currentPxX = initialX + dragOffsetX
                                            val currentPxY = initialY + dragOffsetY
                                            val finalCellX = (currentPxX / cellWidth.value)
                                                .roundToInt()
                                                .coerceIn(0, (cols - widget.width.toInt()).coerceAtLeast(0))
                                                .toFloat()
                                            val finalCellY = (currentPxY / cellHeight.value)
                                                .roundToInt()
                                                .coerceIn(0, (rows - widget.height.toInt()).coerceAtLeast(0))
                                                .toFloat()

                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                            onWidgetMoved(widget.id, finalCellX, finalCellY)
                                        }
                                    )
                                }
                        } else Modifier
                    )
            ) {
                when (widget) {
                    is WidgetModel.Button -> {
                        ButtonWidget(
                            widget = widget,
                            isEditMode = isEditMode,
                            onSendCommand = onSendCommand,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is WidgetModel.Slider -> {
                        SliderWidget(
                            widget = widget,
                            isEditMode = isEditMode,
                            onSendCommand = onSendCommand,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is WidgetModel.Switch -> {
                        SwitchWidget(
                            widget = widget,
                            isEditMode = isEditMode,
                            onSendCommand = onSendCommand,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is WidgetModel.Gauge -> {
                        GaugeWidget(
                            widget = widget,
                            currentValue = telemetryNumeric[widget.telemetryId],
                            isEditMode = isEditMode,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is WidgetModel.TextDisplay -> {
                        TextDisplayWidget(
                            widget = widget,
                            currentText = telemetryText[widget.telemetryId],
                            isEditMode = isEditMode,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is WidgetModel.Graph -> {
                        GraphWidget(
                            widget = widget,
                            points = telemetryGraphs[widget.telemetryId] ?: emptyList(),
                            isEditMode = isEditMode,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (isEditMode) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to reposition",
                        tint = ElectricCyan,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}
