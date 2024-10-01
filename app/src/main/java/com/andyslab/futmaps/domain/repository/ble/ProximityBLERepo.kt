package com.andyslab.futmaps.domain.repository.ble

import com.andyslab.futmaps.domain.entities.ProximityBleResult
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface ProximityBLERepo {
    val proximityData: MutableSharedFlow<Resource<ProximityBleResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}