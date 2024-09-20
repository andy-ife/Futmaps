package com.andyslab.futmaps.domain.repositories.search

import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun onSearchQueryChange(text: String): Flow<Resource<Set<FutLocation>>>
    suspend fun onSearchButtonClick(text:String): Flow<Resource<Set<FutLocation>>>
}