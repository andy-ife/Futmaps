package com.andyslab.futmaps

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.compose.runtime.collectAsState
import com.andyslab.futmaps.data.MapboxProvider
import com.andyslab.futmaps.data.NavigationTools.hasObservers
import com.andyslab.futmaps.ui.navgraph.RootNavGraph
import com.andyslab.futmaps.ui.theme.FUTMapsTheme
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.observeOn

@ColorInt
val statusBarColor: Int = Color.parseColor("#80404040").toInt()
val navigationBarColor: Int = Color.parseColor("#fff0f0f0").toInt()


class MainActivity : ComponentActivity() {
    val viewModel by lazy{
        MainViewModel(application, this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadUser()
        hasObservers = false
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = statusBarColor
            ),

            navigationBarStyle = SystemBarStyle.light(
                scrim = navigationBarColor,
                darkScrim = navigationBarColor
            )
        )
        MapboxProvider.setAccessToken(resources.getString(R.string.mapbox_access_token))
        viewModel.syncDatabases()
        viewModel.initMapboxNavigation()

        setContent {
            FUTMapsTheme {
                RootNavGraph()
            }
        }
    }

    override fun onPause(){
        super.onPause()
        viewModel.saveUser()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveUser()
        //MapboxNavigationApp.current()?.stopTripSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(hasObservers){
            viewModel.unRegisterObservers()
        }
        viewModel.deinitMapboxNavigation()
        viewModel.saveUser()
    }

}


