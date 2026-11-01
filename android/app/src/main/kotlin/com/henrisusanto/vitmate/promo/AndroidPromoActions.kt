package com.henrisusanto.vitmate.promo

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Unduh foto promo ke Galeri lewat MediaStore (tanpa izin di API 29+) dan
 * siapkan file cache untuk dibagikan lewat FileProvider.
 */
class AndroidPromoActions(
    context: Context,
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis
) : PromoActions {
    private val appContext = context.applicationContext

    override suspend fun download(
        imageUrl: String,
        baseFileName: String
    ): PromoDownloadResult = withContext(ioDispatcher) {
        if (!appContext.hasInternetConnection()) return@withContext PromoDownloadResult.NoInternet
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext PromoDownloadResult.PermissionDenied
        }

        val bytes = fetch(imageUrl) ?: return@withContext failureResult()
        val mimeType = imageMimeType(imageUrl)
        val fileName = buildFileName(baseFileName, imageUrl, withTimestamp = true)

        val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(bytes, mimeType, fileName)
        } else {
            saveLegacy(bytes, mimeType, fileName)
        }
        if (saved) PromoDownloadResult.Success else PromoDownloadResult.Failed
    }

    override suspend fun prepareShare(
        imageUrl: String,
        baseFileName: String
    ): PromoShareResult = withContext(ioDispatcher) {
        if (!appContext.hasInternetConnection()) return@withContext PromoShareResult.NoInternet

        val bytes = fetch(imageUrl) ?: return@withContext failureShareResult()
        val mimeType = imageMimeType(imageUrl)
        val fileName = buildFileName(baseFileName, imageUrl, withTimestamp = false)

        val directory = File(appContext.cacheDir, SHARE_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) {
            return@withContext PromoShareResult.Failed
        }
        // Hanya satu file share yang perlu hidup pada satu waktu.
        directory.listFiles()?.forEach { it.delete() }

        val file = File(directory, fileName)
        val written = runCatching { file.writeBytes(bytes) }.isSuccess
        if (!written) return@withContext PromoShareResult.Failed

        runCatching {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
        }.map { uri -> PromoShareResult.Ready(uri, mimeType) }
            .getOrDefault(PromoShareResult.Failed)
    }

    private fun fetch(url: String): ByteArray? = runCatching {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            response.body?.bytes()
        }
    }.getOrNull()

    private fun failureResult(): PromoDownloadResult =
        if (appContext.hasInternetConnection()) {
            PromoDownloadResult.Failed
        } else {
            PromoDownloadResult.NoInternet
        }

    private fun failureShareResult(): PromoShareResult =
        if (appContext.hasInternetConnection()) {
            PromoShareResult.Failed
        } else {
            PromoShareResult.NoInternet
        }

    private fun saveWithMediaStore(bytes: ByteArray, mimeType: String, fileName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$GALLERY_FOLDER"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
        val uri = resolver.insert(collection, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("OutputStream tidak tersedia")
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null
            )
            true
        } catch (exception: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(bytes: ByteArray, mimeType: String, fileName: String): Boolean {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            GALLERY_FOLDER
        )
        if (!directory.exists() && !directory.mkdirs()) return false

        val file = File(directory, fileName)
        return try {
            file.outputStream().use { it.write(bytes) }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.DATA, file.absolutePath)
            }
            appContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) != null
        } catch (exception: Exception) {
            file.delete()
            false
        }
    }

    private fun buildFileName(
        baseFileName: String,
        imageUrl: String,
        withTimestamp: Boolean
    ): String {
        val extension = imageExtension(imageUrl)
        val stamp = if (withTimestamp) "-${clock()}" else ""
        return "$baseFileName$stamp.$extension"
    }

    private companion object {
        const val GALLERY_FOLDER = "JajanCuan"
        const val SHARE_DIRECTORY = "promo"
    }
}
