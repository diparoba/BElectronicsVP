package com.belectronicsvp.ui.dashboard

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.belectronicsvp.bluetooth.BluetoothPermissionHelper
import com.belectronicsvp.bluetooth.ConnectionState
import com.belectronicsvp.bluetooth.DeviceType
import com.belectronicsvp.bluetooth.DiscoveredDevice
import com.belectronicsvp.ui.canvas.PanelCanvas
import com.belectronicsvp.ui.theme.DarkBackground
import com.belectronicsvp.ui.theme.DarkSurface
import com.belectronicsvp.ui.theme.DarkSurfaceVariant
import com.belectronicsvp.ui.theme.ElectricCyan
import com.belectronicsvp.ui.theme.NeonGreen
import com.belectronicsvp.ui.theme.RedDisconnect
import com.belectronicsvp.ui.theme.TextPrimaryDark
import com.belectronicsvp.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDetailScreen(
    viewModel: DashboardDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val panel by viewModel.panel.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val activeDeviceName by viewModel.activeDeviceName.collectAsState()
    val lastDevice by viewModel.lastDevice.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    val telemetryNumeric by viewModel.telemetryNumeric.collectAsState()
    val telemetryText by viewModel.telemetryText.collectAsState()
    val telemetryGraphs by viewModel.telemetryGraphs.collectAsState()

    var showDevicePicker by remember { mutableStateOf(false) }
    var isBleScanning by remember { mutableStateOf(false) }
    val discoveredBleDevices = remember { mutableStateListOf<DiscoveredDevice>() }
    val pairedDevices = remember { mutableStateListOf<DiscoveredDevice>() }

    val bluetoothManager = remember {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    val bluetoothAdapter = bluetoothManager?.adapter

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            refreshPairedDevices(context, bluetoothAdapter, pairedDevices)
        } else {
            Toast.makeText(context, "Bluetooth permissions required to connect", Toast.LENGTH_SHORT).show()
        }
    }

    fun ensurePermissionsAndOpenPicker() {
        if (BluetoothPermissionHelper.hasAllPermissions(context)) {
            refreshPairedDevices(context, bluetoothAdapter, pairedDevices)
            showDevicePicker = true
        } else {
            permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
        }
    }

    // BLE Scanner Callback
    val bleScanCallback = remember {
        object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { dev ->
                    if (!discoveredBleDevices.any { it.address == dev.address }) {
                        val name = dev.name ?: "BLE Device"
                        discoveredBleDevices.add(
                            DiscoveredDevice(
                                name = name,
                                address = dev.address,
                                type = DeviceType.BLE,
                                rssi = result.rssi
                            )
                        )
                    }
                }
            }
        }
    }

    fun startBleScan() {
        if (!BluetoothPermissionHelper.hasAllPermissions(context)) {
            permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
            return
        }
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Toast.makeText(context, "BLE scanner not available", Toast.LENGTH_SHORT).show()
            return
        }
        discoveredBleDevices.clear()
        isBleScanning = true
        try {
            scanner.startScan(bleScanCallback)
        } catch (e: SecurityException) {
            isBleScanning = false
        }
    }

    fun stopBleScan() {
        if (isBleScanning) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
            } catch (e: SecurityException) {
                // Ignore
            }
            isBleScanning = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopBleScan()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = panel?.name ?: "Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimaryDark,
                            maxLines = 1
                        )
                        if (isEditMode) {
                            Text(
                                text = "Design Mode (Drag widgets to reposition)",
                                fontSize = 11.sp,
                                color = ElectricCyan
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                actions = {
                    // In-Header Interactive Connection Chip
                    ConnectionStatusChip(
                        connectionState = connectionState,
                        deviceName = activeDeviceName,
                        hasLastDevice = lastDevice != null,
                        lastDeviceName = lastDevice?.name ?: lastDevice?.address,
                        onChipClick = { ensurePermissionsAndOpenPicker() },
                        onReconnectClick = {
                            if (BluetoothPermissionHelper.hasAllPermissions(context)) {
                                viewModel.reconnectLastDevice()
                            } else {
                                permissionLauncher.launch(BluetoothPermissionHelper.getRequiredPermissions())
                            }
                        },
                        onDisconnectClick = { viewModel.disconnect() }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Edit Mode Toggle
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Done Editing" else "Edit Panel",
                            tint = if (isEditMode) NeonGreen else TextSecondaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            panel?.let { currentPanel ->
                PanelCanvas(
                    panel = currentPanel,
                    isEditMode = isEditMode,
                    telemetryNumeric = telemetryNumeric,
                    telemetryText = telemetryText,
                    telemetryGraphs = telemetryGraphs,
                    onSendCommand = { cmd -> viewModel.sendCommand(cmd) },
                    onWidgetMoved = { widgetId, newX, newY ->
                        viewModel.moveWidget(widgetId, newX, newY)
                    }
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        }
    }

    if (showDevicePicker) {
        DevicePickerDialog(
            pairedDevices = pairedDevices,
            discoveredBleDevices = discoveredBleDevices,
            isBleScanning = isBleScanning,
            onStartBleScan = { startBleScan() },
            onSelectDevice = { device ->
                stopBleScan()
                showDevicePicker = false
                viewModel.connectDevice(device)
            },
            onDismiss = {
                stopBleScan()
                showDevicePicker = false
            }
        )
    }
}

@Composable
fun ConnectionStatusChip(
    connectionState: ConnectionState,
    deviceName: String?,
    hasLastDevice: Boolean,
    lastDeviceName: String?,
    onChipClick: () -> Unit,
    onReconnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    val chipBorderColor = when (connectionState) {
        is ConnectionState.Connected -> NeonGreen
        is ConnectionState.Connecting -> ElectricCyan
        is ConnectionState.Disconnected -> Color.White.copy(alpha = 0.2f)
        is ConnectionState.Error -> RedDisconnect
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant,
        modifier = Modifier
            .border(1.dp, chipBorderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            when (connectionState) {
                is ConnectionState.Connected -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deviceName ?: "Connected",
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.clickable { onChipClick() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✕",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDisconnectClick() }
                            .padding(horizontal = 4.dp)
                    )
                }
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 2.dp,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Connecting...",
                        color = ElectricCyan,
                        fontSize = 12.sp
                    )
                }
                is ConnectionState.Disconnected, is ConnectionState.Error -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RedDisconnect, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Disconnected",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onChipClick() }
                    )
                    if (hasLastDevice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onReconnectClick() }
                                .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Quick Reconnect",
                                tint = ElectricCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Reconnect",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun refreshPairedDevices(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    list: MutableList<DiscoveredDevice>
) {
    list.clear()
    if (bluetoothAdapter == null || !BluetoothPermissionHelper.hasAllPermissions(context)) {
        return
    }
    try {
        bluetoothAdapter.bondedDevices?.forEach { dev ->
            val name = dev.name ?: "Unknown Device"
            list.add(
                DiscoveredDevice(
                    name = name,
                    address = dev.address,
                    type = DeviceType.CLASSIC
                )
            )
        }
    } catch (e: SecurityException) {
        // Permissions issue
    }
}
