package com.belectronicsvp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Universal BLE GATT Client supporting Nordic UART (ESP32 standard), HM-10, and dynamic UART profiles.
 */
class UniversalBleManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val scope: CoroutineScope
) {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingData = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingData: SharedFlow<String> = _incomingData.asSharedFlow()

    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var activeDevice: DiscoveredDevice? = null
    private var negotiatedMtu: Int = 23 // Default BLE MTU

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val device = activeDevice ?: return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnect()
                _connectionState.value = ConnectionState.Error("GATT Error status: $status")
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = ConnectionState.Connecting(device)
                // Request 512-byte MTU for high-speed ESP32 streaming
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                negotiatedMtu = mtu
            }
            // Discover services after MTU negotiation
            gatt.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val device = activeDevice ?: return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnect()
                _connectionState.value = ConnectionState.Error("Service discovery failed: $status")
                return
            }

            val resolved = resolveUartChannels(gatt)
            if (resolved != null) {
                val (rxNotifyChar, txWriteChar) = resolved
                writeCharacteristic = txWriteChar

                // Enable notifications on the RX characteristic
                gatt.setCharacteristicNotification(rxNotifyChar, true)
                val cccd = rxNotifyChar.getDescriptor(CCCD_UUID)
                if (cccd != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        @Suppress("DEPRECATION")
                        cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        gatt.writeDescriptor(cccd)
                    }
                }

                _connectionState.value = ConnectionState.Connected(device, DeviceType.BLE)
            } else {
                disconnect()
                _connectionState.value = ConnectionState.Error(
                    "No compatible BLE UART service found on ${device.name}. Check ESP32 UART service."
                )
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            val bytes = characteristic.value ?: return
            handleIncomingBytes(bytes)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingBytes(value)
        }
    }

    private fun handleIncomingBytes(bytes: ByteArray) {
        if (bytes.isNotEmpty()) {
            val text = String(bytes, Charsets.US_ASCII)
            scope.launch {
                _incomingData.emit(text)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: DiscoveredDevice) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth adapter is disabled")
            return
        }

        disconnect()
        activeDevice = device
        _connectionState.value = ConnectionState.Connecting(device)

        val btDevice = bluetoothAdapter.getRemoteDevice(device.address)
        gatt = btDevice.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    suspend fun send(data: String): Boolean = withContext(Dispatchers.IO) {
        val targetGatt = gatt ?: return@withContext false
        val char = writeCharacteristic ?: return@withContext false
        val bytes = data.toByteArray(Charsets.US_ASCII)

        // Chunk data by MTU minus 3 bytes of GATT header
        val chunkSize = (negotiatedMtu - 3).coerceIn(20, 509)
        var offset = 0
        while (offset < bytes.size) {
            val length = (bytes.size - offset).coerceAtMost(chunkSize)
            val chunk = bytes.copyOfRange(offset, offset + length)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val writeType = if ((char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                } else {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                }
                targetGatt.writeCharacteristic(char, chunk, writeType)
            } else {
                @Suppress("DEPRECATION")
                char.value = chunk
                @Suppress("DEPRECATION")
                targetGatt.writeCharacteristic(char)
            }
            offset += length
        }
        true
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (_: Exception) {}
        gatt = null
        writeCharacteristic = null
        activeDevice = null

        if (_connectionState.value !is ConnectionState.Disconnected) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    /**
     * Resolves Pair(NotifyCharacteristic, WriteCharacteristic) across NUS, HM-10, or generic services.
     */
    private fun resolveUartChannels(gatt: BluetoothGatt): Pair<BluetoothGattCharacteristic, BluetoothGattCharacteristic>? {
        // Priority 1: Nordic UART Service (standard on modern ESP32)
        val nusService = gatt.getService(NUS_SERVICE_UUID)
        if (nusService != null) {
            val rxNotify = nusService.getCharacteristic(NUS_RX_NOTIFY_UUID)
            val txWrite = nusService.getCharacteristic(NUS_TX_WRITE_UUID)
            if (rxNotify != null && txWrite != null) {
                return Pair(rxNotify, txWrite)
            }
        }

        // Priority 2: Legacy HM-10 / CC2541 Service
        val hm10Service = gatt.getService(HM10_SERVICE_UUID)
        if (hm10Service != null) {
            val hm10Char = hm10Service.getCharacteristic(HM10_CHAR_UUID)
            if (hm10Char != null) {
                return Pair(hm10Char, hm10Char)
            }
        }

        // Priority 3: Dynamic fallback across all services
        for (service in gatt.services) {
            var notifyChar: BluetoothGattCharacteristic? = null
            var writeChar: BluetoothGattCharacteristic? = null

            for (char in service.characteristics) {
                val props = char.properties
                if ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    notifyChar = char
                }
                if ((props and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                    writeChar = char
                }
            }

            if (notifyChar != null && writeChar != null) {
                return Pair(notifyChar, writeChar)
            }
        }

        return null
    }

    companion object {
        // Nordic UART Service (NUS) - ESP32 standard
        val NUS_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val NUS_TX_WRITE_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E") // Phone writes to ESP32
        val NUS_RX_NOTIFY_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E") // ESP32 notifies phone

        // HM-10 Legacy Service
        val HM10_SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        val HM10_CHAR_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")

        // Client Characteristic Configuration Descriptor
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }
}
