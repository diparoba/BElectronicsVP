package com.belectronicsvp.protocol

/**
 * Streaming state-machine parser for the asterisk-delimited Keuwlsoft serial protocol.
 *
 * Microcontroller packets follow the format:
 * `*<ID><PAYLOAD>*`
 *
 * This parser handles fragmented chunks, noisy byte streams, RGB channel extractions,
 * and numeric/text frame classification without leaking memory.
 */
class ProtocolParser {

    private val buffer = StringBuilder()
    private var inFrame = false

    /**
     * Feeds incoming ASCII or UTF-8 characters and returns all fully parsed frames.
     *
     * @param input Incoming string or chunk from the serial stream.
     * @return List of parsed [ProtocolFrame] instances.
     */
    fun feed(input: String): List<ProtocolFrame> {
        val frames = mutableListOf<ProtocolFrame>()

        for (char in input) {
            if (char == '*') {
                if (inFrame && buffer.isNotEmpty()) {
                    // Frame completed with payload
                    val payload = buffer.toString()
                    val frame = parsePayload(payload)
                    if (frame != null) {
                        frames.add(frame)
                    }
                    buffer.clear()
                    inFrame = false
                } else {
                    // Start of a frame (or consecutive delimiter reset)
                    inFrame = true
                    buffer.clear()
                }
            } else if (inFrame) {
                // Safeguard against run-away buffers from missing closing asterisks
                if (buffer.length < MAX_FRAME_LENGTH) {
                    buffer.append(char)
                } else {
                    // Buffer exceeded max length, reset to avoid memory bloat
                    buffer.clear()
                    inFrame = false
                }
            }
        }

        return frames
    }

    /**
     * Resets parser state and clears any pending buffers.
     */
    fun reset() {
        buffer.clear()
        inFrame = false
    }

    private fun parsePayload(payload: String): ProtocolFrame? {
        if (payload.isEmpty()) return null

        // Check for RGB format: e.g. R255G120B0
        val rgbMatch = RGB_REGEX.matchEntire(payload)
        if (rgbMatch != null) {
            val r = rgbMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 255) ?: 0
            val g = rgbMatch.groupValues[2].toIntOrNull()?.coerceIn(0, 255) ?: 0
            val b = rgbMatch.groupValues[3].toIntOrNull()?.coerceIn(0, 255) ?: 0
            return ProtocolFrame.Rgb(r, g, b)
        }

        val id = payload[0]
        val valueString = payload.substring(1)

        // Try parsing numeric value (integer or float)
        val numericValue = valueString.toDoubleOrNull()
        if (numericValue != null) {
            return ProtocolFrame.Numeric(id, numericValue)
        }

        // Non-numeric payload -> Text display frame
        return ProtocolFrame.Text(id, valueString)
    }

    companion object {
        private const val MAX_FRAME_LENGTH = 1024
        private val RGB_REGEX = Regex("""^R(\d{1,3})G(\d{1,3})B(\d{1,3})$""", RegexOption.IGNORE_CASE)
    }
}
