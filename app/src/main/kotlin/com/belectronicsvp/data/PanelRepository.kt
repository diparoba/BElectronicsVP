package com.belectronicsvp.data

import android.content.Context
import com.belectronicsvp.model.PanelManager
import com.belectronicsvp.model.PanelModel
import com.belectronicsvp.model.PanelSerialization
import com.belectronicsvp.model.WidgetModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

/**
 * Repository managing persistent storage of dashboard panels on disk.
 */
class PanelRepository(
    private val context: Context? = null,
    baseDir: File? = null
) {

    private val panelManager = PanelManager()
    private val panelsDir = baseDir ?: File(context!!.filesDir, "panels").apply { mkdirs() }

    private val _panelsFlow = MutableStateFlow<List<PanelModel>>(emptyList())
    val panelsFlow: StateFlow<List<PanelModel>> = _panelsFlow.asStateFlow()

    init {
        loadPanels()
        if (_panelsFlow.value.isEmpty()) {
            seedDefaultPanels()
            loadPanels()
        }
    }

    @Synchronized
    fun loadPanels() {
        val files = panelsDir.listFiles { _, name -> name.endsWith(".json") } ?: emptyArray()
        val loaded = files.mapNotNull { file ->
            panelManager.importFromFile(file).getOrNull()
        }.sortedBy { it.name }
        _panelsFlow.value = loaded
    }

    fun getAllPanels(): List<PanelModel> = _panelsFlow.value

    fun getPanel(id: String): PanelModel? = _panelsFlow.value.find { it.id == id }

    @Synchronized
    fun savePanel(panel: PanelModel) {
        val file = File(panelsDir, "${panel.id}.json")
        panelManager.exportToFile(panel, file)
        loadPanels()
    }

    @Synchronized
    fun createPanel(
        name: String,
        description: String = "",
        orientation: com.belectronicsvp.model.PanelOrientation = com.belectronicsvp.model.PanelOrientation.LANDSCAPE,
        baseDensity: Int = 8
    ): PanelModel {
        val panel = panelManager.createPanel(name, description, orientation, baseDensity)
        savePanel(panel)
        return panel
    }

    @Synchronized
    fun renamePanel(id: String, newName: String): PanelModel? {
        val existing = getPanel(id) ?: return null
        val updated = existing.copy(name = newName.ifBlank { existing.name })
        savePanel(updated)
        return updated
    }

    @Synchronized
    fun clonePanel(id: String, newName: String = ""): PanelModel? {
        val existing = getPanel(id) ?: return null
        val cloned = panelManager.clonePanel(existing, newName)
        savePanel(cloned)
        return cloned
    }

    @Synchronized
    fun deletePanel(id: String): Boolean {
        val file = File(panelsDir, "$id.json")
        val deleted = if (file.exists()) file.delete() else false
        loadPanels()
        return deleted
    }

    fun exportToJson(id: String): Result<String> {
        val panel = getPanel(id) ?: return Result.failure(IllegalArgumentException("Panel not found"))
        return PanelSerialization.toJson(panel).let { Result.success(it) }
    }

    fun importFromJson(jsonString: String): Result<PanelModel> {
        return PanelSerialization.fromJson(jsonString).map { parsed ->
            val imported = parsed.copy(id = UUID.randomUUID().toString())
            savePanel(imported)
            imported
        }
    }

    private fun seedDefaultPanels() {
        // 1. ESP32 Sensor Telemetry
        val telemetryPanel = PanelModel(
            id = "default_esp32_telemetry",
            name = "ESP32 Sensor Telemetry",
            description = "Gauge, digital readout and live waveform graph",
            gridColumns = 12,
            gridRows = 8,
            widgets = listOf(
                WidgetModel.Gauge(
                    id = "gauge_1",
                    label = "Temperature (°C)",
                    x = 0f,
                    y = 0f,
                    width = 4f,
                    height = 4f,
                    telemetryId = 'A',
                    minValue = 0.0,
                    maxValue = 100.0,
                    unit = "°C"
                ),
                WidgetModel.TextDisplay(
                    id = "display_1",
                    label = "Status / Message",
                    x = 4f,
                    y = 0f,
                    width = 8f,
                    height = 2f,
                    telemetryId = 'T',
                    defaultText = "Waiting for ESP32..."
                ),
                WidgetModel.Graph(
                    id = "graph_1",
                    label = "Real-time Telemetry (G)",
                    x = 0f,
                    y = 4f,
                    width = 12f,
                    height = 4f,
                    telemetryId = 'G',
                    maxDataPoints = 60,
                    minValue = 0.0,
                    maxValue = 100.0
                )
            )
        )
        savePanel(telemetryPanel)

        // 2. Robot Motor Controller
        val robotPanel = PanelModel(
            id = "default_robot_controller",
            name = "Robot Controller",
            description = "Directional D-pad buttons and speed slider",
            gridColumns = 12,
            gridRows = 8,
            widgets = listOf(
                WidgetModel.Button(
                    id = "btn_fwd",
                    label = "FORWARD",
                    x = 4f,
                    y = 0f,
                    width = 4f,
                    height = 2f,
                    pressCommand = "F",
                    releaseCommand = "S"
                ),
                WidgetModel.Button(
                    id = "btn_left",
                    label = "LEFT",
                    x = 0f,
                    y = 2f,
                    width = 4f,
                    height = 2f,
                    pressCommand = "L",
                    releaseCommand = "S"
                ),
                WidgetModel.Button(
                    id = "btn_stop",
                    label = "STOP",
                    x = 4f,
                    y = 2f,
                    width = 4f,
                    height = 2f,
                    pressCommand = "S",
                    releaseCommand = null
                ),
                WidgetModel.Button(
                    id = "btn_right",
                    label = "RIGHT",
                    x = 8f,
                    y = 2f,
                    width = 4f,
                    height = 2f,
                    pressCommand = "R",
                    releaseCommand = "S"
                ),
                WidgetModel.Button(
                    id = "btn_back",
                    label = "BACKWARD",
                    x = 4f,
                    y = 4f,
                    width = 4f,
                    height = 2f,
                    pressCommand = "B",
                    releaseCommand = "S"
                ),
                WidgetModel.Slider(
                    id = "slider_speed",
                    label = "Speed (0-100%)",
                    x = 0f,
                    y = 6f,
                    width = 12f,
                    height = 2f,
                    commandPrefix = "*V",
                    minValue = 0,
                    maxValue = 100,
                    initialValue = 50
                )
            )
        )
        savePanel(robotPanel)
    }
}
