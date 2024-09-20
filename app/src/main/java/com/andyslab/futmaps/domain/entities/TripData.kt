package com.andyslab.futmaps.domain.entities

data class TripData(
    var driveMode: Boolean = true,
    var destination: FutLocation = FutLocation(name="Nowhere"),
    var timeToDest: Double = 36.0,
    var distanceToDest: Double = 3000.0,
    )
