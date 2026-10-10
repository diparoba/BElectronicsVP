package com.belectronicsvp.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.WidgetModel
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCatalogSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectWidget: (WidgetModel) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Add Widget to Dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Select a control or telemetry widget to place onto the 1:1 grid",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                item {
                    CatalogCategoryHeader("Control Widgets")
                }
                item {
                    CatalogItem(
                        icon = Icons.Default.SmartButton,
                        title = "Push / Toggle Button",
                        description = "Transmits commands on touch press and release (Momentary or Latch)",
                        onClick = {
                            val id = "btn_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.Button(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 3f,
                                    height = 2f,
                                    label = "Button",
                                    pressCommand = "1",
                                    releaseCommand = "0",
                                    isMomentary = true
                                )
                            )
                        }
                    )
                }
                item {
                    CatalogItem(
                        icon = Icons.Default.LinearScale,
                        title = "Range Slider",
                        description = "Adjustable slider with custom min/max range and command prefix (e.g. V: 5)",
                        onClick = {
                            val id = "sld_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.Slider(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 6f,
                                    height = 2f,
                                    label = "Slider",
                                    commandPrefix = "V: ",
                                    commandSuffix = "\n",
                                    minValue = 0,
                                    maxValue = 9,
                                    initialValue = 0
                                )
                            )
                        }
                    )
                }
                item {
                    CatalogItem(
                        icon = Icons.Default.ToggleOn,
                        title = "Toggle Switch",
                        description = "Two-state ON/OFF switch sending discrete commands (e.g. H / L)",
                        onClick = {
                            val id = "swt_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.Switch(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 3f,
                                    height = 2f,
                                    label = "Switch",
                                    onCommand = "H",
                                    offCommand = "L",
                                    initialState = false
                                )
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    CatalogCategoryHeader("Telemetry & Readout Widgets")
                }
                item {
                    CatalogItem(
                        icon = Icons.Default.AvTimer,
                        title = "Dial Gauge",
                        description = "Radial analog meter listening to incoming telemetry (*A<val>*)",
                        onClick = {
                            val id = "gag_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.Gauge(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 4f,
                                    height = 4f,
                                    label = "Gauge",
                                    telemetryId = 'A',
                                    minValue = 0.0,
                                    maxValue = 100.0,
                                    unit = "%"
                                )
                            )
                        }
                    )
                }
                item {
                    CatalogItem(
                        icon = Icons.Default.Code,
                        title = "Text Display",
                        description = "Digital readout or message console listening to telemetry (*T<text>*)",
                        onClick = {
                            val id = "txt_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.TextDisplay(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 4f,
                                    height = 2f,
                                    label = "Display",
                                    telemetryId = 'T',
                                    defaultText = "--"
                                )
                            )
                        }
                    )
                }
                item {
                    CatalogItem(
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        title = "Waveform Graph",
                        description = "Real-time scrolling oscilloscope chart listening to telemetry (*G<val>*)",
                        onClick = {
                            val id = "grp_${UUID.randomUUID().toString().take(6)}"
                            onSelectWidget(
                                WidgetModel.Graph(
                                    id = id,
                                    x = 0f,
                                    y = 0f,
                                    width = 6f,
                                    height = 4f,
                                    label = "Graph",
                                    telemetryId = 'G',
                                    maxDataPoints = 50,
                                    minValue = 0.0,
                                    maxValue = 100.0
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogCategoryHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ElectricCyan,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun CatalogItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
