package com.belectronicsvp.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PanelSerializationTest {

    @Test
    fun `round-trip serialization of panel with multiple widget types`() {
        val originalPanel = PanelModel(
            id = "demo_panel",
            name = "Arduino Rover Control",
            description = "Tele-operation dashboard for HC-05 rover",
            orientation = PanelOrientation.LANDSCAPE,
            baseDensity = 8,
            widgets = listOf(
                WidgetModel.Button(
                    id = "btn_fwd",
                    x = 1f,
                    y = 2f,
                    width = 3f,
                    height = 2f,
                    label = "FORWARD",
                    pressCommand = "F",
                    releaseCommand = "S"
                ),
                WidgetModel.Slider(
                    id = "sld_speed",
                    x = 1f,
                    y = 4f,
                    width = 6f,
                    height = 2f,
                    label = "Speed",
                    commandPrefix = "V: ",
                    commandSuffix = "\n",
                    minValue = 0,
                    maxValue = 9
                ),
                WidgetModel.Gauge(
                    id = "gag_volt",
                    x = 8f,
                    y = 1f,
                    width = 4f,
                    height = 4f,
                    label = "Battery",
                    telemetryId = 'A',
                    minValue = 0.0,
                    maxValue = 12.6,
                    unit = "V"
                ),
                WidgetModel.TextDisplay(
                    id = "txt_status",
                    x = 8f,
                    y = 5f,
                    width = 4f,
                    height = 2f,
                    label = "Status",
                    telemetryId = 'T'
                )
            )
        )

        val jsonString = PanelSerialization.toJson(originalPanel)
        assertTrue(jsonString.contains("Arduino Rover Control"))
        assertTrue(jsonString.contains("\"type\": \"button\"") || jsonString.contains("\"type\":\"button\""))

        val decodedResult = PanelSerialization.fromJson(jsonString)
        assertTrue(decodedResult.isSuccess, "JSON must decode successfully")

        val decodedPanel = decodedResult.getOrThrow()
        assertEquals(originalPanel.id, decodedPanel.id)
        assertEquals(originalPanel.name, decodedPanel.name)
        assertEquals(PanelOrientation.LANDSCAPE, decodedPanel.orientation)
        assertEquals(8, decodedPanel.baseDensity)
        assertEquals(4, decodedPanel.widgets.size)

        val firstWidget = decodedPanel.widgets[0] as WidgetModel.Button
        assertEquals("FORWARD", firstWidget.label)
        assertEquals("F", firstWidget.pressCommand)
        assertEquals("S", firstWidget.releaseCommand)

        val sliderWidget = decodedPanel.widgets[1] as WidgetModel.Slider
        assertEquals("V: ", sliderWidget.commandPrefix)
        assertEquals("\n", sliderWidget.commandSuffix)
        assertEquals(0, sliderWidget.minValue)
        assertEquals(9, sliderWidget.maxValue)
    }

    @Test
    fun `backward compatibility with legacy JSON omitting orientation and density`() {
        val legacyJson = """
            {
                "id": "legacy_panel_1",
                "name": "Legacy Dashboard",
                "description": "Created in older app version",
                "version": 1,
                "widgets": []
            }
        """.trimIndent()

        val decodedResult = PanelSerialization.fromJson(legacyJson)
        assertTrue(decodedResult.isSuccess, "Legacy JSON without orientation must decode successfully")
        val panel = decodedResult.getOrThrow()
        assertEquals("Legacy Dashboard", panel.name)
        assertEquals(PanelOrientation.LANDSCAPE, panel.orientation, "Should default to LANDSCAPE")
        assertEquals(8, panel.baseDensity, "Should default to baseDensity 8")
    }

    @Test
    fun `portrait orientation serialization round-trip`() {
        val portraitPanel = PanelModel(
            id = "portrait_1",
            name = "Vertical Cockpit",
            orientation = PanelOrientation.PORTRAIT,
            baseDensity = 10
        )
        val json = PanelSerialization.toJson(portraitPanel)
        val decoded = PanelSerialization.fromJson(json).getOrThrow()
        assertEquals(PanelOrientation.PORTRAIT, decoded.orientation)
        assertEquals(10, decoded.baseDensity)
    }

    @Test
    fun `handles invalid JSON gracefully with Result failure`() {
        val invalidJson = "{ malformed json content }"
        val result = PanelSerialization.fromJson(invalidJson)
        assertTrue(result.isFailure, "Malformed JSON should return failure")
    }
}
