package com.andyslab.futmaps.ui.indoornavigation

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.andyslab.futmaps.domain.entities.ProximityBleResult
import com.andyslab.futmaps.domain.repository.ble.ProximityBLERepoImpl
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BLEScanViewModel(private val application: Application): AndroidViewModel(application) {
    private val _bleScanState = MutableStateFlow<BleScanState?>(null)
    val bleScanState = _bleScanState.asStateFlow()

    private val _currentProximity = MutableStateFlow("")
    val currentProximity = _currentProximity.asStateFlow()

    private val _visiblePermissionDialog = MutableStateFlow("")
    val visiblePermissionDialog = _visiblePermissionDialog.asStateFlow()

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    private val bleRepo = ProximityBLERepoImpl(adapter, application.applicationContext)

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                return BLEScanViewModel(application) as T
            }
        }
    }

    fun onDismissPermissionDialog(){
        _visiblePermissionDialog.value = ""
    }

    fun onPermissionResult(permission: String, isGranted: Boolean){
        if(!isGranted){
            _visiblePermissionDialog.value = permission
        }
    }

    private fun subscribeToProximityBleChanges(){
        viewModelScope.launch{
            bleRepo.proximityData.collect{ result ->
                when(result){
                    is Resource.Error -> {
                        _bleScanState.value = BleScanState.Disconnected(result.message!!)
                    }
                    is Resource.Loading -> {
                        _bleScanState.value = BleScanState.Connecting(result.message!!)
                    }
                    is Resource.Success -> {
                        if(result.data != null){
                            _bleScanState.value = BleScanState.Connected(result.data)
                            _currentProximity.value = result.data.proximity!!
                        }else{
                            _bleScanState.value = BleScanState.Disconnected("Disconnected")
                        }
                    }
                }
            }
        }
    }

    fun initializeConnection(){
        subscribeToProximityBleChanges()
        bleRepo.startReceiving()
    }

    fun reconnect(){
        bleRepo.reconnect()
    }

    fun disconnect(){
        _bleScanState.value = BleScanState.Disconnecting("Disconnecting...")
        bleRepo.disconnect()
        _bleScanState.value = BleScanState.Disconnected("Disconnected")
    }

    override fun onCleared() {
        super.onCleared()
        bleRepo.closeConnection()
    }

    fun bluetoothIsEnabled(): Boolean{
        return adapter.isEnabled
    }

    fun fakeConnect(){
        viewModelScope.launch{
            _bleScanState.value = BleScanState.Connecting("Scanning for bluetooth devices...")
            delay(7000)
            _bleScanState.value = BleScanState.Connecting("Connecting to device...")
            delay(3000)
            _bleScanState.value = BleScanState.Connecting("Discovering services...")
            delay(2000)
            _bleScanState.value = BleScanState.Connecting("Adjusting MTU space...")
            delay(1000)

            _bleScanState.value = BleScanState.Connected(
                ProximityBleResult(
                "Dean's office", "Bluetooth LE Device", "17:15:33:04:DC:01", "2m"
            )
            )

        }
    }

    fun fakeDisconnect(){
        viewModelScope.launch {
            _bleScanState.value = BleScanState.Disconnecting("Disconnecting...")
            delay(500)
            _bleScanState.value = BleScanState.Disconnected("Connection lost.")
        }
    }
}

sealed class BleScanState {
    data class Connected(val data: ProximityBleResult): BleScanState()
    data class Connecting(val message: String): BleScanState()
    data class Disconnected(val message: String) : BleScanState()
    data class Disconnecting(val message: String): BleScanState()
}