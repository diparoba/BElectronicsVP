package com.belectronicsvp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.RedDisconnect
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark

@Composable
fun WidgetConfigDialog(
    widget: WidgetModel,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    when (widget) {
        is WidgetModel.Button -> ButtonConfigContent(widget, onSave, onDelete, onDismiss)
        is WidgetModel.Slider -> SliderConfigContent(widget, onSave, onDelete, onDismiss)
        is WidgetModel.Switch -> SwitchConfigContent(widget, onSave, onDelete, onDismiss)
        is WidgetModel.Gauge -> GaugeConfigContent(widget, onSave, onDelete, onDismiss)
        is WidgetModel.TextDisplay -> TextDisplayConfigContent(widget, onSave, onDelete, onDismiss)
        is WidgetModel.Graph -> GraphConfigContent(widget, onSave, onDelete, onDismiss)
    }
}

@Composable
private fun ButtonConfigContent(
    widget: WidgetModel.Button,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var pressCommand by remember { mutableStateOf(widget.pressCommand) }
    var releaseCommand by remember { mutableStateOf(widget.releaseCommand ?: "") }
    var isMomentary by remember { mutableStateOf(widget.isMomentary) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Button Widget",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            onSave(
                widget.copy(
                    label = label.trim(),
                    pressCommand = pressCommand,
                    releaseCommand = releaseCommand.ifEmpty { null },
                    isMomentary = isMomentary,
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Button Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Button Mode:", fontSize = 12.sp, color = TextSecondaryDark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isMomentary,
                onClick = { isMomentary = true },
                label = { Text("Momentary (Push)") },
                colors = customFilterChipColors(),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !isMomentary,
                onClick = { isMomentary = false },
                label = { Text("Toggle (Latch)") },
                colors = customFilterChipColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pressCommand,
            onValueChange = { pressCommand = it },
            label = { Text(if (isMomentary) "Press Command (Touch Down)" else "ON Command") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = releaseCommand,
            onValueChange = { releaseCommand = it },
            label = { Text(if (isMomentary) "Release Command (Touch Up, optional)" else "OFF Command") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun SliderConfigContent(
    widget: WidgetModel.Slider,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var prefix by remember { mutableStateOf(widget.commandPrefix) }
    var suffix by remember { mutableStateOf(widget.commandSuffix) }
    var minVal by remember { mutableIntStateOf(widget.minValue) }
    var maxVal by remember { mutableIntStateOf(widget.maxValue) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Slider Widget",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            onSave(
                widget.copy(
                    label = label.trim(),
                    commandPrefix = prefix,
                    commandSuffix = suffix,
                    minValue = minVal,
                    maxValue = maxVal.coerceAtLeast(minVal + 1),
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Slider Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = minVal.toString(),
                onValueChange = { minVal = it.toIntOrNull() ?: 0 },
                label = { Text("Min Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxVal.toString(),
                onValueChange = { maxVal = it.toIntOrNull() ?: 100 },
                label = { Text("Max Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text("Prefix (e.g. V: )") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = suffix,
                onValueChange = { suffix = it },
                label = { Text("Suffix (e.g. \\n)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live preview of transmitted command
        val sampleVal = (minVal + maxVal) / 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground, RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Text("Command Output Preview:", fontSize = 11.sp, color = TextSecondaryDark)
                Text(
                    text = "${prefix}${sampleVal}${suffix}".replace("\n", "\\n").replace("\r", "\\r"),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ElectricCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun SwitchConfigContent(
    widget: WidgetModel.Switch,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var onCommand by remember { mutableStateOf(widget.onCommand) }
    var offCommand by remember { mutableStateOf(widget.offCommand) }
    var initialState by remember { mutableStateOf(widget.initialState) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Switch Widget",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            onSave(
                widget.copy(
                    label = label.trim(),
                    onCommand = onCommand,
                    offCommand = offCommand,
                    initialState = initialState,
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Switch Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = onCommand,
                onValueChange = { onCommand = it },
                label = { Text("ON Command") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = offCommand,
                onValueChange = { offCommand = it },
                label = { Text("OFF Command") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Initial State (ON / OFF):", fontSize = 13.sp, color = TextPrimaryDark)
            Switch(
                checked = initialState,
                onCheckedChange = { initialState = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricCyan,
                    checkedTrackColor = ElectricCyan.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun GaugeConfigContent(
    widget: WidgetModel.Gauge,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var telemetryId by remember { mutableStateOf(widget.telemetryId.toString()) }
    var minVal by remember { mutableDoubleStateOf(widget.minValue) }
    var maxVal by remember { mutableDoubleStateOf(widget.maxValue) }
    var unit by remember { mutableStateOf(widget.unit) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Gauge Widget",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            val charId = telemetryId.trim().firstOrNull()?.uppercaseChar() ?: 'A'
            onSave(
                widget.copy(
                    label = label.trim(),
                    telemetryId = charId,
                    minValue = minVal,
                    maxValue = maxVal.coerceAtLeast(minVal + 0.1),
                    unit = unit.trim(),
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Gauge Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = telemetryId,
                onValueChange = { if (it.length <= 1) telemetryId = it.uppercase() },
                label = { Text("Telemetry ID (A-Z)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (e.g. %, V, °C)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = minVal.toString(),
                onValueChange = { minVal = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Min Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxVal.toString(),
                onValueChange = { maxVal = it.toDoubleOrNull() ?: 100.0 },
                label = { Text("Max Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun TextDisplayConfigContent(
    widget: WidgetModel.TextDisplay,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var telemetryId by remember { mutableStateOf(widget.telemetryId.toString()) }
    var defaultText by remember { mutableStateOf(widget.defaultText) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Text Display",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            val charId = telemetryId.trim().firstOrNull()?.uppercaseChar() ?: 'T'
            onSave(
                widget.copy(
                    label = label.trim(),
                    telemetryId = charId,
                    defaultText = defaultText,
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Display Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = telemetryId,
                onValueChange = { if (it.length <= 1) telemetryId = it.uppercase() },
                label = { Text("Telemetry ID (A-Z)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = defaultText,
                onValueChange = { defaultText = it },
                label = { Text("Default Placeholder") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun GraphConfigContent(
    widget: WidgetModel.Graph,
    onSave: (WidgetModel) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(widget.label) }
    var telemetryId by remember { mutableStateOf(widget.telemetryId.toString()) }
    var minVal by remember { mutableDoubleStateOf(widget.minValue) }
    var maxVal by remember { mutableDoubleStateOf(widget.maxValue) }
    var maxPoints by remember { mutableIntStateOf(widget.maxDataPoints) }
    var width by remember { mutableFloatStateOf(widget.width) }
    var height by remember { mutableFloatStateOf(widget.height) }

    BaseConfigDialog(
        title = "Configure Graph Widget",
        onDismiss = onDismiss,
        onDelete = { onDelete(widget.id) },
        onSave = {
            val charId = telemetryId.trim().firstOrNull()?.uppercaseChar() ?: 'G'
            onSave(
                widget.copy(
                    label = label.trim(),
                    telemetryId = charId,
                    minValue = minVal,
                    maxValue = maxVal.coerceAtLeast(minVal + 0.1),
                    maxDataPoints = maxPoints.coerceIn(10, 200),
                    width = width,
                    height = height
                )
            )
        }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Graph Label") },
            colors = customTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = telemetryId,
                onValueChange = { if (it.length <= 1) telemetryId = it.uppercase() },
                label = { Text("Telemetry ID (A-Z)") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxPoints.toString(),
                onValueChange = { maxPoints = it.toIntOrNull() ?: 50 },
                label = { Text("Buffer Points") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = minVal.toString(),
                onValueChange = { minVal = it.toDoubleOrNull() ?: 0.0 },
                label = { Text("Min Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxVal.toString(),
                onValueChange = { maxVal = it.toDoubleOrNull() ?: 100.0 },
                label = { Text("Max Value") },
                colors = customTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DimensionSteppers(
            width = width.toInt(),
            height = height.toInt(),
            onWidthChange = { width = it.toFloat() },
            onHeightChange = { height = it.toFloat() }
        )
    }
}

@Composable
private fun DimensionSteppers(
    width: Int,
    height: Int,
    onWidthChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Grid Cell Dimensions:", fontSize = 12.sp, color = TextSecondaryDark)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepperItem(
                label = "Width",
                value = width,
                min = 1,
                max = 24,
                onValueChange = onWidthChange,
                modifier = Modifier.weight(1f)
            )
            StepperItem(
                label = "Height",
                value = height,
                min = 1,
                max = 24,
                onValueChange = onHeightChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepperItem(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ElectricCyan, modifier = Modifier.size(16.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = TextSecondaryDark)
            Text(text = "$value cells", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        }
        IconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = ElectricCyan, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BaseConfigDialog(
    title: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimaryDark
                )
                IconButton(onClick = {
                    onDelete()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Widget",
                        tint = RedDisconnect
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                content()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Save", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = TextSecondaryDark,
    cursorColor = ElectricCyan,
    focusedTextColor = TextPrimaryDark,
    unfocusedTextColor = TextPrimaryDark
)

@Composable
private fun customFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
    selectedLabelColor = ElectricCyan,
    containerColor = DarkSurfaceVariant,
    labelColor = TextSecondaryDark
)
