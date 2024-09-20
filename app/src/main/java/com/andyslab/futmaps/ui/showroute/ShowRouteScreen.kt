package com.andyslab.futmaps.ui.showroute

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.data.NavigationTools.hasObservers
import com.andyslab.futmaps.data.NavigationTools.locationObserver
import com.andyslab.futmaps.data.NavigationTools.navCamera
import com.andyslab.futmaps.data.NavigationTools.replayProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routeLineApi
import com.andyslab.futmaps.data.NavigationTools.routeLineView
import com.andyslab.futmaps.data.NavigationTools.routeProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routesObserver
import com.andyslab.futmaps.data.NavigationTools.tripProgressApi
import com.andyslab.futmaps.data.NavigationTools.viewportDataSource
import com.andyslab.futmaps.domain.entities.TripData
import com.andyslab.futmaps.domain.repositories.mapbox.MapboxNavigationRepoImpl
import com.andyslab.futmaps.ui.ErrorDialog
import com.andyslab.futmaps.ui.LoadingDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.ui.home.HomeScreenMapButton
import com.andyslab.futmaps.utils.ExpandedType
import com.andyslab.futmaps.utils.Resource
import com.andyslab.futmaps.utils.findActivity
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, MapboxExperimental::class,
    ExperimentalPreviewMapboxNavigationAPI::class
)
@Composable
fun ShowRouteScreen(navController: NavHostController){
    val context = LocalContext.current
    val navigationRepo = MapboxNavigationRepoImpl(context, context.findActivity())
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val viewModel = ShowRouteViewModel(navigationRepo, fusedLocationProviderClient)
    val tripProgress by viewModel.tripProgress.collectAsState()

    var currentTripData by remember{
        mutableStateOf(TripData())
    }

    val lastUserLocation by viewModel.lastUserLocation.collectAsState()

    val userTripState by viewModel.userTripDataState.collectAsState()
    val routeUiState by viewModel.routeUiState.collectAsState()

    val coordinateList = listOf(
        Point.fromLngLat(lastUserLocation.second, lastUserLocation.first),
        Point.fromLngLat(userTripState.destination.longitude, userTripState.destination.latitude)
    )

    val tabItems = setOf(
        TabItem(
            title = "Drive",
            unselectedIcon = R.drawable.drive_icon_outlined,
            selectedIcon = R.drawable.drive_icon_filled
            ),
        TabItem(
            title = "Walk",
            unselectedIcon = R.drawable.walk_outline,
            selectedIcon = R.drawable.walk_icon_filled
        )
    )

    val mapViewPortState = MapViewportState()

    val sheetState = rememberBottomSheetState(
        initialValue = ExpandedType.HALF,
        defineValues = {
            ExpandedType.COLLAPSED at offset(80)
            ExpandedType.HALF at height(250.dp)
            ExpandedType.FULL at contentHeight
        }
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        sheetState = sheetState
    )

    var isLoading by remember{
        mutableStateOf(true)
    }

    var isError by remember{
        mutableStateOf(false)
    }

    var enableUserFollowing by remember{
        mutableStateOf(false)
    }

    var tripStarted by remember{
        mutableStateOf(false)
    }

    var showCancelTripDialog by remember{
        mutableStateOf(false)
    }

    val tripProgressScope = rememberCoroutineScope()

    LaunchedEffect(tripStarted) {
        if(tripStarted)
        {
            sheetState.animateTo(ExpandedType.COLLAPSED)
        }

        tripProgressScope.launch {
            viewModel.tripProgress.collect{
                if(it != null){
                    currentTripData = TripData(
                        timeToDest = it.totalTimeRemaining,
                        distanceToDest = it.distanceRemaining
                    )
                }
            }
        }

        //classic collectAsState() not causing recompositions, forcing me to use this ass method
        viewModel.routeUiState.collect{ resource ->
            when(resource){
                is Resource.Error -> {
                    isError = true
                    isLoading = false
                }
                is Resource.Loading -> {
                    isLoading = true
                    isError = false
                }
                is Resource.Success -> {
                    isLoading = false
                    isError = false
                }
                else -> {}
            }
        }

    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent),){
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            if(!tripStarted){
            StartNavigationBottomSheet(
            tripState = currentTripData.also{
                it.driveMode = userTripState.driveMode
            },
            cancel = {
                viewModel.unRegisterObservers()
                viewModel.stopTripSession()
                navController.navigate(Screen.HomeScreen.route){
                    popUpTo(Screen.HomeScreen.route){
                        inclusive = true
                    }
                }
            },
            startNav = {
                enableUserFollowing = true
                navCamera.requestNavigationCameraToIdle()
                viewportDataSource.evaluate()
                navCamera.requestNavigationCameraToFollowing()
                viewportDataSource.evaluate()
                //mapViewPortState.transitionToFollowPuckState()
                tripStarted = true
            }
        )}else{
            TripProgressBottomSheet(
                tripData = currentTripData,
                onCancel = {
                    showCancelTripDialog = true
                }
            )
        }
                       },
        modifier = Modifier.fillMaxSize(),
        sheetShape = if(!tripStarted)BottomSheetDefaults.ExpandedShape else RectangleShape,
        sheetContainerColor = Color.White,
        sheetSwipeEnabled = false,
        topBar = {
            if(!tripStarted){
            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                //.shadow(2.dp)
                .background(Color.White)
                .padding(start = 10.dp, end = 10.dp, top = 40.dp,),
                verticalArrangement = Arrangement.spacedBy(20.dp)){
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        painter = painterResource(R.drawable.target_icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF672976)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = "Your Location",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        fontWeight = FontWeight.Light,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF672976))

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.location_pin),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFF7471B)
                    )

                    Text(
                        text = userTripState.destination.name,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                //Spacer(modifier = Modifier.height(60.dp))

                var selectedTabIndex by remember {
                    mutableIntStateOf(0)
                }
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    ) {
                    tabItems.forEachIndexed { index, tabItem ->
                        Tab(selected = index == selectedTabIndex,
                            onClick = {
                                val bool = index == 0 //true for drive tab, false for walk tab
                                selectedTabIndex = index
                                if(userTripState.driveMode != bool)
                                    viewModel.toggleTripMode(bool)
                                      },
                            text = {
                                Text(
                                    text = tabItem.title,
                                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular),),
                                )
                            },
                            icon = {
                                val id = if(index == selectedTabIndex)tabItem.selectedIcon else tabItem.unselectedIcon
                                Icon(
                                    painter = painterResource(id),
                                    contentDescription = "tab item",
                                    modifier = Modifier.offset(y=4.dp),)
                            },
                            selectedContentColor = Color(0xFf672976),
                            unselectedContentColor = Color(0xFF606060))
                    }
                }

            }
            }else{
                Box(modifier = Modifier.background(Color.Transparent))
            }
        }
        ) {

        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.TopEnd){
            MapboxMap(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = -120.dp),
                compass = {
                    Compass(alignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(
                                x = -4.dp, y = 110.dp
                            ),
                        fadeWhenFacingNorth = false)
                },
                mapViewportState = mapViewPortState,
                style = { MapStyle(style = Style.MAPBOX_STREETS) }
                ){
                //LaunchedEffect type block that exposes raw MapView object.
                //All APIs can be used inside the MapEffect.
                MapEffect(key1 = enableUserFollowing, key2 = userTripState) { mapView ->
                    if(!enableUserFollowing){
                        viewModel.stopTripSession()
                        viewportDataSource = viewModel.initViewportDataSource(mapView.mapboxMap)
                        //observers, viewportdatasource, and camera referenced in this code are
                        // IMPORTED from my custom NavigationTools singleton. I did this to
                        //have the same set of nav tools to use in different parts of the app.
                        //also the code was incredibly long and would clutter my composable
                        val tripProgressFormatter = viewModel.initTripProgressUpdateFormatter()
                        tripProgressApi = MapboxTripProgressApi(tripProgressFormatter)
                        routeProgressObserver = viewModel.attachRouteProgressObserver(viewportDataSource, tripProgressApi)

                        val routeLineOptions = viewModel.initRouteLine(Style.MAPBOX_STREETS)
                        val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
                            val result = routeLineApi.updateTraveledRouteLine(point)
                            routeLineView.renderRouteLineUpdate(mapView.mapboxMap.style!!, result)
                        }

                        mapView.location.addOnIndicatorPositionChangedListener(onPositionChangedListener)

                        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
                        routeLineView = MapboxRouteLineView(routeLineOptions)

                        navCamera = viewModel.initCamera(mapView, mapView.mapboxMap, viewportDataSource)

                        routesObserver = MapboxNavigationApp.current()?.let { it1 ->
                            viewModel.attachRouteObserver(
                                viewportDataSource, mapView.mapboxMap, it1, routeLineApi, routeLineView)
                        }!!

                        locationObserver = viewModel.attachLocationsObserver(
                            viewportDataSource, navCamera, NavigationLocationProvider()
                        )

                        replayProgressObserver = MapboxNavigationApp.current()?.mapboxReplayer.let { it1 ->
                            ReplayProgressObserver(
                                it1!!
                            )
                        }
                        hasObservers = true

                        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS){
                        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
                        MapboxNavigationApp.current()?.registerRoutesObserver(routesObserver)
                        MapboxNavigationApp.current()?.registerRouteProgressObserver(routeProgressObserver)
                        MapboxNavigationApp.current()?.registerRouteProgressObserver(replayProgressObserver)

                        viewModel.startTripSession()

                            if(MapboxNavigationApp.current()?.getNavigationRoutes()?.isEmpty() == true) {
                                if(userTripState.driveMode){
                                    viewModel.fetchDriveRoutes(
                                        MapboxNavigationApp.current()!!,
                                        navCamera,
                                        coordinateList,
                                    ){}
                                }else{
                                    viewModel.fetchWalkRoutes(
                                        MapboxNavigationApp.current()!!,
                                        navCamera,
                                        coordinateList,
                                    ){}
                                }
                        } else {
                        // clear the routes
                            MapboxNavigationApp.current()?.setNavigationRoutes(listOf())
                            //fetch new ones
                                if(userTripState.driveMode){
                                    viewModel.fetchDriveRoutes(
                                        MapboxNavigationApp.current()!!,
                                        navCamera,
                                        coordinateList
                                    ){

                                    }
                                }else{
                                    viewModel.fetchWalkRoutes(
                                        MapboxNavigationApp.current()!!,
                                        navCamera,
                                        coordinateList,
                                    ){}
                                }
                    }
                    }
                }else{
                    viewModel.enableUserPuckForNavigation(mapView)
                }
                }
            }
            
            AnimatedVisibility(visible = tripStarted){
                HomeScreenMapButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = 300.dp, x = -10.dp),
                    iconResId = R.drawable.outline_map_24){
                    if(navCamera.state == NavigationCameraState.FOLLOWING){
                        mapViewPortState.idle()
                        navCamera.requestNavigationCameraToIdle()
                        viewportDataSource.evaluate()
                        navCamera.requestNavigationCameraToOverview()
                        viewportDataSource.evaluate()
                    }

                    if(navCamera.state == NavigationCameraState.OVERVIEW){
                        navCamera.requestNavigationCameraToIdle()
                        viewportDataSource.evaluate()
                        navCamera.requestNavigationCameraToFollowing()
                        viewportDataSource.evaluate()
                        //mapViewPortState.transitionToFollowPuckState()
                    }
                }
            }

            if(showCancelTripDialog){
                ShowCancelTripDialog(
                    onDismiss = {showCancelTripDialog = false},
                    onCancelTripClick = {
                        viewModel.stopTripSession()
                        viewModel.unRegisterObservers()
                        showCancelTripDialog = false
                        navController.navigate(Screen.HomeScreen.route){
                            popUpTo(Screen.HomeScreen.route){
                                inclusive = true
                            }
                        }
                    }
                )
            }

        }
    }
        AnimatedVisibility(
            visible = (isLoading || isError),
            enter = slideInVertically(initialOffsetY = {it/2}),
            exit = slideOutVertically(targetOffsetY = {it/2})
        ){
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
                contentAlignment = Alignment.Center){

                if(isLoading){
                LoadingDialog(message = "Calculating route..."){
                    navController.navigate(Screen.HomeScreen.route){
                        popUpTo(Screen.ShowRouteScreen.route){
                            inclusive=true
                        }
                    }
                }
                }

                if(isError){
                    ErrorDialog(
                            message = (routeUiState as Resource.Error?)?.message.toString(),
                            onDismiss = {
                                navController.navigate(Screen.SearchScreen.route){
                                    popUpTo(Screen.ShowRouteScreen.route){
                                        inclusive = true
                                    }
                                }
                            }) {
                            viewModel.retryFetch()
                        }

                }
            }
        }
}
}

@Composable
@Preview
fun Prev(){
    ShowRouteScreen(navController = rememberNavController())
}

data class TabItem(
    val title: String,
    @DrawableRes val unselectedIcon: Int,
    @DrawableRes val selectedIcon: Int,
)