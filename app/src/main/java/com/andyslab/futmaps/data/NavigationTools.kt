package com.andyslab.futmaps.data

import android.content.Context
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateValue
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.flow.MutableSharedFlow

object NavigationTools {
    lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    lateinit var navCamera: NavigationCamera
    lateinit var routesObserver: RoutesObserver
    lateinit var locationObserver: LocationObserver
    lateinit var routeProgressObserver: RouteProgressObserver
    lateinit var replayProgressObserver: ReplayProgressObserver
    lateinit var routeLineApi: MapboxRouteLineApi
    lateinit var routeLineView: MapboxRouteLineView

    lateinit var tripProgressFormatter: TripProgressUpdateFormatter
    lateinit var tripProgressApi: MapboxTripProgressApi

    var tripProgress: MutableSharedFlow<TripProgressUpdateValue?> = MutableSharedFlow()

    lateinit var pointAnnotationManager: PointAnnotationManager

    var hasObservers = false
}