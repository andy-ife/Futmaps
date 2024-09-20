package com.andyslab.futmaps.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.entities.TripData
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.domain.repositories.firestoredb.FirestoreRepoImpl
import com.andyslab.futmaps.domain.repositories.search.SearchRepoImpl
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private val _searchState: MutableStateFlow<Resource<Set<FutLocation>>?> = MutableStateFlow(null)
    val searchState = _searchState.asStateFlow()

    private val _suggestions = MutableStateFlow<Set<FutLocation>>(setOf())
    val suggestions = _suggestions.asStateFlow()

    private val searchRepo = SearchRepoImpl()
    private val firestoreRepo = FirestoreRepoImpl()

    private var job = Job()
        get(){
            if (field.isCancelled) field = Job()
            return field
        }

    //perform search on algolia index and retrieve values
    fun onSearchQueryChange(text: String, onSuccess: () -> Unit = {}){
        job.cancel()//Cancel old Job and its child Flows
        viewModelScope.launch(job){
            searchRepo.onSearchQueryChange(text).collect{ resource ->
                when(resource){
                    is Resource.Error -> _searchState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading -> _searchState.value = Resource.Loading()
                    is Resource.Success -> {
                        _searchState.value = Resource.Success(resource.data)
                        _suggestions.value = resource.data!!//it's a set, don't worry :)
                        delay(500)
                        onSuccess()
                    }
                }
            }
        }
    }
    //same as onSearchQueryChange, but returns more hits with no keystroke allowance delay
    fun onSearchButtonClick(text: String, onSuccess: () -> Unit){
        job.cancel()//cancel old job
        viewModelScope.launch {
            searchRepo.onSearchButtonClick(text).collect{ resource ->
                when(resource){
                    is Resource.Error -> _searchState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading -> _searchState.value = Resource.Loading()
                    is Resource.Success -> {
                        _searchState.value = Resource.Success(resource.data)
                        _suggestions.value = resource.data!!
                        onSuccess()
                    }
                }
            }
        }
    }

    fun retrieveFutLocation(name: String, onSuccess: () -> Unit){
        viewModelScope.launch{
            firestoreRepo.retrieveFutLocation(name).collect{resource ->
                when(resource){
                    is Resource.Error -> _searchState.value = Resource.Error(resource.message.toString())
                    is Resource.Loading -> _searchState.value = Resource.Loading()
                    is Resource.Success -> {
                        UserProfile.instance.tripData = TripData(destination = resource.data!!)
                        onSuccess()
                    }
                }
            }
        }
    }

    fun dismissDialog(){
        _searchState.value = null
    }
}
