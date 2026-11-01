package com.henrisusanto.vitmate.data.image

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

interface ImageDownloader {
    suspend fun download(url: String, destination: File): Boolean
}

/**
 * Mengunduh gambar ke file lokal secara atomik: tulis ke file sementara di
 * direktori yang sama, lalu rename. File lama tidak tersentuh bila unduhan
 * gagal.
 */
class OkHttpImageDownloader(
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageDownloader {

    override suspend fun download(url: String, destination: File): Boolean =
        withContext(ioDispatcher) {
            val parent = destination.parentFile ?: return@withContext false
            if (!parent.exists() && !parent.mkdirs()) return@withContext false

            var temporaryFile: File? = null
            try {
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val body = response.body ?: return@withContext false
                    if (body.contentLength() == 0L) return@withContext false

                    temporaryFile = File.createTempFile("image-", ".tmp", parent)
                    body.byteStream().use { input ->
                        temporaryFile!!.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (temporaryFile!!.length() == 0L) return@withContext false
                    if (destination.exists() && !destination.delete()) {
                        return@withContext false
                    }
                    if (!temporaryFile!!.renameTo(destination)) {
                        return@withContext false
                    }
                    temporaryFile = null
                    true
                }
            } catch (exception: IOException) {
                false
            } finally {
                temporaryFile?.delete()
            }
        }
}
