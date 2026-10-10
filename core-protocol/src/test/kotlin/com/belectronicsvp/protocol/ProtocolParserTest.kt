package com.belectronicsvp.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProtocolParserTest {

    @Test
    fun `parses standard numeric telemetry frame`() {
        val parser = ProtocolParser()
        val frames = parser.feed("*A125*")

        assertEquals(1, frames.size)
        val frame = frames[0] as ProtocolFrame.Numeric
        assertEquals('A', frame.id)
        assertEquals(125.0, frame.value)
    }

    @Test
    fun `parses negative and decimal numeric frames`() {
        val parser = ProtocolParser()
        val frames = parser.feed("*B-45.75*")

        assertEquals(1, frames.size)
        val frame = frames[0] as ProtocolFrame.Numeric
        assertEquals('B', frame.id)
        assertEquals(-45.75, frame.value)
    }

    @Test
    fun `parses string text display frame`() {
        val parser = ProtocolParser()
        val frames = parser.feed("*TSystem Online: 24.5 C*")

        assertEquals(1, frames.size)
        val frame = frames[0] as ProtocolFrame.Text
        assertEquals('T', frame.id)
        assertEquals("System Online: 24.5 C", frame.text)
    }

    @Test
    fun `parses RGB color frame`() {
        val parser = ProtocolParser()
        val frames = parser.feed("*R255G120B0*")

        assertEquals(1, frames.size)
        val frame = frames[0] as ProtocolFrame.Rgb
        assertEquals(255, frame.red)
        assertEquals(120, frame.green)
        assertEquals(0, frame.blue)
    }

    @Test
    fun `handles fragmented chunks across serial stream reads`() {
        val parser = ProtocolParser()

        val chunk1 = parser.feed("*A1")
        assertTrue(chunk1.isEmpty(), "Chunk 1 should not yield a complete frame")

        val chunk2 = parser.feed("50*")
        assertEquals(1, chunk2.size, "Chunk 2 should complete the frame")
        val frame = chunk2[0] as ProtocolFrame.Numeric
        assertEquals('A', frame.id)
        assertEquals(150.0, frame.value)
    }

    @Test
    fun `discards garbage bytes outside asterisk delimiters`() {
        val parser = ProtocolParser()
        val frames = parser.feed("Arduino Serial Booting...\r\n*A10*Garbage noise 12345*B20*more noise")

        assertEquals(2, frames.size)
        assertEquals(ProtocolFrame.Numeric('A', 10.0), frames[0])
        assertEquals(ProtocolFrame.Numeric('B', 20.0), frames[1])
    }

    @Test
    fun `handles empty frames and invalid payloads gracefully`() {
        val parser = ProtocolParser()
        val frames = parser.feed("****A5***")

        assertEquals(1, frames.size)
        assertEquals(ProtocolFrame.Numeric('A', 5.0), frames[0])
    }
}
