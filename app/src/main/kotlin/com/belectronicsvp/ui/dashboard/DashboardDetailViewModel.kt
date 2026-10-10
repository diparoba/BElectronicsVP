package com.belectronicsvp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belectronicsvp.bluetooth.BluetoothClassicManager
import com.belectronicsvp.bluetooth.ConnectionState
import com.belectronicsvp.bluetooth.DeviceType
import com.belectronicsvp.bluetooth.DiscoveredDevice
import com.belectronicsvp.bluetooth.LastDeviceStore
import com.belectronicsvp.bluetooth.UniversalBleManager
import com.belectronicsvp.data.PanelRepository
import com.belectronicsvp.model.PanelModel
import com.belectronicsvp.protocol.ProtocolFrame
import com.belectronicsvp.protocol.ProtocolParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardDetailViewModel(
    private val panelId: String,
    private val repository: PanelRepository,
    private val classicManager: BluetoothClassicManager,
    private val bleManager: UniversalBleManager,
    private val lastDeviceStore: LastDeviceStore,
    private val protocolParser: ProtocolParser = ProtocolParser()
) : ViewModel() {

    private val _panel = MutableStateFlow<PanelModel?>(null)
    val panel: StateFlow<PanelModel?> = _panel.asStateFlow()

    private val _isBleActive = MutableStateFlow(false)
    val isBleActive: StateFlow<Boolean> = _isBleActive.asStateFlow()

    // Combined connection state depending on whether BLE or Classic is active
    val connectionState: StateFlow<ConnectionState> = combine(
        _isBleActive,
        classicManager.connectionState,
        bleManager.connectionState
    ) { isBle, classicState, bleState ->
        if (isBle) bleState else classicState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.Disconnected)

    private val _activeDeviceName = MutableStateFlow<String?>(null)
    val activeDeviceName: StateFlow<String?> = _activeDeviceName.asStateFlow()

    private val _lastDevice = MutableStateFlow<DiscoveredDevice?>(lastDeviceStore.getLastDevice())
    val lastDevice: StateFlow<DiscoveredDevice?> = _lastDevice.asStateFlow()

    private val _telemetryNumeric = MutableStateFlow<Map<Char, Double>>(emptyMap())
    val telemetryNumeric: StateFlow<Map<Char, Double>> = _telemetryNumeric.asStateFlow()

    private val _telemetryText = MutableStateFlow<Map<Char, String>>(emptyMap())
    val telemetryText: StateFlow<Map<Char, String>> = _telemetryText.asStateFlow()

    private val _telemetryGraphs = MutableStateFlow<Map<Char, List<Double>>>(emptyMap())
    val telemetryGraphs: StateFlow<Map<Char, List<Double>>> = _telemetryGraphs.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    init {
        loadPanel()
        listenToIncomingData()
    }

    private fun loadPanel() {
        _panel.value = repository.getPanel(panelId)
    }

    private fun listenToIncomingData() {
        // Collect Classic Stream
        viewModelScope.launch {
            classicManager.incomingData.collect { chunk ->
                processIncomingString(chunk)
            }
        }

        // Collect BLE Stream
        viewModelScope.launch {
            bleManager.incomingData.collect { chunk ->
                processIncomingString(chunk)
            }
        }
    }

    private fun processIncomingString(input: String) {
        val frames = protocolParser.feed(input)
        frames.forEach { frame ->
            when (frame) {
                is ProtocolFrame.Numeric -> {
                    _telemetryNumeric.value = _telemetryNumeric.value + (frame.id to frame.value)

                    // Append to graph history (keep up to 60 points)
                    val currentList = _telemetryGraphs.value[frame.id] ?: emptyList()
                    val updatedList = (currentList + frame.value).takeLast(60)
                    _telemetryGraphs.value = _telemetryGraphs.value + (frame.id to updatedList)
                }
                is ProtocolFrame.Text -> {
                    _telemetryText.value = _telemetryText.value + (frame.id to frame.text)
                }
                is ProtocolFrame.Rgb -> {
                    // RGB frame handler
                }
                is ProtocolFrame.Raw -> {
                    // Raw noise or non-frame data
                }
            }
        }
    }

    fun reconnectLastDevice() {
        val last = _lastDevice.value ?: return
        if (last.type == DeviceType.BLE) {
            _isBleActive.value = true
            _activeDeviceName.value = last.name
            bleManager.connect(last)
        } else {
            _isBleActive.value = false
            _activeDeviceName.value = last.name
            classicManager.connect(last)
        }
    }

    fun connectDevice(device: DiscoveredDevice) {
        lastDeviceStore.saveLastDevice(device)
        _lastDevice.value = device
        _activeDeviceName.value = device.name

        if (device.type == DeviceType.BLE) {
            _isBleActive.value = true
            classicManager.disconnect()
            bleManager.connect(device)
        } else {
            _isBleActive.value = false
            bleManager.disconnect()
            classicManager.connect(device)
        }
    }

    fun disconnect() {
        if (_isBleActive.value) {
            bleManager.disconnect()
        } else {
            classicManager.disconnect()
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            if (_isBleActive.value) {
                bleManager.send(command)
            } else {
                classicManager.send(command)
            }
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun moveWidget(widgetId: String, newX: Float, newY: Float) {
        val current = _panel.value ?: return
        val updatedWidgets = current.widgets.map { widget ->
            if (widget.id == widgetId) {
                when (widget) {
                    is com.belectronicsvp.model.WidgetModel.Button -> widget.copy(x = newX, y = newY)
                    is com.belectronicsvp.model.WidgetModel.Slider -> widget.copy(x = newX, y = newY)
                    is com.belectronicsvp.model.WidgetModel.Switch -> widget.copy(x = newX, y = newY)
                    is com.belectronicsvp.model.WidgetModel.Gauge -> widget.copy(x = newX, y = newY)
                    is com.belectronicsvp.model.WidgetModel.TextDisplay -> widget.copy(x = newX, y = newY)
                    is com.belectronicsvp.model.WidgetModel.Graph -> widget.copy(x = newX, y = newY)
                }
            } else {
                widget
            }
        }
        val updatedPanel = current.copy(widgets = updatedWidgets)
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }

    fun resizeWidget(widgetId: String, newWidth: Float, newHeight: Float) {
        val current = _panel.value ?: return
        val updatedWidgets = current.widgets.map { widget ->
            if (widget.id == widgetId) {
                when (widget) {
                    is com.belectronicsvp.model.WidgetModel.Button -> widget.copy(width = newWidth, height = newHeight)
                    is com.belectronicsvp.model.WidgetModel.Slider -> widget.copy(width = newWidth, height = newHeight)
                    is com.belectronicsvp.model.WidgetModel.Switch -> widget.copy(width = newWidth, height = newHeight)
                    is com.belectronicsvp.model.WidgetModel.Gauge -> widget.copy(width = newWidth, height = newHeight)
                    is com.belectronicsvp.model.WidgetModel.TextDisplay -> widget.copy(width = newWidth, height = newHeight)
                    is com.belectronicsvp.model.WidgetModel.Graph -> widget.copy(width = newWidth, height = newHeight)
                }
            } else {
                widget
            }
        }
        val updatedPanel = current.copy(widgets = updatedWidgets)
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }

    fun addWidget(widget: com.belectronicsvp.model.WidgetModel) {
        val current = _panel.value ?: return
        val updatedPanel = current.copy(widgets = current.widgets + widget)
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }

    fun deleteWidget(widgetId: String) {
        val current = _panel.value ?: return
        val updatedPanel = current.copy(widgets = current.widgets.filter { it.id != widgetId })
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }

    fun updateWidget(updatedWidget: com.belectronicsvp.model.WidgetModel) {
        val current = _panel.value ?: return
        val updatedWidgets = current.widgets.map { if (it.id == updatedWidget.id) updatedWidget else it }
        val updatedPanel = current.copy(widgets = updatedWidgets)
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }

    fun updateGridDensity(density: Int) {
        val current = _panel.value ?: return
        val updatedPanel = current.copy(baseDensity = density.coerceIn(4, 24))
        _panel.value = updatedPanel
        repository.savePanel(updatedPanel)
    }
}
