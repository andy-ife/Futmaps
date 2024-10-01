package com.andyslab.futmaps.domain.repository.mapbox

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import com.andyslab.futmaps.R
import com.andyslab.futmaps.data.NavigationTools
import com.andyslab.futmaps.utils.Resource
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.formatter.Rounding.INCREMENT_TEN
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MapboxNavigationRepoImpl(val context: Context, val owner: LifecycleOwner):
    MapboxNavigationRepo {
        init{
            NavigationTools.tripProgressFormatter = initTripProgressUpdateFormatter()
            NavigationTools.tripProgressApi = MapboxTripProgressApi(NavigationTools.tripProgressFormatter)
        }
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    override fun initMapboxNavigation() {
        //setUp MapboxNavigationApp
        if(!MapboxNavigationApp.isSetup()){
            MapboxNavigationApp.setup{
                NavigationOptions.Builder(context).build()
            }
        }
        //attach lifecycle owner
        MapboxNavigationApp.attach(owner)

        //register observers
        val observer = FutmapsNavigationObserver()//application context can be found in this class in
        MapboxNavigationApp.registerObserver(observer)//mapboxNavigation.navigationOptions.applicationContext
    }

    override fun deinitMapboxNavigation() {
        MapboxNavigationApp.detach(owner)
        MapboxNavigationApp.disable()
    }

    override fun startTripSession() {
        val mapBoxNavigation = MapboxNavigationApp.current()
        //permission check
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mapBoxNavigation?.startTripSession()
    }

    override fun stopTripSession() {
        val mapboxNavigation = MapboxNavigationApp.current()
        mapboxNavigation?.stopTripSession()
    }

    override fun initViewPortDataSource(mapboxMap: MapboxMap): MapboxNavigationViewportDataSource {
         MapboxNavigationViewportDataSource(mapboxMap).also{
             it.overviewPadding = overviewPadding
             it.followingPadding = followingPadding
             it.followingPitchPropertyOverride(40.0)
             return it
        }
    }

    override suspend fun attachRouteObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        routeLineApi: MapboxRouteLineApi,
        routeLineView: MapboxRouteLineView,
        mapboxMap: MapboxMap,
        mapboxNavigation: MapboxNavigation
        ): RoutesObserver{
            RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
                routeLineApi.setNavigationRoutes(
                    newRoutes = routeUpdateResult.navigationRoutes,
                    alternativeRoutesMetadata = mapboxNavigation.getAlternativeMetadataFor(
                        routeUpdateResult.navigationRoutes
                    )
                ){value ->
                    routeLineView.renderRouteDrawData(
                        mapboxMap.style!!,
                        value
                    )
                }
                // update the camera position to account for the new route
                viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
                viewportDataSource.evaluate()
            } else {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        mapboxMap.style!!,
                        value
                    )
                }
                // remove the route reference from camera position evaluations
                viewportDataSource.clearRouteData()
                viewportDataSource.evaluate()
            }

    }.also { return it }
    }

    override fun attachLocationObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        camera: NavigationCamera,
        navigationLocationProvider: NavigationLocationProvider): LocationObserver {

        val locationObserver = object: LocationObserver {
            var firstLocationUpdateReceived = false

            override fun onNewRawLocation(rawLocation: Location) {
                // not handled
            }

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                val enhancedLocation = locationMatcherResult.enhancedLocation
                // update location puck's position on the map
                navigationLocationProvider.changePosition(
                    location = enhancedLocation,
                    keyPoints = locationMatcherResult.keyPoints,
                )

                // update camera position to account for new location
                viewportDataSource.onLocationChanged(enhancedLocation)
                viewportDataSource.evaluate()

                // if this is the first location update the activity has received,
                // it's best to immediately move the camera to the current user location
                if (!firstLocationUpdateReceived) {
                    firstLocationUpdateReceived = true
                    camera.requestNavigationCameraToOverview(
                        stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                            .maxDuration(0) // instant transition
                            .build()
                    )
                }
            }
        }
        return locationObserver
    }

    override fun attachRouteProgressObserver(
        viewportDataSource: MapboxNavigationViewportDataSource,
        tripProgressApi: MapboxTripProgressApi, )
    : RouteProgressObserver{
        RouteProgressObserver { routeProgress ->
            //update my trusty tripProgress instance
            val tripProgress = tripProgressApi.getTripProgress(routeProgress)
            CoroutineScope(Dispatchers.Default).launch {
                NavigationTools.tripProgress.emit(tripProgress)
            }
            // update the camera position to account for the progressed fragment of the route
            viewportDataSource.onRouteProgressChanged(routeProgress)
            viewportDataSource.evaluate()
        }.also{return it}
    }

    override fun initNavigationCamera(
        mapboxMap: MapboxMap,
        mapView: MapView,
        viewportDataSource: MapboxNavigationViewportDataSource
    ): NavigationCamera {
        // initialize Navigation Camera
        val navigationCamera = NavigationCamera(
            mapboxMap,
            mapView.camera,
            viewportDataSource
        )
        return navigationCamera
    }

    override fun initRouteLine(mapStyle: String): MapboxRouteLineViewOptions{
        val routeLineColors = RouteLineColorResources.Builder()
            .routeDefaultColor(Color.parseColor("#FF965ca4"))
            .routeLineTraveledColor((Color.parseColor("#FFC8a4d1")))
            .build()

        MapboxRouteLineViewOptions.Builder(context)
            .routeLineBelowLayerId(mapStyle)
            .routeLineColorResources(routeLineColors)
            .destinationWaypointIcon(R.drawable.location_circle)
            .build()
            .also{
                return it
            }
    }

    override suspend fun fetchDriveRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinatesList: List<Point>
    ): Flow<Resource<List<NavigationRoute>>>{
        return flow{
            var isError = false
            var msg = ""
            var result: List<NavigationRoute> = listOf()
            emit(Resource.Loading())
            try{
                mapboxNavigation.requestRoutes(
                    RouteOptions.builder()
                        .applyDefaultNavigationOptions()
                        .applyLanguageAndVoiceUnitOptions(context)
                        .alternatives(false)
                        .coordinatesList(coordinatesList)
                        .layersList(listOf(mapboxNavigation.getZLevel(), null))
                        .build(),

                    object : NavigationRouterCallback {
                        override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            @RouterOrigin routerOrigin: String
                        ) {
                            // disable navigation camera
                            camera.requestNavigationCameraToIdle()
                            // set a route to receive route progress updates and provide a route reference
                            // to the viewport data source (via RoutesObserver)
                            mapboxNavigation.setNavigationRoutes(routes)
                            // enable the camera back
                            camera.requestNavigationCameraToOverview()

                            result = routes.also{
                                isError = it.isEmpty()
                            }
                        }

                        override fun onFailure(
                            reasons: List<RouterFailure>,
                            routeOptions: RouteOptions
                        ) {
                            isError = true
                        }

                        override fun onCanceled(
                            routeOptions: RouteOptions,
                            @RouterOrigin routerOrigin: String
                        ) {
                            isError = true
                        }
                    }
                ).also{emit(Resource.Loading())}

                if(isError){
                    emit(Resource.Error("Route not found, failed, or was cancelled."))
                }
                else{
                    camera.requestNavigationCameraToOverview()
                    delay(4000)//give mapbox time to do it's wonky-ass animations
                    emit(Resource.Success(result))
                }
        }catch (e: Exception){
            isError = true
                msg = e.toString()
        }
            if(isError){
                emit(Resource.Error(msg))
            }
    }
    }

    override suspend fun fetchWalkRoutes(
        mapboxNavigation: MapboxNavigation,
        camera: NavigationCamera,
        coordinatesList: List<Point>
    ): Flow<Resource<List<NavigationRoute>>> {
        return flow{
            var isError = false
            var msg = ""
            var result: List<NavigationRoute> = listOf()
            emit(Resource.Loading())
            try{
                mapboxNavigation.requestRoutes(
                    RouteOptions.builder()
                        .applyDefaultNavigationOptions(
                            profile = DirectionsCriteria.PROFILE_WALKING  //walking routes
                        )
                        .applyLanguageAndVoiceUnitOptions(context)
                        .alternatives(true)
                        .coordinatesList(coordinatesList)
                        .layersList(listOf(mapboxNavigation.getZLevel(), null))
                        .build(),

                    object : NavigationRouterCallback {
                        override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            @RouterOrigin routerOrigin: String
                        ) {
                            // disable navigation camera
                            camera.requestNavigationCameraToIdle()
                            // set a route to receive route progress updates and provide a route reference
                            // to the viewport data source (via RoutesObserver)
                            mapboxNavigation.setNavigationRoutes(routes)
                            // enable the camera back
                            camera.requestNavigationCameraToOverview()

                            result = routes.also{
                                isError = it.isEmpty()
                            }
                        }

                        override fun onFailure(
                            reasons: List<RouterFailure>,
                            routeOptions: RouteOptions
                        ) {
                            isError = true
                        }

                        override fun onCanceled(
                            routeOptions: RouteOptions,
                            @RouterOrigin routerOrigin: String
                        ) {
                            isError = true
                        }
                    }
                ).also{emit(Resource.Loading())}

                if(isError){
                    emit(Resource.Error("Route not found, failed, or was cancelled."))
                }
                else{
                    camera.requestNavigationCameraToOverview()
                    delay(4000)//give mapbox time to do it's wonky-ass animations
                    emit(Resource.Success(result))
                }
            }catch (e: Exception){
                isError = true
                msg = e.toString()
            }
            if(isError){
                emit(Resource.Error(msg))
            }
        }
    }

    override fun initTripProgressUpdateFormatter(): TripProgressUpdateFormatter {
        val distanceFormatterOptions =
            DistanceFormatterOptions.Builder(context)
                .locale(java.util.Locale.UK)
                .unitType(UnitType.METRIC)
                .roundingIncrement(INCREMENT_TEN)
                .build()

        val result = TripProgressUpdateFormatter.Builder(context)
            .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
            .timeRemainingFormatter(TimeRemainingFormatter(context, java.util.Locale.UK))
            .estimatedTimeToArrivalFormatter(
                EstimatedTimeToArrivalFormatter(
                    context,
                    TimeFormat.TWENTY_FOUR_HOURS
            ))
            .build()

        return result
        }
    }

//mapbox navigation observer implementation
class FutmapsNavigationObserver(): MapboxNavigationObserver{
    private val locationObserver = FutmapsLocationObserver()

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }
}

//location observer implementation
class FutmapsLocationObserver(): LocationObserver{
    override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {

    }

    override fun onNewRawLocation(rawLocation: Location) {

    }
}