package com.belectronicsvp.protocol

/**
 * Represents a parsed incoming telemetry or control frame in the Keuwlsoft serial protocol.
 */
sealed class ProtocolFrame {
    /**
     * Numeric measurement intended for a gauge, slider, bar, or meter.
     * Example: `*A125*` -> id = 'A', value = 125.0
     */
    data class Numeric(val id: Char, val value: Double) : ProtocolFrame()

    /**
     * Text status string intended for a text display or notification box.
     * Example: `*TSystem Ready*` -> id = 'T', text = "System Ready"
     */
    data class Text(val id: Char, val text: String) : ProtocolFrame()

    /**
     * Multi-channel RGB color values for an indicator LED or lamp widget.
     * Example: `*R255G120B0*` -> red = 255, green = 120, blue = 0
     */
    data class Rgb(val red: Int, val green: Int, val blue: Int) : ProtocolFrame()

    /**
     * Fallback for custom or unrecognized frames delimited by asterisks.
     */
    data class Raw(val raw: String) : ProtocolFrame()
}
