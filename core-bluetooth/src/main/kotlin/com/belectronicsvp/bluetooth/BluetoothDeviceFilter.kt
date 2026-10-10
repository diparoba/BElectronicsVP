package com.belectronicsvp.bluetooth

/**
 * Type of Bluetooth connection supported by the peripheral device.
 */
enum class DeviceType {
    CLASSIC,
    BLE,
    DUAL,
    UNKNOWN
}

/**
 * Representation of a discovered or paired Bluetooth device.
 */
data class DiscoveredDevice(
    val name: String,
    val address: String,
    val type: DeviceType = DeviceType.UNKNOWN,
    val rssi: Int? = null
)

/**
 * Helper for searching and real-time filtering of Bluetooth devices.
 */
object BluetoothDeviceFilter {

    /**
     * Filters a list of devices matching a query against device name or MAC address.
     *
     * @param devices The full list of devices.
     * @param query The search query string.
     * @return Filtered list matching the query.
     */
    fun filter(devices: List<DiscoveredDevice>, query: String): List<DiscoveredDevice> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return devices

        return devices.filter { device ->
            device.name.contains(trimmed, ignoreCase = true) ||
                    device.address.contains(trimmed, ignoreCase = true)
        }
    }
}
