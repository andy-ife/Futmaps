import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.QuickSearchItems
import com.andyslab.futmaps.domain.entities.TripData
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.domain.repository.firestoredb.FirestoreRepoImpl
import com.andyslab.futmaps.domain.repository.mapbox.MapboxMapsRepoImpl
import com.andyslab.futmaps.utils.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.maps.MapView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(val fusedLocationProviderClient: FusedLocationProviderClient) : ViewModel(){
    private val _uiState: MutableStateFlow<Resource<FutLocation>?> = MutableStateFlow(null)
    val uiState = _uiState.asStateFlow()

    private val _suggestedSearches = MutableStateFlow<Set<FutLocation>>(QuickSearchItems.items)
    val suggestedSearches = _suggestedSearches.asStateFlow()

    private val _isAdmin = MutableStateFlow(UserProfile.instance.isAdmin)
    val isAdmin = _isAdmin.asStateFlow()

    private val mapboxRepo = MapboxMapsRepoImpl()
    private val firestoreRepo = FirestoreRepoImpl()

    init{
        setLastUserLocation()
    }

    @SuppressLint("MissingPermission")
    fun setLastUserLocation() {
        // Retrieve the last known location
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    UserProfile.instance.lastKnownCoordinates = (Pair(it.latitude, it.longitude))
                }
            }
            .addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception

            }
    }

    fun enableUserPuck(mapView: MapView){
        mapboxRepo.enableUserPuck(mapView)
    }

    fun drawAnnotation(
        mapView: MapView,
        icon: Bitmap,
        location: FutLocation
    ){
        mapboxRepo.drawAnnotation(mapView, icon, location)
    }

    fun retrieveFutLocation(name: String, onSuccess: () -> Unit){
        viewModelScope.launch{
            firestoreRepo.retrieveFutLocation(name).collect{resource ->
                when(resource){
                    is Resource.Error ->
                        _uiState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading ->
                        _uiState.value = Resource.Loading()
                    is Resource.Success -> {
                        _uiState.value = Resource.Success(resource.data!!)
                        UserProfile.instance.tripData = TripData(destination = resource.data)
                        onSuccess()
                    }
                }
            }
        }
    }

    fun dismissDialog(){
        _uiState.value = null
    }


    sealed class SheetContentState{
        object QuickSearchItems: SheetContentState()
        object RecentSearchItems: SheetContentState()
    }

}
