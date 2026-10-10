package com.belectronicsvp.bluetooth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BluetoothDeviceFilterTest {

    private val sampleDevices = listOf(
        DiscoveredDevice(name = "ESP32-Rover", address = "98:CD:AC:12:34:56", type = DeviceType.BLE),
        DiscoveredDevice(name = "HC-05-Telemetry", address = "00:14:03:05:59:7A", type = DeviceType.CLASSIC),
        DiscoveredDevice(name = "Arduino-Robot", address = "00:19:10:08:24:90", type = DeviceType.CLASSIC),
        DiscoveredDevice(name = "JDY-30-Relay", address = "20:18:11:05:12:34", type = DeviceType.DUAL)
    )

    @Test
    fun `returns all devices when search query is empty or whitespace`() {
        val emptyResult = BluetoothDeviceFilter.filter(sampleDevices, "")
        assertEquals(4, emptyResult.size)

        val whitespaceResult = BluetoothDeviceFilter.filter(sampleDevices, "   ")
        assertEquals(4, whitespaceResult.size)
    }

    @Test
    fun `filters devices by name case-insensitively`() {
        val espResult = BluetoothDeviceFilter.filter(sampleDevices, "esp32")
        assertEquals(1, espResult.size)
        assertEquals("ESP32-Rover", espResult[0].name)

        val roverResult = BluetoothDeviceFilter.filter(sampleDevices, "ROVER")
        assertEquals(1, roverResult.size)
        assertEquals("ESP32-Rover", roverResult[0].name)
    }

    @Test
    fun `filters devices by MAC address`() {
        val macResult = BluetoothDeviceFilter.filter(sampleDevices, "00:14:03")
        assertEquals(1, macResult.size)
        assertEquals("HC-05-Telemetry", macResult[0].name)
    }

    @Test
    fun `returns empty list when no device matches query`() {
        val noMatch = BluetoothDeviceFilter.filter(sampleDevices, "NonExistentDevice")
        assertTrue(noMatch.isEmpty())
    }
}
