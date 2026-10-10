package com.belectronicsvp.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.OpenInFull
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.belectronicsvp.model.PanelModel
import com.belectronicsvp.model.PanelOrientation
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
    onWidgetResized: (widgetId: String, newWidth: Float, newHeight: Float) -> Unit = { _, _, _ -> },
    onWidgetSelected: (WidgetModel) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val density = LocalDensity.current
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val orientation = panel.orientation
        val baseDensity = if (panel.baseDensity > 0) panel.baseDensity else 8

        // Strict 1:1 Square Cell Engine:
        // Landscape: Base density defines rows, cell size S = Height / rows, cols = Width / S
        // Portrait:  Base density defines cols, cell size S = Width / cols, rows = Height / S
        val rows: Int
        val cols: Int
        val cellSizeDp = if (orientation == PanelOrientation.LANDSCAPE) {
            rows = baseDensity
            val size = availableHeight / rows
            cols = (availableWidth / size).toInt().coerceAtLeast(1)
            size
        } else {
            cols = baseDensity
            val size = availableWidth / cols
            rows = (availableHeight / size).toInt().coerceAtLeast(1)
            size
        }

        val cellSizePx = with(density) { cellSizeDp.toPx() }

        // Draw pixel-perfect grid overlay when in Edit Mode
        if (isEditMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalW = cols * cellSizePx
                val totalH = rows * cellSizePx

                for (c in 0..cols) {
                    val x = c * cellSizePx
                    drawLine(
                        color = Color.White.copy(alpha = 0.09f),
                        start = Offset(x, 0f),
                        end = Offset(x, totalH),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                for (r in 0..rows) {
                    val y = r * cellSizePx
                    drawLine(
                        color = Color.White.copy(alpha = 0.09f),
                        start = Offset(0f, y),
                        end = Offset(totalW, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // Render widgets mapped to strict 1:1 cells
        panel.widgets.forEach { widget ->
            val initialX = widget.x * cellSizePx
            val initialY = widget.y * cellSizePx

            var dragOffsetX by remember(widget.id, widget.x) { mutableFloatStateOf(0f) }
            var dragOffsetY by remember(widget.id, widget.y) { mutableFloatStateOf(0f) }

            var resizeDeltaW by remember(widget.id, widget.width) { mutableFloatStateOf(0f) }
            var resizeDeltaH by remember(widget.id, widget.height) { mutableFloatStateOf(0f) }

            val totalX = initialX + dragOffsetX
            val totalY = initialY + dragOffsetY

            val widgetW = ((widget.width * cellSizePx + resizeDeltaW) / density.density).dp
            val widgetH = ((widget.height * cellSizePx + resizeDeltaH) / density.density).dp

            Box(
                modifier = Modifier
                    .offset { IntOffset(totalX.roundToInt(), totalY.roundToInt()) }
                    .size(widgetW, widgetH)
                    .then(
                        if (isEditMode) {
                            Modifier
                                .border(1.5.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { onWidgetSelected(widget) }
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
                                            val finalCellX = (currentPxX / cellSizePx)
                                                .roundToInt()
                                                .coerceIn(0, (cols - widget.width.toInt()).coerceAtLeast(0))
                                                .toFloat()
                                            val finalCellY = (currentPxY / cellSizePx)
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
                // Render widget content
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

                // Edit Mode handles: Move indicator and Corner resize handle
                if (isEditMode) {
                    // Move handle icon in top-right
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to reposition",
                        tint = ElectricCyan,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    )

                    // Corner resize handle in bottom-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp))
                            .background(ElectricCyan.copy(alpha = 0.85f))
                            .pointerInput(widget.id) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        resizeDeltaW += dragAmount.x
                                        resizeDeltaH += dragAmount.y
                                    },
                                    onDragEnd = {
                                        val totalPxW = widget.width * cellSizePx + resizeDeltaW
                                        val totalPxH = widget.height * cellSizePx + resizeDeltaH
                                        val newWidthCells = (totalPxW / cellSizePx)
                                            .roundToInt()
                                            .coerceIn(1, cols - widget.x.toInt())
                                            .toFloat()
                                        val newHeightCells = (totalPxH / cellSizePx)
                                            .roundToInt()
                                            .coerceIn(1, rows - widget.y.toInt())
                                            .toFloat()

                                        resizeDeltaW = 0f
                                        resizeDeltaH = 0f
                                        onWidgetResized(widget.id, newWidthCells, newHeightCells)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Resize widget",
                            tint = DarkBackground,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
