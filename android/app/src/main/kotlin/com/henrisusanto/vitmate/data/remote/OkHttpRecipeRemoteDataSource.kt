package com.henrisusanto.vitmate.data.remote

import com.henrisusanto.vitmate.BuildConfig
import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import com.henrisusanto.vitmate.data.remote.dto.PostPayload
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * REST client RTDB memakai OkHttp + kotlinx.serialization.
 *
 * OkHttp menambahkan Accept-Encoding: gzip dan men-decompress respons secara
 * transparan. Retry hanya berlaku untuk GET dan tidak mengulang 4xx.
 */
class OkHttpRecipeRemoteDataSource(
    baseUrl: String = BuildConfig.RTDB_BASE_URL,
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = defaultJson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val retryDelayMillis: suspend (Long) -> Unit = { delay(it) }
) : RecipeRemoteDataSource {

    private val baseHttpUrl: HttpUrl = baseUrl.toHttpUrl()

    override suspend fun fetchMeta(): RemoteResult<MetaDto> =
        get("meta.json") { body -> json.decodeFromString(MetaDto.serializer(), body) }

    override suspend fun fetchSearchIndex(): RemoteResult<Map<String, SearchIndexEntryDto>> =
        get("search_index.json") { body ->
            json.decodeFromString(
                kotlinx.serialization.builtins.MapSerializer(
                    String.serializer(),
                    SearchIndexEntryDto.serializer()
                ),
                body
            )
        }

    override suspend fun fetchPost(recipeId: String): RemoteResult<PostPayload> =
        get("posts", "$recipeId.json") { body ->
            PostPayload(
                dto = json.decodeFromString(PostDto.serializer(), body),
                rawJson = body
            )
        }

    private suspend fun <T> get(
        vararg pathSegments: String,
        parse: (String) -> T
    ): RemoteResult<T> = withContext(ioDispatcher) {
        val url = baseHttpUrl.newBuilder().apply {
            pathSegments.forEach { addPathSegment(it) }
        }.build()
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
            .build()

        var attempt = 0
        while (true) {
            try {
                client.newCall(request).execute().use { response ->
                    when {
                        response.code == HTTP_NOT_FOUND -> return@withContext RemoteResult.NoData
                        !response.isSuccessful -> {
                            if (response.code < HTTP_SERVER_ERROR || attempt >= maxRetries) {
                                return@withContext RemoteResult.Failure(
                                    kind = FailureKind.HTTP,
                                    message = "HTTP ${response.code} untuk ${request.url}"
                                )
                            }
                        }
                        else -> {
                            val body = response.body?.string().orEmpty()
                            return@withContext if (body.isBlank() || body.trim() == JSON_NULL) {
                                RemoteResult.NoData
                            } else {
                                try {
                                    RemoteResult.Success(parse(body))
                                } catch (exception: Exception) {
                                    RemoteResult.Failure(
                                        kind = FailureKind.PARSE,
                                        message = "Respons tidak sesuai skema untuk ${request.url}",
                                        cause = exception
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (exception: IOException) {
                if (attempt >= maxRetries) {
                    return@withContext RemoteResult.Failure(
                        kind = FailureKind.NETWORK,
                        message = "Gagal menghubungi ${request.url}",
                        cause = exception
                    )
                }
            }
            // F-022: delay coroutine, bukan Thread.sleep, agar thread IO tidak tertahan.
            attempt++
            retryDelayMillis(RETRY_BASE_DELAY_MILLIS * attempt)
        }
        @Suppress("UNREACHABLE_CODE")
        RemoteResult.Failure(
            kind = FailureKind.NETWORK,
            message = "Gagal menghubungi ${request.url}"
        )
    }

    companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
        private const val JSON_NULL = "null"
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 20L
        private const val WRITE_TIMEOUT_SECONDS = 15L
        private const val DEFAULT_MAX_RETRIES = 2
        private const val RETRY_BASE_DELAY_MILLIS = 250L

        fun defaultJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
}
