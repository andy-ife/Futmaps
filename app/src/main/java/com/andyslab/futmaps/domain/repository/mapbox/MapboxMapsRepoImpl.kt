package com.andyslab.futmaps.domain.repository.mapbox

import android.graphics.Bitmap
import android.graphics.Color
import com.andyslab.futmaps.data.NavigationTools.pointAnnotationManager
import com.andyslab.futmaps.domain.entities.FutLocation
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport

class MapboxMapsRepoImpl: MapboxMapsRepo {
    override fun enableUserPuck(mapView: MapView){
        with(mapView){
            location.locationPuck = createDefault2DPuck(withBearing = false)
            location.enabled = true
            location.puckBearing = PuckBearing.COURSE
            viewport.transitionTo(
                targetState = viewport.makeFollowPuckViewportState(),
                transition = viewport.makeImmediateViewportTransition()
            )
        }
    }

    override fun enableUserPuckForNavigation(mapView: MapView) {
        with(mapView){
            location.locationPuck = createDefault2DPuck(withBearing = false)
            location.enabled = true
            location.puckBearing = PuckBearing.COURSE
        }
    }

    override fun idleViewPort(mapView: MapView) {
        mapView.viewport.idle()
    }

    override fun drawAnnotation(mapView: MapView, icon: Bitmap, location: FutLocation): PointAnnotation {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        // Set options for the resulting symbol layer.
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(com.mapbox.geojson.Point.fromLngLat(location.longitude, location.latitude))
            // Specify the bitmap you assigned to the point annotation
            // The bitmap will be added to map style automatically.
            .withIconImage(icon)
            .withTextColor(Color.parseColor("#FFDE3B21"))
            .withTextSize(10.0)
            .withTextRadialOffset(1.0)
            .withTextEmissiveStrength(1.0)
            .withTextHaloColor(Color.parseColor("#FFFFFFFF"))
            .withTextHaloBlur(2.0)
            .withTextHaloWidth(0.5)
            .withTextAnchor(TextAnchor.LEFT)
            .withTextField(location.name)
        // Add the resulting pointAnnotation to the map.
        pointAnnotationManager.create(pointAnnotationOptions).also{
            return it
        }
    }

    override fun deleteAnnotation(pointAnnotation: PointAnnotation) {
        pointAnnotationManager.delete(pointAnnotation)
    }
}