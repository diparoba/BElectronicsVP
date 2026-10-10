package com.belectronicsvp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark

@Composable
fun GridDensityDialog(
    currentDensity: Int,
    onDensitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var density by remember { mutableIntStateOf(currentDensity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Grid Density Settings",
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Adjust the 1:1 square cell size across the canvas. Lower base density creates larger cells, higher density allows more compact widgets.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = density == 6,
                        onClick = { density = 6 },
                        label = { Text("Coarse (6)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = density == 8,
                        onClick = { density = 8 },
                        label = { Text("Standard (8)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = density == 12,
                        onClick = { density = 12 },
                        label = { Text("Fine (12)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Custom Base Density:", fontSize = 13.sp, color = TextPrimaryDark)
                    Text(text = "$density cells", fontWeight = FontWeight.Bold, color = ElectricCyan)
                }

                Slider(
                    value = density.toFloat(),
                    onValueChange = { density = it.toInt() },
                    valueRange = 4f..20f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDensitySelected(density)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Apply", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
