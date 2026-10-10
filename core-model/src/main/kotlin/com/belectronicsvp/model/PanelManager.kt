package com.belectronicsvp.model

import java.io.File
import java.util.UUID

/**
 * Manages panel creation, cloning, and file import/export.
 */
class PanelManager {

    /**
     * Creates a new blank panel with default grid dimensions and specified orientation.
     */
    fun createPanel(
        name: String,
        description: String = "",
        orientation: PanelOrientation = PanelOrientation.LANDSCAPE,
        baseDensity: Int = 8
    ): PanelModel {
        return PanelModel(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Untitled Panel" },
            description = description,
            orientation = orientation,
            baseDensity = baseDensity,
            widgets = emptyList()
        )
    }

    /**
     * Clones an existing panel with a new identifier and name.
     */
    fun clonePanel(source: PanelModel, newName: String): PanelModel {
        return source.copy(
            id = UUID.randomUUID().toString(),
            name = newName.ifBlank { "${source.name} (Copy)" }
        )
    }

    /**
     * Exports a panel to a file in `.json` or `.bep` format.
     */
    fun exportToFile(panel: PanelModel, targetFile: File): Result<Unit> {
        return runCatching {
            val json = PanelSerialization.toJson(panel)
            targetFile.writeText(json, Charsets.UTF_8)
        }
    }

    /**
     * Imports and validates a panel from a file.
     */
    fun importFromFile(sourceFile: File): Result<PanelModel> {
        return runCatching {
            require(sourceFile.exists()) { "Panel file does not exist: ${sourceFile.absolutePath}" }
            val content = sourceFile.readText(Charsets.UTF_8)
            val decoded = PanelSerialization.fromJson(content).getOrThrow()
            require(decoded.name.isNotBlank()) { "Invalid panel: Missing panel name" }
            decoded
        }
    }
}
