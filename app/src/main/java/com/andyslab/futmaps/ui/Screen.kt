package com.andyslab.futmaps.ui

sealed class Screen (val route: String,){
    data object BigLoadingScreen: Screen(route = "big_loading")
    data object RequestPermissionScreen: Screen(route = "request_permissions")
    data object HomeScreen: Screen(route = "home")
    data object SearchScreen: Screen(route = "search")
    data object LoginScreen: Screen(route = "login")
    data object EditLocationsScreen: Screen(route = "edit_locations")
    data object ShowRouteScreen: Screen(route = "shoe_route")
    data object BLEScanScreen: Screen(route = "ble_scan")
}