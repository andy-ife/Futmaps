package com.andyslab.futmaps.data

import com.mapbox.common.MapboxOptions

object MapboxProvider {
    fun setAccessToken(token: String){
        MapboxOptions.accessToken = token
    }
}