package com.belectronicsvp.bluetooth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility helper for managing Bluetooth runtime permissions across Android versions.
 */
object BluetoothPermissionHelper {

    /**
     * Returns the array of required runtime permissions based on the active Android SDK version.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    /**
     * Checks if all required Bluetooth permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns true if Bluetooth LE is supported on this hardware.
     */
    fun isBleSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
}
