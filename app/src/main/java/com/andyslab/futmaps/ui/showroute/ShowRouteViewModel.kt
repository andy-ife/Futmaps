package com.andyslab.futmaps.ui.showroute

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyslab.futmaps.data.NavigationTools
import com.andyslab.futmaps.data.NavigationTools.locationObserver
import com.andyslab.futmaps.data.NavigationTools.replayProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routeLineApi
import com.andyslab.futmaps.data.NavigationTools.routeLineView
import com.andyslab.futmaps.data.NavigationTools.routeProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routesObserver
import com.andyslab.futmaps.domain.entities.TripData
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.domain.repositories.mapbox.MapboxMapsRepoImpl
import com.andyslab.futmaps.domain.repositories.mapbox.MapboxNavigationRepoImpl
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.andyslab.futmaps.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateValue
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class ShowRouteViewModel(
    private val navigationRepo: MapboxNavigationRepoImpl,
    private val fusedLocationProviderClient: FusedLocationProviderClient)
    : ViewModel(){

        private val _routeUiState: MutableStateFlow<Resource<List<NavigationRoute>>?> = MutableStateFlow(null)
    val routeUiState = _routeUiState.asStateFlow()

    private val _userTripDataState = MutableStateFlow(UserProfile.instance.tripData)
    val userTripDataState = _userTripDataState.asStateFlow()

    private val _tripProgress = MutableStateFlow<TripProgressUpdateValue?>(null)
    val tripProgress = _tripProgress.asStateFlow()

    private val _lastUserLocation = MutableStateFlow(UserProfile.instance.lastKnownCoordinates)
    val lastUserLocation = _lastUserLocation.asStateFlow()

    private val mapsRepo = MapboxMapsRepoImpl()

    private val fetchRoutesJob = Job()

    init{
        getLastUserLocation({
            UserProfile.instance.lastKnownCoordinates = it
            _lastUserLocation.value = it
        }){
            fetchRoutesJob.cancel("Couldn't get current user location")//stop fetching the routes
            _routeUiState.value = Resource.Error("Could not get your current location. Restart navigation.")
        }

        viewModelScope.launch{
            NavigationTools.tripProgress.collect{
                _tripProgress.value = it
            }
        }
    }

    //medium.com
    @SuppressLint("MissingPermission")
    fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit
    ) {
        // Retrieve the last known location
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // If location is not null, invoke the success callback with latitude and longitude
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }
                }
                .addOnFailureListener { exception ->
                    // If an error occurs, invoke the failure callback with the exception
                    onGetLastLocationFailed(exception)
                }
    }

    fun retryFetch(){
        _routeUiState.value = null//supposed to trigger recomposition. Doesn't make map effect run again though.
    }

    fun initViewportDataSource(mapboxMap: MapboxMap): MapboxNavigationViewportDataSource{
        navigationRepo.initViewPortDataSource(mapboxMap).also{return it}
    }

    fun initCamera(
        mapView: MapView,
        mapboxMap: MapboxMap,
        viewportDataSource: MapboxNavigationViewportDataSource
    ): NavigationCamera{
        navigationRepo.initNavigationCamera(mapboxMap, mapView, viewportDataSource).also{return it}
    }

    fun initRouteLine(mapStyle: String): MapboxRouteLineViewOptions{
        navigationRepo.initRouteLine(mapStyle).also{
            return it
        }
    }

    fun attachRouteObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        mapboxMap: MapboxMap,
        mapboxNavigation: MapboxNavigation,
        routeLineApi: MapboxRouteLineApi,
        routeLineView: MapboxRouteLineView): RoutesObserver{
        var observer: RoutesObserver
        runBlocking {
            observer = navigationRepo.attachRouteObserver(
                viewportDataSource, routeLineApi, routeLineView, mapboxMap, mapboxNavigation
            )
        }
        return observer
    }

    fun attachLocationsObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        camera: NavigationCamera,
        provider: NavigationLocationProvider): LocationObserver{
        navigationRepo.attachLocationObserver(viewportDataSource, camera, provider).also{
            return it
        }
    }

    fun attachRouteProgressObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        tripProgressApi: MapboxTripProgressApi, ): RouteProgressObserver{
        navigationRepo.attachRouteProgressObserver(viewportDataSource, tripProgressApi).also{
            return it
        }
    }

    fun fetchDriveRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinateList: List<Point>,
        onSuccess: () -> Unit){
        viewModelScope.launch(fetchRoutesJob){
            navigationRepo.fetchDriveRoutes(
                mapboxNavigation,
                camera,
                coordinateList,
            ).collect{resource ->
                when(resource){
                    is Resource.Error ->
                        _routeUiState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading ->
                        _routeUiState.value = Resource.Loading()
                    is Resource.Success ->
                        {
                        _routeUiState.value = Resource.Success(resource.data)
                        onSuccess()
                    }
                }
            }
        }
    }

    fun fetchWalkRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinateList: List<Point>,
        onSuccess: () -> Unit){
        viewModelScope.launch(fetchRoutesJob){
            navigationRepo.fetchWalkRoutes(
                mapboxNavigation,
                camera,
                coordinateList,
            ).collect{resource ->
                when(resource){
                    is Resource.Error ->
                        _routeUiState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading ->
                        _routeUiState.value = Resource.Loading()
                    is Resource.Success ->
                    {
                        _routeUiState.value = Resource.Success(resource.data)
                        onSuccess()
                    }
                }
            }
        }
    }

    fun initTripProgressUpdateFormatter(): TripProgressUpdateFormatter{
        return navigationRepo.initTripProgressUpdateFormatter()
    }

    fun enableUserPuck(mapView: MapView){
        mapsRepo.enableUserPuck(mapView)
    }

    fun enableUserPuckForNavigation(mapView: MapView){
        mapsRepo.enableUserPuckForNavigation(mapView)
    }

    fun idleViewport(mapView: MapView){
        mapsRepo.idleViewPort(mapView)
    }

    fun toggleTripMode(driveMode: Boolean){
        if(driveMode){
            UserProfile.instance.tripData.driveMode = true
            val copy = UserProfile.instance.tripData.copy()
            _userTripDataState.value = copy
        }
        else{
            UserProfile.instance.tripData.driveMode = false
            val copy = UserProfile.instance.tripData.copy()
            _userTripDataState.value = copy
        }
    }

    fun unRegisterObservers(){
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
        MapboxNavigationApp.current()?.unregisterRoutesObserver(routesObserver)
        MapboxNavigationApp.current()?.unregisterRouteProgressObserver(routeProgressObserver)
        MapboxNavigationApp.current()?.unregisterRouteProgressObserver(replayProgressObserver)
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    fun startTripSession(){
        navigationRepo.startTripSession()
    }

    fun stopTripSession(){
        navigationRepo.stopTripSession()
    }
}

