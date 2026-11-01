package com.henrisusanto.vitmate.data.remote

import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostPayload
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto

/**
 * Akses data publik RTDB lewat REST. Hanya tiga endpoint yang diizinkan:
 * /meta.json, /search_index.json, dan /posts/{id}.json.
 */
interface RecipeRemoteDataSource {
    suspend fun fetchMeta(): RemoteResult<MetaDto>

    suspend fun fetchSearchIndex(): RemoteResult<Map<String, SearchIndexEntryDto>>

    suspend fun fetchPost(recipeId: String): RemoteResult<PostPayload>
}
