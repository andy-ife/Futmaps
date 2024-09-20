package com.andyslab.futmaps.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

//****For Composables*****//
//utility function to get current Activity from a Composable
fun Context.findActivity(): ComponentActivity {
    var context = this
    while(context is ContextWrapper){
        if(context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}