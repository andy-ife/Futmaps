package com.andyslab.futmaps.domain.repository.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.andyslab.futmaps.domain.entities.ProximityBleResult
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.pow

private const val DEVICE_NAME = "Dean's Office"
private const val DEVICE_ADDRESS = "E4:65:B8:75:9B:C6"
private const val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
private const val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
const val CCCD_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

private const val MAX_CONNECTION_ATTEMPTS = 5

@SuppressLint("MissingPermission")
class ProximityBLERepoImpl(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context,
) : ProximityBLERepo {

    override val proximityData: MutableSharedFlow<Resource<ProximityBleResult>> =
        MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings
        .Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var currentConnectionAttempt = 1

    private var proximityBleResult: ProximityBleResult? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//            coroutineScope.launch{
//                if(result?.device?.name != null){
//                    proximityData.emit(Resource.Loading(
//                        message = "Device found: ${result.device?.name}\n" +
//                                "${result.device?.address}"
//                    ))
//                }
//            }
            try {
                if (result?.device?.name == DEVICE_NAME || result?.device?.address == DEVICE_ADDRESS) {
                    coroutineScope.launch {
                        proximityData.emit(Resource.Loading(message = "Device found: ${result.device.name}..."))
                    }
                    proximityBleResult = ProximityBleResult(
                        result.device.name,
                        "Bluetooth Low Energy Beacon",
                        result.device.address,
                        null
                    )

                    coroutineScope.launch {
                        proximityData.emit(Resource.Loading(message = "Connecting to device..."))
                    }
                    if (isScanning) {
                        result.device?.connectGatt(
                            context,
                            true,
                            gattCallback,
                            BluetoothDevice.TRANSPORT_LE
                        )
                        isScanning = false
                        bleScanner.stopScan(this)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
                coroutineScope.launch {
                    proximityData.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    coroutineScope.launch {
                        proximityData.emit(Resource.Loading(message = "Discovering services..."))
                    }
                    gatt.discoverServices()
                    this@ProximityBLERepoImpl.gatt = gatt
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    coroutineScope.launch {
                        proximityData.emit(Resource.Success(null))
                    }
                    gatt.close()
                }
            } else {
                gatt.close()
                currentConnectionAttempt += 1
                coroutineScope.launch {
                    proximityData.emit(Resource.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAX_CONNECTION_ATTEMPTS"))
                }
                if (currentConnectionAttempt <= MAX_CONNECTION_ATTEMPTS) {
                    this@ProximityBLERepoImpl.startReceiving()
                } else {
                    coroutineScope.launch {
                        proximityData.emit(Resource.Error(message = "Could not connect to BLE device."))
                    }
                    currentConnectionAttempt = 1
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                printGattTable()
                coroutineScope.launch {
                    proximityData.emit(Resource.Loading(message = "Adjusting MTU space..."))
                }
                requestMtu(517)
            }
        }

        fun calculateDistance(rssi: Int, txPower: Int = -59, n: Double = 2.0): Double {
            val distance = 10.0.pow((txPower - rssi) / (10 * n))
            return String.format(Locale.getDefault(), "%.2f", distance).toDouble()
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            // Handle the RSSI value here
            Log.d("RSSI", "RSSI: $rssi")
            coroutineScope.launch {
                proximityData.emit(
                    Resource.Success(
                        ProximityBleResult(
                            DEVICE_NAME,
                            "Bluetooth Low Energy Beacon",
                            DEVICE_ADDRESS,
                            "${calculateDistance(rssi)}m"
                        )
                    )
                )
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID)
            if (characteristic == null) {
                coroutineScope.launch {
                    proximityData.emit(Resource.Error(message = "Could not find proximity data publisher."))
                }
                return
            }
            enableNotifications(characteristic)
            startReadingRssi()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            with(characteristic) {
                when (uuid) {
                    UUID.fromString(CHARACTERISTIC_UUID) -> {
                        var result = ""
                        value.forEach {
                            result = "$result$it "
                        }
                        proximityBleResult!!.proximity = result
                        coroutineScope.launch {
                            proximityData.emit(Resource.Success(proximityBleResult))
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    fun startReadingRssi(){
        coroutineScope.launch {
            while(gatt != null){
                try{
                    gatt?.readRemoteRssi()
                }catch(e: Exception){
                    proximityData.emit(Resource.Error(message = "Error reading rssi value. Are you close enough to the beacon?"))
                    break
                }
            }
        }
    }


    override fun startReceiving() {
        try {
            coroutineScope.launch {
                proximityData.emit(Resource.Loading(message = "Scanning for BLE devices..."))
            }
            isScanning = true
            bleScanner.startScan(null, scanSettings, scanCallback)

            coroutineScope.launch {
                delay(25000)
                if (isScanning) {
                    isScanning = false
                    bleScanner.stopScan(scanCallback)
                    proximityData.emit(Resource.Error("Could not find any Ble devices"))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
            coroutineScope.launch {
                proximityData.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun findCharacteristic(
        serviceUUID: String,
        characteristicUUID: String
    ): BluetoothGattCharacteristic? {
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristic ->
            characteristic.uuid.toString() == characteristicUUID
        }
    }

    private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val cccdUUID = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

        characteristic.getDescriptor(cccdUUID)?.let { cccdDescriptor ->
            if (gatt?.setCharacteristicNotification(characteristic, true) == false) {
                coroutineScope.launch {
                    proximityData.emit(Resource.Error("Could not subscribe to proximity data notifications"))
                }
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        gatt?.let {
            descriptor.value = payload
            Log.d("TAG", "Result of descriptor ${it.writeDescriptor(descriptor)}")
        } ?: error("Not connected to a BLE device!")
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID)
        if (characteristic != null) {
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
        gatt = null
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val cccdUUID = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUUID)?.let { cccdDescriptor ->
            if (gatt?.setCharacteristicNotification(characteristic, false) == false) {
                coroutineScope.launch {
                    proximityData.emit(Resource.Error("Could not subscribe to proximity data notifications"))
                }
                return
            }

            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }
}