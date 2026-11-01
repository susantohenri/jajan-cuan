package com.henrisusanto.vitmate.data.testutil

import com.henrisusanto.vitmate.data.remote.OkHttpRecipeRemoteDataSource
import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import com.henrisusanto.vitmate.data.remote.dto.PostPayload
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

object Fixtures {
    val json = OkHttpRecipeRemoteDataSource.defaultJson()

    fun metaJson(): String = read("meta.json")

    fun indexJson(): String = read("search_index.json")

    fun postJson(name: String): String = read("posts/$name.json")

    fun meta(): MetaDto = json.decodeFromString(MetaDto.serializer(), metaJson())

    fun index(): Map<String, SearchIndexEntryDto> = json.decodeFromString(
        MapSerializer(String.serializer(), SearchIndexEntryDto.serializer()),
        indexJson()
    )

    fun indexEntry(recipeId: String): SearchIndexEntryDto =
        checkNotNull(index()[recipeId]) { "Entry $recipeId tidak ada di fixture" }

    fun post(name: String): PostPayload {
        val raw = postJson(name)
        return PostPayload(
            dto = json.decodeFromString(PostDto.serializer(), raw),
            rawJson = raw
        )
    }

    private fun read(path: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$path")) {
            "Fixture fixtures/$path tidak ditemukan"
        }
        return stream.bufferedReader().use { it.readText() }
    }
}
