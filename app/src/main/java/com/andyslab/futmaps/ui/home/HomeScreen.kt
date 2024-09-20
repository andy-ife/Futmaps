package com.andyslab.futmaps.ui.home

import HomeViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.data.FirestoreProvider.futLocations
import com.andyslab.futmaps.ui.ErrorDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.utils.ExpandedType
import com.andyslab.futmaps.utils.Resource
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.MapLoadingErrorType
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.observable.toMapLoadedEventData
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import io.morfly.compose.bottomsheet.material3.sheetVisibleHeightDp
import kotlinx.coroutines.delay

var key = true
@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun HomeScreen(navController: NavHostController){
    val context = LocalContext.current
    //val activity = context.findActivity()
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val viewModel = HomeViewModel(fusedLocationProviderClient)

    val mapState = rememberMapState()

    val isAdmin by viewModel.isAdmin.collectAsState()

    val mapViewPortState = rememberMapViewportState {
        setCameraOptions {
            zoom(10.0)
            center(Point.fromLngLat(9.5836, 6.5463))
            pitch(0.0)
            bearing(0.0)
        }
    }

    val sheetState = rememberBottomSheetState(
        initialValue = ExpandedType.COLLAPSED,
        defineValues = {
            ExpandedType.COLLAPSED at height(266.dp)
            ExpandedType.HALF at offset(percent = 50)
            ExpandedType.FULL at contentHeight
        }
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        sheetState = sheetState
    )

    val searchQueryState = remember{
        mutableStateOf("")
    }

    val quickSearchItems by viewModel.suggestedSearches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val locationPin = getBitmap(context, R.drawable.annotation_pin)

    //this is morfly's bottom sheet implementation. Not material3
    BottomSheetScaffold(
        sheetContent = {
            SearchBottomSheet(
                searchQueryState = searchQueryState,
                suggestedSearches = quickSearchItems,
                goToSearchScreen = {
                    navController.navigate(Screen.SearchScreen.route) },
                goToSearchResultScreen = {name ->
                    viewModel.retrieveFutLocation(name){
                        navController.navigate(Screen.ShowRouteScreen.route)
                    }
                    //launched = true
                }
            )},
        modifier = Modifier
            .fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetContainerColor = Color.White,
        sheetTonalElevation = 16.dp,
        sheetShadowElevation = 8.dp,
    ){
        Box(modifier = Modifier
            .fillMaxSize()){
            MapboxMap(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-80).dp),
                compass = {
                  Compass(
                      alignment = Alignment.CenterEnd,
                      modifier = Modifier
                          .size(48.dp)
                          .offset(
                              x = -4.dp,
                              y = -(sheetState.sheetVisibleHeightDp - 240.dp)
                          ),
                  )
                },
                mapViewportState = mapViewPortState,
                mapState = mapState,
                style = { MapStyle(style = Style.MAPBOX_STREETS) }
                ){
                //LaunchedEffect type block that exposes raw MapView object.
                //All APIs can be used inside the MapEffect.
                MapEffect(Unit) { mapView ->
                    viewModel.enableUserPuck(mapView)
                    futLocations.forEach {
                        viewModel.drawAnnotation(mapView, locationPin, it)
                    }
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 60.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.End) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {

                    Column(modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                    , horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)){
                    HomeScreenMapButton(
                        modifier = Modifier.border(2.dp, Color.White, CircleShape),
                        iconResId = R.drawable.profile_icon,
                        containerColor = if(isAdmin)Color(0xffc5cae9) else Color.White,
                        contentColor = if(isAdmin)Color(0xFF3c0949) else Color(0xFF404040),
                        iconSize = 48.dp)
                    {
                        navController.navigate(Screen.LoginScreen.route)
                    }

                    if(isAdmin){
                    Box(modifier = (Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp))){

                        Row(modifier = Modifier.wrapContentSize(),
                            verticalAlignment= Alignment.CenterVertically){
                        Text(
                            text = "Admin",
                            color = Color(0xFF672976),
                            fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp)

                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xff388e3c),
                            )
                        }

                    }}
                    }

                    HomeScreenMapButton(iconResId = R.drawable.terrain_icon){}
                }

                Spacer(modifier = Modifier.height(340.dp))

                
                val ofset = if(isAdmin)200.dp else 290.dp
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = -(sheetState.sheetVisibleHeightDp - ofset)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom){
                    
                    TextButton(onClick = {
                            navController.navigate(Screen.BLEScanScreen.route)
                                         },
                        shape = RoundedCornerShape(10.dp,),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF672976)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            2.dp
                        )
                        ) {
                        Text(text = "Switch to indoor navigation",
                            fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF672976))
                        
                            Icon(
                                painter = painterResource(id = R.drawable.round_bluetooth_24),
                                contentDescription = "bluetooth",
                                modifier = Modifier.size(20.dp))
                    }

                    Column(modifier = Modifier.wrapContentSize(),
                        ){
                HomeScreenMapButton(
                    modifier = Modifier,
                    iconResId = R.drawable.target_icon,
                    onClick = {
                        mapViewPortState.transitionToFollowPuckState()
                    })

                if(isAdmin){
                    Spacer(modifier = Modifier.height(20.dp))

                    HomeScreenMapButton(
                        modifier = Modifier,
                        iconResId = R.drawable.round_add_24,
                        containerColor = Color(0xFF672976),
                        contentColor = Color.White,
                    ){
                        navController.navigate(Screen.EditLocationsScreen.route)
                    }
                }}
                }
            }

            if(uiState is Resource.Loading){
                ElevatedCard(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xff965ca4)
                    )) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                        contentAlignment = Alignment.Center){
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xff965ca4),
                        strokeWidth = 2.5.dp)
                    }
                }
            }

            if(uiState is Resource.Error){
                ErrorDialog(
                    message = "Location not found. It might not have been uploaded yet.",
                    mainAction = "OK",
                    onDismiss = {
                        viewModel.dismissDialog()
                    }) {
                    viewModel.dismissDialog()
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPrev(){
    HomeScreen(rememberNavController())
}

@Composable
fun HomeScreenMapButton(
    modifier: Modifier = Modifier,
    iconResId: Int,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF672976),
    iconSize: Dp = 24.dp,
    onClick: () -> Unit = {}){

    val interactionSource = remember{
        MutableInteractionSource()
    }

    val rippleColor = if(containerColor == Color.White) Color(0xFF672976) else Color.White
    IconButton(
        onClick = { onClick() },
        modifier = modifier
            .indication(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    color = rippleColor,
                    radius = 24.dp,
                )
            )
            .size(48.dp)
            .shadow(2.dp, CircleShape),
        interactionSource = interactionSource,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = "profile icon",
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getBitmap(context: Context, drawableRes: Int): Bitmap {
    val drawable: Drawable = context.getDrawable(drawableRes)!!
    val canvas: Canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)

    return bitmap
}

