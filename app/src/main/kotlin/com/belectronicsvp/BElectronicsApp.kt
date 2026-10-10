package com.belectronicsvp

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import com.belectronicsvp.bluetooth.BluetoothClassicManager
import com.belectronicsvp.bluetooth.LastDeviceStore
import com.belectronicsvp.bluetooth.UniversalBleManager
import com.belectronicsvp.data.PanelRepository
import com.belectronicsvp.protocol.ProtocolParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BElectronicsApp : Application() {

    lateinit var panelRepository: PanelRepository
        private set

    lateinit var lastDeviceStore: LastDeviceStore
        private set

    lateinit var bluetoothClassicManager: BluetoothClassicManager
        private set

    lateinit var universalBleManager: UniversalBleManager
        private set

    val protocolParser = ProtocolParser()

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        panelRepository = PanelRepository(context = this)
        lastDeviceStore = LastDeviceStore(this)
        bluetoothClassicManager = BluetoothClassicManager(bluetoothAdapter, appScope)
        universalBleManager = UniversalBleManager(this, bluetoothAdapter, appScope)
    }

    companion object {
        lateinit var instance: BElectronicsApp
            private set
    }
}
