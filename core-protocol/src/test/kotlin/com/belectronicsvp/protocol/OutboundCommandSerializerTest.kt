package com.belectronicsvp.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OutboundCommandSerializerTest {

    @Test
    fun `formats button press and release commands`() {
        val serializer = OutboundCommandSerializer()

        val press = serializer.formatButton(isPressed = true, pressCommand = "1", releaseCommand = "0")
        assertEquals("1", press)

        val release = serializer.formatButton(isPressed = false, pressCommand = "1", releaseCommand = "0")
        assertEquals("0", release)

        val noRelease = serializer.formatButton(isPressed = false, pressCommand = "A", releaseCommand = null)
        assertNull(noRelease)
    }

    @Test
    fun `formats slider values with prefixes and suffixes`() {
        val serializer = OutboundCommandSerializer()

        val defaultSlider = serializer.formatSlider(prefix = "*S", value = 150)
        assertEquals("*S150*", defaultSlider)

        val customSlider = serializer.formatSlider(prefix = "CMD_", value = 42, suffix = "#")
        assertEquals("CMD_42#", customSlider)
    }

    @Test
    fun `applies rate limiting per widget identifier`() {
        val serializer = OutboundCommandSerializer()

        // First transmission at time 100ms should pass
        val first = serializer.shouldTransmit("slider1", currentTimeMs = 100L, minIntervalMs = 50L)
        assertTrue(first, "Initial transmission must be allowed")

        // Rapid transmission at time 120ms (within 50ms window) must be throttled
        val throttled = serializer.shouldTransmit("slider1", currentTimeMs = 120L, minIntervalMs = 50L)
        assertFalse(throttled, "Transmission within interval must be throttled")

        // Transmission at time 160ms (after 50ms window) should pass
        val allowed = serializer.shouldTransmit("slider1", currentTimeMs = 160L, minIntervalMs = 50L)
        assertTrue(allowed, "Transmission after interval must be allowed")

        // Different widget at time 120ms should independently pass
        val otherWidget = serializer.shouldTransmit("slider2", currentTimeMs = 120L, minIntervalMs = 50L)
        assertTrue(otherWidget, "Independent widget must not be throttled by first widget")
    }
}
