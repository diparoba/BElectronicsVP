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
            widgets = listOf(
                WidgetModel.Button(
                    id = "btn_fwd",
                    x = 10f,
                    y = 20f,
                    label = "FORWARD",
                    pressCommand = "F",
                    releaseCommand = "S"
                ),
                WidgetModel.Slider(
                    id = "sld_speed",
                    x = 10f,
                    y = 100f,
                    label = "Speed",
                    minValue = 0,
                    maxValue = 255
                ),
                WidgetModel.Gauge(
                    id = "gag_volt",
                    x = 220f,
                    y = 20f,
                    label = "Battery",
                    telemetryId = 'A',
                    minValue = 0.0,
                    maxValue = 12.6,
                    unit = "V"
                ),
                WidgetModel.TextDisplay(
                    id = "txt_status",
                    x = 220f,
                    y = 180f,
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
        assertEquals(4, decodedPanel.widgets.size)

        val firstWidget = decodedPanel.widgets[0] as WidgetModel.Button
        assertEquals("FORWARD", firstWidget.label)
        assertEquals("F", firstWidget.pressCommand)
    }

    @Test
    fun `handles invalid JSON gracefully with Result failure`() {
        val invalidJson = "{ malformed json content }"
        val result = PanelSerialization.fromJson(invalidJson)
        assertTrue(result.isFailure, "Malformed JSON should return failure")
    }
}
