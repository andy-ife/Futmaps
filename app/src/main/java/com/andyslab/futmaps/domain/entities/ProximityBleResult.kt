package com.andyslab.futmaps.domain.entities

data class ProximityBleResult(
    val name: String,
    val type: String,
    val address: String,
    var proximity: String?,
)
