package com.belectronicsvp.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark

@Composable
fun SliderWidget(
    widget: WidgetModel.Slider,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onSendCommand: (String) -> Unit
) {
    var currentValue by remember(widget.initialValue) {
        mutableFloatStateOf(widget.initialValue.toFloat())
    }
    var lastSendTimestamp by remember { mutableLongStateOf(0L) }

    fun sendSliderValue(value: Int, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (force || now - lastSendTimestamp >= 50) { // 50ms throttle
            lastSendTimestamp = now
            val formatted = "${widget.commandPrefix}$value${widget.commandSuffix}"
            onSendCommand(formatted)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = widget.label,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = currentValue.toInt().toString(),
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Slider(
                value = currentValue,
                onValueChange = { newVal ->
                    if (!isEditMode) {
                        currentValue = newVal
                        sendSliderValue(newVal.toInt(), force = false)
                    }
                },
                onValueChangeFinished = {
                    if (!isEditMode) {
                        sendSliderValue(currentValue.toInt(), force = true)
                    }
                },
                valueRange = widget.minValue.toFloat()..widget.maxValue.toFloat(),
                enabled = !isEditMode,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
