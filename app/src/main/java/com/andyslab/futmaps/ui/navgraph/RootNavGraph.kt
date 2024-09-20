package com.andyslab.futmaps.ui.navgraph

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.ui.bigloading.BigLoadingScreen
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.ui.editlocations.EditLocationsScreen
import com.andyslab.futmaps.ui.home.HomeScreen
import com.andyslab.futmaps.ui.indoornavigation.BLEScanScreen
import com.andyslab.futmaps.ui.login.LoginScreen
import com.andyslab.futmaps.ui.requestlocationpermission.RequestPermissionScreen
import com.andyslab.futmaps.ui.search.SearchScreen
import com.andyslab.futmaps.ui.showroute.ShowRouteScreen

@Composable
fun RootNavGraph(){
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current

    val firstDestination = if(hasLocationPermissions(context)) Screen.BigLoadingScreen.route else Screen.RequestPermissionScreen.route

    NavHost(
        navController = navController,
        route = "root_graph",
        startDestination = firstDestination){

        composable(route = Screen.BigLoadingScreen.route){
            BigLoadingScreen(navController)
        }

        composable(route = Screen.RequestPermissionScreen.route){
            RequestPermissionScreen(navController)
        }

        composable(
            route = Screen.HomeScreen.route,
            enterTransition = {
                EnterTransition.None
            },
            popEnterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            popExitTransition = {
                ExitTransition.None
            }){
            HomeScreen(navController)
        }

        composable(
            route = Screen.SearchScreen.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
            )},

            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(500)
                )
            },
            ){

            SearchScreen(navController)
        }

        composable(route = Screen.LoginScreen.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )},
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },){
            LoginScreen(navController)
        }

        composable(route = Screen.EditLocationsScreen.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )},

            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }){
            EditLocationsScreen(navController)
        }

        composable(route = Screen.ShowRouteScreen.route){
            ShowRouteScreen(navController)
        }

        composable(route = Screen.BLEScanScreen.route){
            BLEScanScreen(navController)
        }
    }
}

fun hasLocationPermissions(context: Context): Boolean{
    val fineLocationPermission = context.checkSelfPermission(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val coarseLocationPermission = context.checkSelfPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return fineLocationPermission == PackageManager.PERMISSION_GRANTED
            && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
}

