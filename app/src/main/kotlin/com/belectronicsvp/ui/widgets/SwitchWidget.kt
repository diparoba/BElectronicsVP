package com.belectronicsvp.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@Composable
fun SwitchWidget(
    widget: WidgetModel.Switch,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onSendCommand: (String) -> Unit
) {
    var isChecked by remember(widget.initialState) { mutableStateOf(widget.initialState) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
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
                fontSize = 13.sp
            )
            Switch(
                checked = isChecked,
                enabled = !isEditMode,
                onCheckedChange = { checked ->
                    isChecked = checked
                    val cmd = if (checked) widget.onCommand else widget.offCommand
                    if (cmd.isNotEmpty()) onSendCommand(cmd)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricCyan,
                    checkedTrackColor = ElectricCyan.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}
