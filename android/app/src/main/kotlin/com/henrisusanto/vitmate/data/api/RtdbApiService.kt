package com.henrisusanto.vitmate.data.api

import com.henrisusanto.vitmate.data.model.SearchIndexResponse
import com.henrisusanto.vitmate.data.model.MetaResponse
import com.henrisusanto.vitmate.data.model.PostResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RtdbApiService {
    @GET("search_index.json")
    suspend fun getSearchIndex(): SearchIndexResponse

    @GET("meta.json")
    suspend fun getMeta(): MetaResponse

    @GET("posts/{id}.json")
    suspend fun getPost(@Path("id") id: String): PostResponse
}