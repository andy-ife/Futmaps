package com.andyslab.futmaps.domain.entities

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

const val USER_PROFILE_NAME = "user_profile"
const val USER_PROFILE_KEY = "user_profile_key"
const val TAG = "Login Activity"

class UserProfile(private val context: Context){

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PROFILE_NAME)
        lateinit var instance: User
    }

    init{
        runBlocking { instance = loadUser() }//initializes instance. if datastore is empty, assign default User object.
    }

    suspend fun loadUser(): User {
        val dataStoreKey = stringPreferencesKey(USER_PROFILE_KEY)
        val prefs = context.dataStore.data.first()

        val json = prefs[dataStoreKey]
        val gson = Gson()

        return (gson.fromJson(json, User::class.java) ?: User()).also{
            instance = it
        }
    }

    suspend fun saveUser(player: User) {
        val dataStoreKey = stringPreferencesKey(USER_PROFILE_KEY)
        //convert player data to string so it can be stored with a string preferences key
        val gson = Gson()
        val json = gson.toJson(player)

        context.dataStore.edit {
            it[dataStoreKey] = json
        }
    }

}


data class User(
    var isAdmin: Boolean = false,
    var hasSyncedDatabase: Boolean = false,
    var lastKnownCoordinates: Pair<Double, Double> = Pair(8.978468, 7.450726),//city view coordinates
    var tripData: TripData = TripData()
)