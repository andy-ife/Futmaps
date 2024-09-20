package com.andyslab.futmaps.domain.repositories.mapbox

import com.andyslab.futmaps.utils.Resource
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.flow.Flow

interface MapboxNavigationRepo {
    //initialization
    fun initMapboxNavigation()
    fun deinitMapboxNavigation()

    //trip session
    fun startTripSession()
    fun stopTripSession()

    //init data source to be provided to camera
    fun initViewPortDataSource(mapboxMap: MapboxMap): MapboxNavigationViewportDataSource

    //attach route observer to data source
    suspend fun attachRouteObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        routeLineApi: MapboxRouteLineApi,
        routeLineView: MapboxRouteLineView,
        mapboxMap: MapboxMap,
        mapboxNavigation: MapboxNavigation): RoutesObserver

    //attach location observer to data source
    fun attachLocationObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        camera: NavigationCamera,
        navigationLocationProvider: NavigationLocationProvider
    ): LocationObserver

    //attach routeprogress observer to data source
    fun attachRouteProgressObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        tripProgressApi: MapboxTripProgressApi, ): RouteProgressObserver

    //init camera with a map, a view, and a data source.
    fun initNavigationCamera(
        mapboxMap: MapboxMap,
        mapView: MapView,
        viewportDataSource: MapboxNavigationViewportDataSource): NavigationCamera

    //init route line
    fun initRouteLine(mapStyle: String): MapboxRouteLineViewOptions

    //get routes
    suspend fun fetchDriveRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinatesList: List<Point>): Flow<Resource<List<NavigationRoute>>>

    suspend fun fetchWalkRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinatesList: List<Point>): Flow<Resource<List<NavigationRoute>>>

    //trip progress data
    fun initTripProgressUpdateFormatter(): TripProgressUpdateFormatter
}