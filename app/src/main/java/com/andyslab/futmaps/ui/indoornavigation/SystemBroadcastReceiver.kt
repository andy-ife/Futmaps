package com.andyslab.futmaps.ui.indoornavigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

//listens for changes in the bluetooth adapter
@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent) -> Unit,
){
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(newValue = onSystemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)

        val broadcast = object: BroadcastReceiver(){
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                if (intent != null) {
                    currentOnSystemEvent(intent)
                }
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        onDispose{
            context.unregisterReceiver(broadcast)
        }
    }
}