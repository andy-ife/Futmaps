package com.andyslab.futmaps.ui.editlocations

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.domain.repository.firestoredb.FirestoreRepoImpl
import com.andyslab.futmaps.domain.repository.mapbox.MapboxMapsRepoImpl
import com.andyslab.futmaps.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class EditLocationsViewModel(
    private val fusedLocationProviderClient: FusedLocationProviderClient
): ViewModel() {
    private val _syncState: MutableStateFlow<Resource<Boolean>?> = MutableStateFlow(null)
    val syncState = _syncState.asStateFlow()

    private val _lastUserLocation = MutableStateFlow(UserProfile.instance.lastKnownCoordinates)
    val lastUserLocation = _lastUserLocation.asStateFlow()

    private val mapboxRepo = MapboxMapsRepoImpl()
    private val firestoreRepo = FirestoreRepoImpl()

    init{
        getLastUserLocation({
            UserProfile.instance.lastKnownCoordinates = it
        }){}
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

    fun drawAnnotation(
        mapView: MapView,
        icon: Bitmap,
        location: FutLocation
    ): PointAnnotation{
        mapboxRepo.drawAnnotation(mapView, icon, location).also{return it}
    }

    fun deleteAnnotation(annotation: PointAnnotation){
        mapboxRepo.deleteAnnotation(annotation)
    }

    fun enableUserPuck(mapView: MapView){
        mapboxRepo.enableUserPuck(mapView)
    }

    fun uploadNewLocation(context: Context, futLocation: FutLocation){
        CoroutineScope(Dispatchers.IO).launch {
            firestoreRepo.uploadFutLocation(futLocation).collect{ resource ->
                when(resource){
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> withContext(Dispatchers.Main){
                        UserProfile.instance.hasSyncedDatabase = false
                        firestoreRepo.syncDatabases().collect{resource ->
                            when(resource){
                                is Resource.Error -> Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show()
                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    Toast.makeText(context, "Location saved successfully", Toast.LENGTH_SHORT).show()
                                    UserProfile.instance.hasSyncedDatabase = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun generateObjectId(): String{
        return (1..10)
            .map { Random.nextInt(0, 10) }
            .joinToString("")

    }
}