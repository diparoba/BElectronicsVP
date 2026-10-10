package com.belectronicsvp.protocol

import java.util.concurrent.ConcurrentHashMap

/**
 * Serializes user interaction events (buttons, sliders, toggles) into outbound command strings
 * and applies rate-limiting to prevent microcontroller UART buffer overflows.
 */
class OutboundCommandSerializer {

    private val lastDispatchTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * Formats button action into its configured command string.
     *
     * @param isPressed True if touch down, false if touch release.
     * @param pressCommand String to transmit on touch down (e.g. "1" or "A").
     * @param releaseCommand String to transmit on touch release (e.g. "0" or "a"), or null if no release action.
     * @return Command string to transmit, or null if no command is mapped.
     */
    fun formatButton(isPressed: Boolean, pressCommand: String?, releaseCommand: String?): String? {
        return if (isPressed) {
            pressCommand?.takeIf { it.isNotEmpty() }
        } else {
            releaseCommand?.takeIf { it.isNotEmpty() }
        }
    }

    /**
     * Formats slider position into standard framed command.
     *
     * @param prefix Prefix string (e.g. "*S" or "*A").
     * @param value Integer value to transmit.
     * @param suffix Suffix string (default "*").
     * @return Formatted command string, e.g. `*S125*`.
     */
    fun formatSlider(prefix: String, value: Int, suffix: String = "*"): String {
        return "$prefix$value$suffix"
    }

    /**
     * Evaluates if an outbound command for a given widget should be throttled based on time elapsed.
     *
     * @param widgetKey Unique identifier for the widget (e.g. "slider_A").
     * @param currentTimeMs Current time in milliseconds.
     * @param minIntervalMs Minimum elapsed interval allowed between transmissions (default 50 ms).
     * @return True if the command is allowed and timestamp updated; false if throttled.
     */
    fun shouldTransmit(
        widgetKey: String,
        currentTimeMs: Long,
        minIntervalMs: Long = DEFAULT_THROTTLE_INTERVAL_MS
    ): Boolean {
        val lastTime = lastDispatchTimestamps[widgetKey] ?: 0L
        if (currentTimeMs - lastTime >= minIntervalMs) {
            lastDispatchTimestamps[widgetKey] = currentTimeMs
            return true
        }
        return false
    }

    /**
     * Clears tracked throttle timestamps.
     */
    fun resetThrottle() {
        lastDispatchTimestamps.clear()
    }

    companion object {
        const val DEFAULT_THROTTLE_INTERVAL_MS = 50L
    }
}
