package com.belectronicsvp.ui.dashboardlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import com.belectronicsvp.model.PanelOrientation
import com.belectronicsvp.ui.about.AboutDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.model.PanelModel
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.RedDisconnect
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardListScreen(
    viewModel: DashboardListViewModel,
    onOpenPanel: (String) -> Unit
) {
    val context = LocalContext.current
    val panels by viewModel.panels.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var panelToRename by remember { mutableStateOf<PanelModel?>(null) }
    var panelToDelete by remember { mutableStateOf<PanelModel?>(null) }

    // SAF launcher for importing .json / .bep files
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val json = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!json.isNullOrBlank()) {
                    val result = viewModel.importPanel(json)
                    if (result.isSuccess) {
                        Toast.makeText(context, "Panel imported: ${result.getOrNull()?.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to parse panel: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ViewQuilt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BElectronicsVP",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAboutDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About BElectronicsVP",
                            tint = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = { importFileLauncher.launch("application/json") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import Panel",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ElectricCyan,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Dashboard")
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (panels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No dashboards yet. Tap + to create one!",
                    color = TextSecondaryDark,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(panels, key = { it.id }) { panel ->
                    DashboardCard(
                        panel = panel,
                        onClick = { onOpenPanel(panel.id) },
                        onRename = { panelToRename = panel },
                        onClone = {
                            viewModel.clonePanel(panel.id)
                            Toast.makeText(context, "Duplicated ${panel.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { panelToDelete = panel },
                        onExport = {
                            sharePanel(context, panel, viewModel.getExportJson(panel.id))
                        }
                    )
                }
            }
        }
    }

    // Create Dialog
    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var selectedOrientation by remember { mutableStateOf(PanelOrientation.LANDSCAPE) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Dashboard") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Dashboard Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Screen Orientation",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedOrientation == PanelOrientation.LANDSCAPE,
                            onClick = { selectedOrientation = PanelOrientation.LANDSCAPE },
                            label = { Text("Horizontal") },
                            leadingIcon = if (selectedOrientation == PanelOrientation.LANDSCAPE) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedOrientation == PanelOrientation.PORTRAIT,
                            onClick = { selectedOrientation = PanelOrientation.PORTRAIT },
                            label = { Text("Vertical") },
                            leadingIcon = if (selectedOrientation == PanelOrientation.PORTRAIT) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val created = viewModel.createPanel(
                                name = newName.trim(),
                                description = newDesc.trim(),
                                orientation = selectedOrientation
                            )
                            showCreateDialog = false
                            onOpenPanel(created.id)
                        }
                    }
                ) {
                    Text("Create", color = ElectricCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    panelToRename?.let { panel ->
        var updatedName by remember { mutableStateOf(panel.name) }
        AlertDialog(
            onDismissRequest = { panelToRename = null },
            title = { Text("Rename Dashboard") },
            text = {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = { Text("Dashboard Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (updatedName.isNotBlank()) {
                            viewModel.renamePanel(panel.id, updatedName.trim())
                        }
                        panelToRename = null
                    }
                ) {
                    Text("Save", color = ElectricCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { panelToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    panelToDelete?.let { panel ->
        AlertDialog(
            onDismissRequest = { panelToDelete = null },
            title = { Text("Delete Dashboard") },
            text = { Text("Are you sure you want to delete '${panel.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePanel(panel.id)
                        panelToDelete = null
                    }
                ) {
                    Text("Delete", color = RedDisconnect)
                }
            },
            dismissButton = {
                TextButton(onClick = { panelToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun DashboardCard(
    panel: PanelModel,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = panel.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimaryDark
                )
                if (panel.description.isNotBlank()) {
                    Text(
                        text = panel.description,
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "${panel.widgets.size} widgets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (panel.orientation == PanelOrientation.LANDSCAPE) "Horizontal" else "Vertical",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Density: ${panel.baseDensity}",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondaryDark
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onClone()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share / Export") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = RedDisconnect) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RedDisconnect) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun sharePanel(context: Context, panel: PanelModel, json: String?) {
    if (json.isNullOrBlank()) {
        Toast.makeText(context, "Failed to prepare export", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "BElectronicsVP Dashboard: ${panel.name}")
            putExtra(Intent.EXTRA_TEXT, json)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Dashboard (${panel.name})"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
