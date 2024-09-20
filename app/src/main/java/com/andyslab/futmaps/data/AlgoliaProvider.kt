package com.andyslab.futmaps.data

import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.andyslab.futmaps.BuildConfig


object AlgoliaProvider {
    val writeClient by lazy{
        ClientSearch(
            ApplicationID(BuildConfig.ALGOLIA_APP_ID),
            APIKey(BuildConfig.ALGOLIA_WRITE_KEY))
    }

    val searchClient by lazy{
        ClientSearch(
            ApplicationID(BuildConfig.ALGOLIA_APP_ID),
            APIKey(BuildConfig.ALGOLIA_SEARCH_KEY))
    }
}