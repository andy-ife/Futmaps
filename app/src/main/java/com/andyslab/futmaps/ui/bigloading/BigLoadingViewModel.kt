package com.andyslab.futmaps.ui.bigloading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.domain.repositories.firestoredb.FirestoreRepoImpl
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BigLoadingViewModel: ViewModel() {
    private val _bigLoadingState = MutableStateFlow<Resource<Set<FutLocation>>?>(null)
    val bigLoadingState = _bigLoadingState.asStateFlow()

    private val firestoreRepo = FirestoreRepoImpl()

    fun retrieveAllFutLocations(){
        viewModelScope.launch{
            firestoreRepo.retrieveAllFutLocations().collect{resource ->
                when(resource){
                    is Resource.Error -> _bigLoadingState.value = Resource.Error("retrieval error:"+resource.message.toString())
                    is Resource.Loading -> _bigLoadingState.value = (Resource.Loading())
                    is Resource.Success -> _bigLoadingState.value = Resource.Success(resource.data)
                }
            }
        }
    }
}