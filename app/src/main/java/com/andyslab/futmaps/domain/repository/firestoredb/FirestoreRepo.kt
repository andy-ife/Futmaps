package com.andyslab.futmaps.domain.repository.firestoredb

import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FirestoreRepo{
    suspend fun syncDatabases(): Flow<Resource<Boolean>>
    suspend fun retrieveFutLocation(name: String): Flow<Resource<FutLocation>>
    suspend fun retrieveAllFutLocations():Flow<Resource<Set<FutLocation>>>
    suspend fun uploadFutLocation(futLocation: FutLocation):Flow<Resource<Boolean>>
}