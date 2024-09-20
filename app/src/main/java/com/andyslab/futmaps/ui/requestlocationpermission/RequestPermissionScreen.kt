package com.andyslab.futmaps.ui.requestlocationpermission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.ui.PermissionDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.utils.findActivity

@Composable
fun RequestPermissionScreen(navController: NavHostController,){
    val context = LocalContext.current
    val activity = context.findActivity()
    val viewModel = viewModel<RequestPermissionViewModel>()
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val dialog by viewModel.visiblePermissionDialog.collectAsState()
    
    val locationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(
                permission = permission,
                isGranted = isGranted
            )
            if(isGranted){
                navController.navigate(Screen.BigLoadingScreen.route) {
                    popUpTo(Screen.RequestPermissionScreen.route) {
                        inclusive = true
                    }
                }
            }
        })

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
        ){

        Spacer(modifier = Modifier.height(100.dp))

        Box(modifier = Modifier
            .size(50.dp)
            .background(Color(0xFF303f9f)))

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Give Futmaps access to your precise location",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 17.sp)){
                    append("FUTMaps needs precise location access to " +
                        "give you accurate navigation instructions " +
                        "and other useful features.\n\nTap ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                        append("Continue ")
                    }
                    append("and then\n\nSelect ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                        append("Precise")
                    }
                }
            }
        )

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.Bottom){
        TextButton(
            onClick = {
                locationPermissionResultLauncher.launch(
                    permission
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF672976)
            ),
            elevation = ButtonDefaults.buttonElevation(2.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Continue",
                color = Color.White,
                fontSize = 14.sp,)
        }

        }
    }

    if(dialog.isNotBlank()){
        PermissionDialog(
            permission = "Location",
            description = {
                Text(text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 16.sp)){
                        append("Futmaps can't work without knowing your precise location. Go to app " )
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)){ append("Settings")}
                        append(" to grant precise location access.")
                    }
                })
            },
            onDismiss = {viewModel.onDismissDialog()},
            onGoToAppSettingsClick = {activity.openAppSettings()})
    }
}

@Preview
@Composable
fun RequestPermissionScreenPrev(){
    RequestPermissionScreen(rememberNavController())
}

//utility function that opens App settings
fun Activity.openAppSettings(){
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}




