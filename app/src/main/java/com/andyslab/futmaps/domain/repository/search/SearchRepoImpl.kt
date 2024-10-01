package com.andyslab.futmaps.domain.repository.search

import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.query
import com.algolia.search.helper.deserialize
import com.algolia.search.model.IndexName
import com.andyslab.futmaps.data.AlgoliaProvider
import com.andyslab.futmaps.domain.entities.FutLocation
import com.andyslab.futmaps.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepoImpl: SearchRepository {
    private val client = AlgoliaProvider.searchClient

    override suspend fun onSearchQueryChange(text: String): Flow<Resource<Set<FutLocation>>> {
        var result: Set<FutLocation>
        return flow{
            emit(Resource.Loading())
            delay(500)
            try{
                val query = query{
                    query = text
                    hitsPerPage = 10
                    attributesToRetrieve{
                        +"name"
                        +"tag"
                    }
                }
                val index = client.initIndex(IndexName("fut_locations"))
                val response = index.search(query)

                response.hits.deserialize<FutLocation>(FutLocation.serializer()).also{
                    result = it.toSet()
                }
                emit(Resource.Success(result))
            }
            catch(e: Exception){
                //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                emit(Resource.Error(e.message.toString()))
            }
        }
    }


    //very similar to what onSearchQueryChange() does, but this returns 20 hits instead of 10 and
    //with a reduced delay since no incoming keystroke
    override suspend fun onSearchButtonClick(text: String): Flow<Resource<Set<FutLocation>>> {
        var result: Set<FutLocation>
        return flow{
            emit(Resource.Loading())
            //delay(500)
            try{
                val query = query{
                    query = text
                    hitsPerPage = 20
                    attributesToRetrieve{
                        +"name"
                        +"tag"
                    }
                }
                val index = client.initIndex(IndexName("fut_locations"))
                val response = index.search(query)

                response.hits.deserialize<FutLocation>(FutLocation.serializer()).also{
                    result = it.toSet()
                }
                emit(Resource.Success(result))
            }
            catch(e: Exception){
                //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                emit(Resource.Error(e.message.toString()))
            }

        }
    }
}