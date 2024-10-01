package com.andyslab.futmaps

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.andyslab.futmaps.data.NavigationTools.locationObserver
import com.andyslab.futmaps.data.NavigationTools.replayProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routeLineApi
import com.andyslab.futmaps.data.NavigationTools.routeLineView
import com.andyslab.futmaps.data.NavigationTools.routeProgressObserver
import com.andyslab.futmaps.data.NavigationTools.routesObserver
import com.andyslab.futmaps.domain.entities.UserProfile
import com.andyslab.futmaps.domain.repository.firestoredb.FirestoreRepoImpl
import com.andyslab.futmaps.domain.repository.mapbox.MapboxNavigationRepoImpl
import com.andyslab.futmaps.utils.Resource
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel(private val application: Application, owner: LifecycleOwner): AndroidViewModel(application) {
    val mapboxRepo = MapboxNavigationRepoImpl(application, owner)
    val firestoreRepo = FirestoreRepoImpl()
    val userProfile = UserProfile(application)

    fun initMapboxNavigation(){
        mapboxRepo.initMapboxNavigation()
    }

    fun unRegisterObservers(){

        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
        MapboxNavigationApp.current()?.unregisterRoutesObserver(routesObserver)
        MapboxNavigationApp.current()?.unregisterRouteProgressObserver(routeProgressObserver)
        MapboxNavigationApp.current()?.unregisterRouteProgressObserver(replayProgressObserver)
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    fun deinitMapboxNavigation(){
        mapboxRepo.deinitMapboxNavigation()
    }

    fun saveUser(){
        CoroutineScope(Dispatchers.IO).launch{
            userProfile.saveUser(UserProfile.instance)
        }
    }

    fun loadUser(){
        runBlocking{
            userProfile.loadUser()
        }
    }


    //utility function to sync Firestore db with Algolia indexes.
    fun syncDatabases(){
            CoroutineScope(Dispatchers.IO).launch{
                firestoreRepo.syncDatabases().collect{ resource ->
                    when(resource){
                        is Resource.Error -> {
                            withContext(Dispatchers.Main){
                                Toast.makeText(application, "sync error:" + resource.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            withContext(Dispatchers.Main){
                                Toast.makeText(application, "Sync successful", Toast.LENGTH_SHORT).show()
                            }
                            UserProfile.instance.hasSyncedDatabase = true}
                    }
                }
            }
    }
}