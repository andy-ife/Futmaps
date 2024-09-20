package com.andyslab.futmaps.domain.entities

import com.andyslab.futmaps.R
import kotlinx.serialization.Serializable

@Serializable
data class FutLocation(
    var objectID: String = "",
    var name: String = "",
    var tag: String = "",
    var latitude: Double = 9.0820,
    var longitude: Double = 8.6753,
    var type: String = "",
    var isOpen: Boolean = true,
    var photos: List<Int> = listOf(),
    var icon: Int = R.drawable.generic_office,
    var description: String = "",
)
