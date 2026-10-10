package com.belectronicsvp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Manages Bluetooth Classic (SPP RFCOMM) connections with automatic channel 1 fallback.
 */
class BluetoothClassicManager(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val scope: CoroutineScope
) {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingData = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingData: SharedFlow<String> = _incomingData.asSharedFlow()

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var readJob: Job? = null

    @SuppressLint("MissingPermission")
    fun connect(device: DiscoveredDevice) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth is disabled or unavailable")
            return
        }

        disconnect()
        _connectionState.value = ConnectionState.Connecting(device)

        scope.launch(Dispatchers.IO) {
            try {
                val btDevice = bluetoothAdapter.getRemoteDevice(device.address)
                val connectedSocket = createAndConnectSocket(btDevice)
                socket = connectedSocket
                outputStream = connectedSocket.outputStream

                _connectionState.value = ConnectionState.Connected(device, DeviceType.CLASSIC)
                startReadLoop(connectedSocket.inputStream, device)
            } catch (e: Exception) {
                disconnect()
                _connectionState.value = ConnectionState.Error(
                    message = "Failed to connect to ${device.name}: ${e.message}",
                    throwable = e
                )
            }
        }
    }

    suspend fun send(data: String): Boolean = withContext(Dispatchers.IO) {
        val stream = outputStream ?: return@withContext false
        try {
            stream.write(data.toByteArray(Charsets.US_ASCII))
            stream.flush()
            true
        } catch (e: IOException) {
            disconnect()
            _connectionState.value = ConnectionState.Error("Write failed: ${e.message}", e)
            false
        }
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null

        try {
            outputStream?.close()
        } catch (_: Exception) {}
        outputStream = null

        try {
            socket?.close()
        } catch (_: Exception) {}
        socket = null

        if (_connectionState.value !is ConnectionState.Disconnected) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAndConnectSocket(device: BluetoothDevice): BluetoothSocket {
        // Try standard SPP UUID first
        try {
            val standardSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            standardSocket.connect()
            return standardSocket
        } catch (e: IOException) {
            // Fallback to reflection on channel 1 (critical for HC-05/06 and older peripherals)
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                val fallbackSocket = method.invoke(device, 1) as BluetoothSocket
                fallbackSocket.connect()
                return fallbackSocket
            } catch (fallbackError: Exception) {
                throw IOException("Both standard SPP and RFCOMM channel 1 failed: ${e.message}", fallbackError)
            }
        }
    }

    private fun startReadLoop(inputStream: InputStream, device: DiscoveredDevice) {
        readJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            while (isActive) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val text = String(buffer, 0, bytesRead, Charsets.US_ASCII)
                        _incomingData.emit(text)
                    } else if (bytesRead == -1) {
                        // End of stream, remote disconnected
                        disconnect()
                        break
                    }
                } catch (e: IOException) {
                    if (isActive) {
                        disconnect()
                        _connectionState.value = ConnectionState.Error("Connection closed by peer", e)
                    }
                    break
                }
            }
        }
    }

    companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
