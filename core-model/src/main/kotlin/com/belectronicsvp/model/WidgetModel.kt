package com.belectronicsvp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Polymorphic widget definitions supported by BElectronicsVP dashboards.
 */
@Serializable
sealed class WidgetModel {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
    abstract val label: String

    @Serializable
    @SerialName("button")
    data class Button(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 100f,
        override val height: Float = 60f,
        override val label: String = "Button",
        val pressCommand: String = "1",
        val releaseCommand: String? = "0",
        val isMomentary: Boolean = true
    ) : WidgetModel()

    @Serializable
    @SerialName("slider")
    data class Slider(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 200f,
        override val height: Float = 50f,
        override val label: String = "Slider",
        val commandPrefix: String = "*S",
        val commandSuffix: String = "*",
        val minValue: Int = 0,
        val maxValue: Int = 100,
        val initialValue: Int = 0
    ) : WidgetModel()

    @Serializable
    @SerialName("switch")
    data class Switch(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 80f,
        override val height: Float = 50f,
        override val label: String = "Switch",
        val onCommand: String = "H",
        val offCommand: String = "L",
        val initialState: Boolean = false
    ) : WidgetModel()

    @Serializable
    @SerialName("gauge")
    data class Gauge(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 150f,
        override val height: Float = 150f,
        override val label: String = "Gauge",
        val telemetryId: Char = 'A',
        val minValue: Double = 0.0,
        val maxValue: Double = 100.0,
        val unit: String = "%"
    ) : WidgetModel()

    @Serializable
    @SerialName("text_display")
    data class TextDisplay(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 180f,
        override val height: Float = 60f,
        override val label: String = "Display",
        val telemetryId: Char = 'T',
        val defaultText: String = "--"
    ) : WidgetModel()

    @Serializable
    @SerialName("graph")
    data class Graph(
        override val id: String,
        override val x: Float,
        override val y: Float,
        override val width: Float = 250f,
        override val height: Float = 150f,
        override val label: String = "Graph",
        val telemetryId: Char = 'G',
        val maxDataPoints: Int = 50,
        val minValue: Double = 0.0,
        val maxValue: Double = 100.0
    ) : WidgetModel()
}
