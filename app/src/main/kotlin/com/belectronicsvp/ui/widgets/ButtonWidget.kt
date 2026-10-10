package com.belectronicsvp.ui.widgets

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.TextPrimaryDark

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ButtonWidget(
    widget: WidgetModel.Button,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onSendCommand: (String) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLatchedOn by remember { mutableStateOf(false) }

    val activeColor = ElectricCyan
    val inactiveColor = DarkSurfaceVariant
    val backgroundColor = if (widget.isMomentary) {
        if (isPressed) activeColor.copy(alpha = 0.35f) else inactiveColor
    } else {
        if (isLatchedOn) activeColor.copy(alpha = 0.35f) else inactiveColor
    }

    val borderColor = if (isPressed || (!widget.isMomentary && isLatchedOn)) {
        activeColor
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .then(
                if (!isEditMode) {
                    Modifier.pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isPressed = true
                                if (widget.isMomentary) {
                                    onSendCommand(widget.pressCommand)
                                } else {
                                    isLatchedOn = !isLatchedOn
                                    val cmd = if (isLatchedOn) widget.pressCommand else (widget.releaseCommand ?: "")
                                    if (cmd.isNotEmpty()) onSendCommand(cmd)
                                }
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isPressed = false
                                if (widget.isMomentary) {
                                    widget.releaseCommand?.let { relCmd ->
                                        if (relCmd.isNotEmpty()) onSendCommand(relCmd)
                                    }
                                }
                                true
                            }
                            else -> false
                        }
                    }
                } else Modifier
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = widget.label,
            color = if (isPressed || (!widget.isMomentary && isLatchedOn)) ElectricCyan else TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
