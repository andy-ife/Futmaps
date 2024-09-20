package com.andyslab.futmaps.domain.repositories.mapbox

import android.graphics.Bitmap
import com.andyslab.futmaps.domain.entities.FutLocation
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation

interface MapboxMapsRepo {
    fun enableUserPuck(mapView: MapView)
    fun enableUserPuckForNavigation(mapView: MapView)
    fun idleViewPort(mapView: MapView)
    fun drawAnnotation(mapView: MapView,
                       icon: Bitmap,
                       location: FutLocation): PointAnnotation
    fun deleteAnnotation(pointAnnotation: PointAnnotation)
}