package com.henrisusanto.vitmate.data.remote

import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import com.henrisusanto.vitmate.data.remote.dto.PostPayload
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * REST client RTDB memakai OkHttp + kotlinx.serialization.
 *
 * OkHttp menambahkan Accept-Encoding: gzip dan men-decompress respons secara
 * transparan. Retry hanya berlaku untuk GET dan tidak mengulang 404.
 */
class OkHttpRecipeRemoteDataSource(
    baseUrl: String = BASE_URL,
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = defaultJson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
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

        try {
            client.newCall(request).execute().use { response ->
                when {
                    response.code == HTTP_NOT_FOUND -> RemoteResult.NoData
                    !response.isSuccessful -> RemoteResult.Failure(
                        kind = FailureKind.HTTP,
                        message = "HTTP ${response.code} untuk ${request.url}"
                    )
                    else -> {
                        val body = response.body?.string().orEmpty()
                        if (body.isBlank() || body.trim() == JSON_NULL) {
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
            RemoteResult.Failure(
                kind = FailureKind.NETWORK,
                message = "Gagal menghubungi ${request.url}",
                cause = exception
            )
        }
    }

    companion object {
        const val BASE_URL =
            "https://jajan-cuan-default-rtdb.asia-southeast1.firebasedatabase.app/"

        private const val HTTP_NOT_FOUND = 404
        private const val JSON_NULL = "null"
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 20L
        private const val WRITE_TIMEOUT_SECONDS = 15L

        fun defaultJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .build()
    }
}

/**
 * Retry sederhana untuk GET: maksimal dua kali ulang dengan backoff pendek.
 * Kegagalan 4xx (termasuk 404) tidak diulang.
 */
internal class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val baseDelayMillis: Long = 250L,
    private val sleep: (Long) -> Unit = { millis -> Thread.sleep(millis) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method != "GET") return chain.proceed(request)

        var attempt = 0
        while (true) {
            try {
                val response = chain.proceed(request)
                if (response.code < 500 || attempt >= maxRetries) {
                    return response
                }
                response.close()
            } catch (exception: IOException) {
                if (attempt >= maxRetries) throw exception
            }
            attempt++
            sleep(baseDelayMillis * attempt)
        }
    }
}
