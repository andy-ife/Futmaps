package com.andyslab.futmaps.ui.requestlocationpermission

import android.Manifest
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestPermissionViewModel: ViewModel() {
    private val _visiblePermissionDialog = MutableStateFlow("")
    val visiblePermissionDialog = _visiblePermissionDialog.asStateFlow()

    fun onDismissDialog(){
        _visiblePermissionDialog.value = ""
    }

    fun onPermissionResult(permission: String, isGranted: Boolean){
        if(!isGranted){
            _visiblePermissionDialog.value = permission
        }
    }
}