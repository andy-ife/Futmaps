package com.andyslab.futmaps.ui.bigloading

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.data.FirestoreProvider
import com.andyslab.futmaps.ui.ErrorDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.utils.Resource

@Composable
fun BigLoadingScreen(navController: NavHostController){
    val context = LocalContext.current
    val viewModel = viewModel<BigLoadingViewModel>()
    var showErrorDialog by remember{
        mutableStateOf("")
    }

    LaunchedEffect(true) {
        viewModel.retrieveAllFutLocations()//start emitting values
        viewModel.bigLoadingState.collect{resource ->
            when(resource){
                is Resource.Error -> {
                    showErrorDialog = resource.message.toString()
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    FirestoreProvider.futLocations = resource.data!!
                    navController.navigate(Screen.HomeScreen.route){
                        popUpTo(Screen.BigLoadingScreen.route){
                            inclusive = true
                        }
                    }
                }
                null -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color(0xFF965ca4)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Setting up maps and navigation...",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.sourcesans3_regular))
            )
        }

        if(showErrorDialog.isNotBlank()){
            ErrorDialog(
                message = showErrorDialog,
                onDismiss = {
                    showErrorDialog = ""
                    viewModel.retrieveAllFutLocations()}) {
                showErrorDialog = ""
                viewModel.retrieveAllFutLocations()
            }
        }
    }
}