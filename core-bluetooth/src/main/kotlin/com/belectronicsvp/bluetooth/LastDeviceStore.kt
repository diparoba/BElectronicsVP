package com.belectronicsvp.bluetooth

import android.content.Context
import android.content.SharedPreferences

/**
 * Storage manager for persisting the last connected device and auto-reconnect preferences.
 */
class LastDeviceStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastDevice(device: DiscoveredDevice) {
        prefs.edit()
            .putString(KEY_ADDRESS, device.address)
            .putString(KEY_NAME, device.name)
            .putString(KEY_TYPE, device.type.name)
            .apply()
    }

    fun getLastDevice(): DiscoveredDevice? {
        val address = prefs.getString(KEY_ADDRESS, null) ?: return null
        val name = prefs.getString(KEY_NAME, "Unknown Device") ?: "Unknown Device"
        val typeStr = prefs.getString(KEY_TYPE, DeviceType.UNKNOWN.name) ?: DeviceType.UNKNOWN.name
        val type = runCatching { DeviceType.valueOf(typeStr) }.getOrDefault(DeviceType.UNKNOWN)

        return DiscoveredDevice(name = name, address = address, type = type)
    }

    fun clearLastDevice() {
        prefs.edit()
            .remove(KEY_ADDRESS)
            .remove(KEY_NAME)
            .remove(KEY_TYPE)
            .apply()
    }

    fun setAutoReconnect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECONNECT, enabled).apply()
    }

    fun isAutoReconnectEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_RECONNECT, false)
    }

    companion object {
        private const val PREFS_NAME = "belectronics_bt_prefs"
        private const val KEY_ADDRESS = "last_device_address"
        private const val KEY_NAME = "last_device_name"
        private const val KEY_TYPE = "last_device_type"
        private const val KEY_AUTO_RECONNECT = "auto_reconnect_enabled"
    }
}
