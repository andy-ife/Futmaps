package com.andyslab.futmaps.ui.indoornavigation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTING
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andyslab.futmaps.R
import com.andyslab.futmaps.ui.PermissionDialog
import com.andyslab.futmaps.ui.Screen
import com.andyslab.futmaps.ui.requestlocationpermission.openAppSettings
import com.andyslab.futmaps.utils.findActivity

val blePermissionsList =
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }else{
        null //no permissions required for android 11 and below
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEScanScreen(
    navController: NavHostController
){
    val context = LocalContext.current
    val activity = context.findActivity()

    var bluetoothHasBeenEnabled by remember{
        mutableStateOf(false)
    }
    var bluetoothDialogIsAlreadyShown by remember {
        mutableStateOf(false)
    }

    val viewModel = viewModel<BLEScanViewModel>(factory = BLEScanViewModel.Factory)
    val permissionDialog by viewModel.visiblePermissionDialog.collectAsState()
    val bleScanState by viewModel.bleScanState.collectAsState()
    val currentProximity by viewModel.currentProximity.collectAsState()

    val requestEnableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()) { result ->
        bluetoothDialogIsAlreadyShown = false
        bluetoothHasBeenEnabled = (result.resultCode == Activity.RESULT_OK || viewModel.bluetoothIsEnabled())
        if(bluetoothHasBeenEnabled)
            viewModel.initializeConnection()
    }

    val requestBluetoothPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()){ permissionMap ->
        permissionMap.entries.forEach{
            viewModel.onPermissionResult(it.key, it.value)
            if(!it.value) return@rememberLauncherForActivityResult

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetoothLauncher.launch(enableBtIntent)
            bluetoothDialogIsAlreadyShown = true
        }
    }

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) {bluetoothState ->
        val action = bluetoothState.action ?: return@SystemBroadcastReceiver

        if(action == BluetoothAdapter.ACTION_STATE_CHANGED && bluetoothHasBeenEnabled){
            if(!viewModel.bluetoothIsEnabled()){
                if(!bluetoothDialogIsAlreadyShown){
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    requestEnableBluetoothLauncher.launch(enableBtIntent)
                    bluetoothDialogIsAlreadyShown = true
                }
            }
        }

        val extra = bluetoothState?.extras?.getInt(BluetoothAdapter.EXTRA_STATE) ?: return@SystemBroadcastReceiver
        if(extra == STATE_OFF || extra == STATE_TURNING_OFF){
            viewModel.disconnect()
        }
    }

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) { intent ->
        val extra = intent?.extras?.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE) ?: return@SystemBroadcastReceiver
        if(extra == STATE_DISCONNECTING || extra == STATE_DISCONNECTED){
            viewModel.disconnect()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Indoor Navigation",
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        color = Color.White
                    )},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.HomeScreen.route){
                                popUpTo(Screen.BLEScanScreen.route){
                                    inclusive = true
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF52299E)
                ))
        },
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 60.dp),
                contentAlignment = Alignment.Center){
                TextButton(
                    onClick = {
                        if(bleScanState == null || bleScanState is BleScanState.Disconnected ||
                            bleScanState is BleScanState.Connected){
                        if (blePermissionsList != null && !context.hasBluetoothPermissions()) {
                            requestBluetoothPermissionsLauncher.launch(blePermissionsList)
                        } else{
                            if(!viewModel.bluetoothIsEnabled()){
                                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                requestEnableBluetoothLauncher.launch(enableBtIntent)
                                bluetoothDialogIsAlreadyShown = true
                            }else{
                                viewModel.initializeConnection()
                            }
                        }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(0xFF31185f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if(bleScanState !is BleScanState.Connected){
                    Text(
                        text = "Find beacons",
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp)
                    }else{
                        Text(
                            text = "Scan again",
                            fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) {scaffoldPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF52299E), Color(0xFF31185f)
                    )
                )
            ),
            contentAlignment = Alignment.Center){

            if(bleScanState == null){ // initial state
                ElevatedCard(modifier = Modifier
                    .size(100.dp)
                    .offset(y = -60.dp),
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF31185f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(2.dp)) {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                    Icon(painter = painterResource(id = R.drawable.round_bluetooth_24),
                        contentDescription = "bluetooth",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF31185f))
                    }
                }
            }

            if(bleScanState is BleScanState.Connecting){ // loading state
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color.White,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = (bleScanState as BleScanState.Connecting).message,
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            if(bleScanState is BleScanState.Connected){ // success state
                BleConnectedScreen(device = (bleScanState as BleScanState.Connected).data)
            }

            if(bleScanState is BleScanState.Disconnecting){ // loading state
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = Color.White,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = (bleScanState as BleScanState.Disconnecting).message,
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            if(bleScanState is BleScanState.Disconnected){ //error state
                Text(
                    text = (bleScanState as BleScanState.Disconnected).message,
                    fontSize = 17.sp,
                    fontFamily = FontFamily(Font(R.font.sourcesans3_regular)),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }


            if(permissionDialog.isNotBlank()){
                PermissionDialog(
                    permission = "Bluetooth",
                    description = {
                        Text(text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 16.sp)){
                                append("Indoor navigation can't work without bluetooth access. Go to app " )
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)){ append("Settings")}
                                append(" and grant the ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)){ append("Nearby devices")}
                                append(" permission to continue.")
                            }
                        })
                    },
                    onDismiss = {viewModel.onDismissPermissionDialog()},
                    onGoToAppSettingsClick = {activity.openAppSettings()})
            }
        }
    }
}


@Preview
@Composable
fun BLEScanScreenPreview(){
    BLEScanScreen(navController = rememberNavController())
}

fun Context.hasBluetoothPermissions(): Boolean{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        val bluetoothPermission = this.checkSelfPermission(
            Manifest.permission.BLUETOOTH
        )

        val scanPermission = this.checkSelfPermission(
            Manifest.permission.BLUETOOTH_SCAN
        )

        val connectPermission = this.checkSelfPermission(
            Manifest.permission.BLUETOOTH_CONNECT
        )

        return bluetoothPermission == PackageManager.PERMISSION_GRANTED
            && scanPermission == PackageManager.PERMISSION_GRANTED
            && connectPermission == PackageManager.PERMISSION_GRANTED
    }
    else{
        return true
    }
}

