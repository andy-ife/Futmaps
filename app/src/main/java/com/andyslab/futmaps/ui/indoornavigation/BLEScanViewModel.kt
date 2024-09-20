package com.andyslab.futmaps.ui.indoornavigation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BLEScanViewModel(private val bluetoothAdapter: BluetoothAdapter, ): ViewModel() {
    private val _bleScanState = MutableStateFlow("")

    private val _visiblePermissionDialog = MutableStateFlow("")
    val visiblePermissionDialog = _visiblePermissionDialog.asStateFlow()

    fun onDismissPermissionDialog(){
        _visiblePermissionDialog.value = ""
    }

    fun onPermissionResult(permission: String, isGranted: Boolean){
        if(!isGranted){
            _visiblePermissionDialog.value = permission
        }
    }


}