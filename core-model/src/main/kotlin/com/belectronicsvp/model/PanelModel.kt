package com.belectronicsvp.model

import kotlinx.serialization.Serializable

/**
 * Orientation modes for dashboard panels.
 */
@Serializable
enum class PanelOrientation {
    LANDSCAPE,
    PORTRAIT
}

/**
 * Top-level data model representing an interactive dashboard panel.
 */
@Serializable
data class PanelModel(
    val id: String,
    val name: String,
    val description: String = "",
    val version: Int = 1,
    val orientation: PanelOrientation = PanelOrientation.LANDSCAPE,
    val baseDensity: Int = 8,
    val gridColumns: Int = 12,
    val gridRows: Int = 8,
    val widgets: List<WidgetModel> = emptyList()
)
