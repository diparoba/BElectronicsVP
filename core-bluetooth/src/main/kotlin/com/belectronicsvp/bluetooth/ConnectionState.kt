package com.belectronicsvp.bluetooth

/**
 * State representing the active Bluetooth communication lifecycle.
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()

    data class Connecting(val device: DiscoveredDevice) : ConnectionState()

    data class Connected(val device: DiscoveredDevice, val type: DeviceType) : ConnectionState()

    data class Error(val message: String, val throwable: Throwable? = null) : ConnectionState()
}
