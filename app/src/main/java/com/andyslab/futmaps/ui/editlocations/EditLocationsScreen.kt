package com.andyslab.futmaps.ui.editlocations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.data.FirestoreProvider.futLocations
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.ui.home.getBitmap
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import io.morfly.compose.bottomsheet.material3.sheetVisibleHeightDp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class)
@Composable
fun EditLocationsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val viewModel = EditLocationsViewModel(fusedLocationProviderClient)

    val mapViewPortState = rememberMapViewportState {
        setCameraOptions {
            zoom(6.0)
            center(Point.fromLngLat(9.5836, 6.5463))
            pitch(0.0)
            bearing(0.0)
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val bottomSheetScope = rememberCoroutineScope()

    val name = remember{mutableStateOf("")}
    val shortDesc = remember{mutableStateOf("")}
    val lat = remember{ mutableDoubleStateOf(0.0) }
    val long = remember{ mutableDoubleStateOf(0.0) }

    val currentCoordinates by viewModel.lastUserLocation.collectAsState()
    var currentAnnotation by remember{
        mutableStateOf<PointAnnotation?>(null)
    }

    val locationPin = getBitmap(context, R.drawable.annotation_pin)
    val locationPinBlue = getBitmap(context, R.drawable.location_circle)

    var drawNewPin by remember{
        mutableStateOf(false)
    }

    var readOnly by remember{
        mutableStateOf(true)
    }

    var firstLaunch by remember{
        mutableStateOf(true)
    }

    //material3 bottom sheet. Not morfly's.
    BottomSheetScaffold(
        sheetContent = {
            EditLocationsBottomSheet(
                name = name,
                shortDesc = shortDesc,
                lat = lat,
                long = long,
                readOnly = readOnly,
                onSaveClick = {
                    if(name.value.isNotBlank() && lat.doubleValue!=0.0 && long.doubleValue!=0.0){
                        val futLocation = FutLocation(
                            objectID = viewModel.generateObjectId(),
                            name = name.value.trim(),
                            tag = shortDesc.value.trim(),
                            latitude = lat.doubleValue,
                            longitude = long.doubleValue,
                        )
                        viewModel.uploadNewLocation(context, futLocation)
                        Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
                        bottomSheetScope.launch {
                            sheetState.hide()
                        }
                    }else{
                        Toast.makeText(context, "Missing or invalid fields", Toast.LENGTH_SHORT).show()
                    }
                })
        },
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContainerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(text = "Add/Edit Locations",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular))
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.HomeScreen.route){
                            popUpTo(Screen.EditLocationsScreen.route){
                                inclusive = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "back",)
                    }
                },
                colors = TopAppBarColors(
                    containerColor = Color(0xFF31185f),
                    scrolledContainerColor = Color(0xFF31185F),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                )
            )
        },
        ) {

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter) {
            MapboxMap(
                modifier = Modifier
                    .fillMaxSize()
                ,
                compass = {
                    Compass(
                        alignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(
                                x = -4.dp,
                            ),
                    )
                },
                mapViewportState = mapViewPortState,
                onMapLongClickListener = { point ->
                    lat.doubleValue = point.latitude()
                    long.doubleValue = point.longitude()
                    drawNewPin = true
                    bottomSheetScope.launch {
                        sheetState.expand()
                    }
                    true
                },
                style = { MapStyle(style = Style.MAPBOX_STREETS) }
            ){
                //LaunchedEffect type block that exposes raw MapView object.
                //All APIs can be used inside the MapEffect.
                MapEffect(drawNewPin) { mapView ->
                    if(firstLaunch) {
                        viewModel.enableUserPuck(mapView)
                        firstLaunch = false
                    }
                    futLocations.forEach {
                        viewModel.drawAnnotation(mapView, locationPin, it)
                    }

                    if(drawNewPin){
                        if(currentAnnotation != null){
                            viewModel.deleteAnnotation(currentAnnotation!!)
                        }
                        viewModel.drawAnnotation(
                            mapView,
                            locationPinBlue,
                            FutLocation(
                                latitude = lat.doubleValue,
                                longitude = long.doubleValue)).also{
                                    currentAnnotation = it
                        }
                        drawNewPin = false
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = -60.dp)
                    .padding(horizontal = 10.dp)) {
                Column(modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally){
                TextButton(
                onClick = {
                    lat.doubleValue = currentCoordinates.first
                    long.doubleValue = currentCoordinates.second
                    drawNewPin = true
                    bottomSheetScope.launch{
                        sheetState.expand()
                    }
                          },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF672976),
                    contentColor = Color.White
                ),
                    contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "add"
                )
                Text(
                    text = "Use Current Location",
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

                    TextButton(
                        onClick = {
                            lat.doubleValue = 0.0
                            long.doubleValue = 0.0
                            readOnly = false
                            drawNewPin = true
                            bottomSheetScope.launch{
                                sheetState.expand()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF672976)
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "add"
                        )
                        Text(
                            text = "Use Another Location",
                            fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                }
        }
        }
    }
}

@Preview
@Composable
fun EditLocationsPreview(){
    EditLocationsScreen(rememberNavController())
}