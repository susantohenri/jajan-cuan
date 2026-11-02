package com.henrisusanto.vitmate.data.image

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class OkHttpImageDownloaderTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val downloader = OkHttpImageDownloader(OkHttpClient())

    @Test
    fun `url tanpa skema menghasilkan false tanpa exception`() = runBlocking {
        val destination = File(temporaryFolder.newFolder("covers"), "cover")

        val result = downloader.download("bukan-url", destination)

        assertFalse(result)
        assertFalse(destination.exists())
    }

    @Test
    fun `url dengan karakter ilegal menghasilkan false tanpa exception`() = runBlocking {
        val destination = File(temporaryFolder.newFolder("covers"), "cover")

        val result = downloader.download("http://example.com/gambar tidak valid.jpg", destination)

        assertFalse(result)
        assertFalse(destination.exists())
    }
}
