package com.andyslab.futmaps.domain.repository.firestoredb

import com.algolia.search.model.IndexName
import com.algolia.search.transport.RequestOptions
import com.andyslab.futmaps.data.AlgoliaProvider
import com.andyslab.futmaps.data.FirestoreProvider
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FirestoreRepoImpl: FirestoreRepo {
    private val futLocationsCollectionRef = FirestoreProvider.firestore.collection("fut_locations")
    private val syncStatusCollectionRef = FirestoreProvider.firestore.collection("sync_status")
    private val index = AlgoliaProvider.writeClient.initIndex(IndexName("fut_locations"))

    //sync firestore db with algolia indices
    override suspend fun syncDatabases(): Flow<Resource<Boolean>> {
        return flow{
            var errorMsg = ""
            emit(Resource.Loading())
            try {
                //first check if db has new changes
                val syncStatus = syncStatusCollectionRef.get().await()
                val docId = syncStatus?.documents!![0].id
                val hasNewChanges = syncStatus.documents[0].getBoolean("hasNewChanges")!!

                if(hasNewChanges){
                    val locations = mutableListOf<FutLocation>()

                    val querySnapshot = futLocationsCollectionRef
                        .get()
                        .addOnFailureListener {
                            errorMsg = it.message.toString()
                        }
                        .await()

                    for (document in querySnapshot?.documents!!) {
                        val futLocation = document.toObject(FutLocation::class.java)
                        if (futLocation != null)
                            locations.add(futLocation)
                        else {
                            errorMsg = "Missing or corrupt firestore document"
                            //emit(Resource.Error("Missing or corrupt firestore document"))
                        }
                    }

                    val json = locations.map {
                        buildJsonObject {
                            put("objectID", it.objectID)
                            put("name", it.name)
                            put("tag", it.tag)
                            put("icon", it.icon.toString())
                        }
                    }

                    val reqOptions = RequestOptions().also{it.writeTimeout = 60}

                    index.saveObjects(json, reqOptions)

                    if(errorMsg.isBlank()){
                        val doc = syncStatusCollectionRef.document(docId)
                        doc.update("hasNewChanges",false)
                        emit(Resource.Success(true))
                    }
                    else{
                        emit(Resource.Error(errorMsg))}
                }

            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun retrieveFutLocation(name: String): Flow<Resource<FutLocation>> {
        var document: FutLocation
        return flow {
            var errorMsg = ""
            emit(Resource.Loading())
            try{
                val querySnapshot = futLocationsCollectionRef
                    .whereEqualTo("name", name)
                    .get()
                    .addOnFailureListener {
                        errorMsg = it.message.toString()
                    }
                    .await()

                querySnapshot.documents[0].toObject(FutLocation::class.java).also{
                    if(it == null){
                        errorMsg = "document not found"
                        //emit(Resource.Error("document not found"))
                    }else{
                        if(errorMsg.isBlank()){
                            document = it
                            emit(Resource.Success(document))
                        }else{
                            emit(Resource.Error(errorMsg))
                        }
                    }
                }
            }
            catch(e: Exception){
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun retrieveAllFutLocations(): Flow<Resource<Set<FutLocation>>> {
        var locations = mutableSetOf<FutLocation>()
        var isError = ""
        return flow{
            emit(Resource.Loading())
            try{
                val querySnapshot = futLocationsCollectionRef.get().addOnFailureListener {
                    isError = "No internet connection. Starting in offline mode."
                }.await()

                for (document in querySnapshot?.documents!!) {
                    val futLocation = document.toObject(FutLocation::class.java)
                    if (futLocation != null){
                        locations.add(futLocation)
                    }
                    else {
                        isError = "Missing or corrupt firestore document"
                        //emit(Resource.Error("Missing or corrupt firestore document"))
                        currentCoroutineContext().cancel()
                    }
                }
                if(isError.isBlank())
                    emit(Resource.Success(locations.toSet()))
                else
                    emit(Resource.Error(isError))
            }catch (e: Exception){
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun uploadFutLocation(futLocation: FutLocation): Flow<Resource<Boolean>> {
        var isError = false
        return flow{
            emit(Resource.Loading())
            try{
                futLocationsCollectionRef.add(futLocation).addOnSuccessListener {
                    isError = false
                }.addOnFailureListener {
                    isError = true
                }
                if(!isError){
                    val syncStatus = syncStatusCollectionRef.get().await()
                    val docId = syncStatus?.documents!![0].id
                    syncStatusCollectionRef.document(docId).update("hasNewChanges", true)
                    emit(Resource.Success())
                }
                else{
                    emit(Resource.Error("Failed to add new location"))
                }
            }
            catch(e: Exception){
                emit(Resource.Error(e.message.toString()))
            }
        }
    }


}
