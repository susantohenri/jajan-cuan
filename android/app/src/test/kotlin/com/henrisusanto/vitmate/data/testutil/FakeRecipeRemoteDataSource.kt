package com.henrisusanto.vitmate.data.testutil

import com.henrisusanto.vitmate.data.remote.RecipeRemoteDataSource
import com.henrisusanto.vitmate.data.remote.RemoteResult
import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostPayload
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto

class FakeRecipeRemoteDataSource : RecipeRemoteDataSource {
    var metaResult: RemoteResult<MetaDto> = RemoteResult.NoData
    var indexResult: RemoteResult<Map<String, SearchIndexEntryDto>> = RemoteResult.NoData
    val postResults = mutableMapOf<String, RemoteResult<PostPayload>>()

    var metaCalls = 0
    var indexCalls = 0
    var postCalls = 0

    override suspend fun fetchMeta(): RemoteResult<MetaDto> {
        metaCalls++
        return metaResult
    }

    override suspend fun fetchSearchIndex(): RemoteResult<Map<String, SearchIndexEntryDto>> {
        indexCalls++
        return indexResult
    }

    override suspend fun fetchPost(recipeId: String): RemoteResult<PostPayload> {
        postCalls++
        return postResults[recipeId] ?: RemoteResult.NoData
    }
}
