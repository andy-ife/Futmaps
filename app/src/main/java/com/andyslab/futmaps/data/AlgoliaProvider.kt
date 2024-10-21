package com.andyslab.futmaps.data

import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.andyslab.futmaps.BuildConfig


object AlgoliaProvider {
    val writeClient by lazy{
        ClientSearch(
            ApplicationID(/*BuildConfig.ALGOLIA_APP_ID*/"S737QDIBK7"),
            APIKey(/*BuildConfig.ALGOLIA_WRITE_KEY*/"b2f78ac16a88265b436c30a56e2a7441"))
    }

    val searchClient by lazy{
        ClientSearch(
            ApplicationID(/*BuildConfig.ALGOLIA_APP_ID*/"S737QDIBK7"),
            APIKey(/*BuildConfig.ALGOLIA_SEARCH_KEY*/"53eaad024adbb6facc7071a8735172e8"))
    }
}