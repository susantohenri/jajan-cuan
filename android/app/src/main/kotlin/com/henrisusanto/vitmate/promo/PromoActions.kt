package com.henrisusanto.vitmate.promo

import android.net.Uri

sealed interface PromoDownloadResult {
    data object Success : PromoDownloadResult
    data object PermissionDenied : PromoDownloadResult
    data object NoInternet : PromoDownloadResult
    data object Failed : PromoDownloadResult
}

sealed interface PromoShareResult {
    data class Ready(val uri: Uri, val mimeType: String) : PromoShareResult
    data object NoInternet : PromoShareResult
    data object Failed : PromoShareResult
}

/**
 * Aksi platform untuk Konten Promosi. Dipisah dari ViewModel agar tidak ada
 * Context/MediaStore di unit test.
 */
interface PromoActions {
    suspend fun download(imageUrl: String, baseFileName: String): PromoDownloadResult

    suspend fun prepareShare(imageUrl: String, baseFileName: String): PromoShareResult
}
