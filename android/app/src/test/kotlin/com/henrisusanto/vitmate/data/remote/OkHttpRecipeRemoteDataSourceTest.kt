package com.henrisusanto.vitmate.data.remote

import com.henrisusanto.vitmate.data.testutil.Fixtures
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OkHttpRecipeRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var source: OkHttpRecipeRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        source = OkHttpRecipeRemoteDataSource(
            baseUrl = server.url("/").toString(),
            client = OkHttpClient(),
            retryDelayMillis = {}
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `meta diparse dari endpoint meta`() = runBlocking {
        server.enqueue(MockResponse().setBody(Fixtures.metaJson()))

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Success)
        val meta = (result as RemoteResult.Success).value
        assertEquals(209, meta.count)
        assertEquals("/meta.json", server.takeRequest().path)
    }

    @Test
    fun `search index diparse menjadi map 209 item`() = runBlocking {
        server.enqueue(MockResponse().setBody(Fixtures.indexJson()))

        val result = source.fetchSearchIndex()

        assertTrue(result is RemoteResult.Success)
        val index = (result as RemoteResult.Success).value
        assertEquals(209, index.size)
        assertEquals("Churros Frozen Crispy", index["1065-churros-frozen-ide-jualan"]?.title)
        assertEquals("/search_index.json", server.takeRequest().path)
    }

    @Test
    fun `post diparse dan json mentah dipertahankan`() = runBlocking {
        val fixture = Fixtures.postJson("churros")
        server.enqueue(MockResponse().setBody(fixture))

        val result = source.fetchPost("1065-churros-frozen-ide-jualan")

        assertTrue(result is RemoteResult.Success)
        val payload = (result as RemoteResult.Success).value
        assertEquals(fixture, payload.rawJson)
        assertEquals("Churros Frozen Crispy", payload.dto.title)
        assertEquals(
            "/posts/1065-churros-frozen-ide-jualan.json",
            server.takeRequest().path
        )
    }

    @Test
    fun `404 dan body null menghasilkan NoData`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        assertTrue(source.fetchPost("tidak-ada") is RemoteResult.NoData)

        server.enqueue(MockResponse().setBody("null"))
        assertTrue(source.fetchPost("tidak-ada") is RemoteResult.NoData)
    }

    @Test
    fun `GET 5xx diulang lalu sukses`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody(Fixtures.metaJson()))

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Success)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `GET 5xx terus menghasilkan Failure HTTP`() = runBlocking {
        repeat(3) { server.enqueue(MockResponse().setResponseCode(503)) }

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Failure)
        assertEquals(FailureKind.HTTP, (result as RemoteResult.Failure).kind)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `gagal jaringan menghasilkan Failure NETWORK`() = runBlocking {
        val noRetrySource = OkHttpRecipeRemoteDataSource(
            baseUrl = server.url("/").toString(),
            client = OkHttpClient(),
            maxRetries = 0,
            retryDelayMillis = {}
        )
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )

        val result = noRetrySource.fetchMeta()

        assertTrue(result is RemoteResult.Failure)
        assertEquals(FailureKind.NETWORK, (result as RemoteResult.Failure).kind)
    }

    @Test
    fun `respons gzip didekompresi transparan`() = runBlocking {
        val compressed = ByteArrayOutputStream().also { output ->
            GZIPOutputStream(output).use { gzip ->
                gzip.write(Fixtures.metaJson().toByteArray())
            }
        }.toByteArray()
        server.enqueue(
            MockResponse()
                .setHeader("Content-Encoding", "gzip")
                .setBody(Buffer().write(compressed))
        )

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Success)
        assertEquals(209, (result as RemoteResult.Success).value.count)
        assertTrue(
            server.takeRequest().getHeader("Accept-Encoding")?.contains("gzip") == true
        )
    }

    @Test
    fun `body bukan json menghasilkan Failure PARSE`() = runBlocking {
        server.enqueue(MockResponse().setBody("bukan-json"))

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Failure)
        assertEquals(FailureKind.PARSE, (result as RemoteResult.Failure).kind)
    }

    @Test
    fun `4xx tidak diulang`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403))

        val result = source.fetchMeta()

        assertTrue(result is RemoteResult.Failure)
        assertEquals(FailureKind.HTTP, (result as RemoteResult.Failure).kind)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `backoff retry memakai delay coroutine 250ms lalu 500ms`() = runBlocking {
        val delays = mutableListOf<Long>()
        val retrySource = OkHttpRecipeRemoteDataSource(
            baseUrl = server.url("/").toString(),
            client = OkHttpClient(),
            retryDelayMillis = { delays += it }
        )
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody(Fixtures.metaJson()))

        val result = retrySource.fetchMeta()

        assertTrue(result is RemoteResult.Success)
        assertEquals(listOf(250L, 500L), delays)
        assertEquals(3, server.requestCount)
    }
}
