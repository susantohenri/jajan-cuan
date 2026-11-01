package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.promo.PromoActions
import com.henrisusanto.vitmate.promo.PromoDownloadResult
import com.henrisusanto.vitmate.promo.PromoShareResult

/** Aksi promo palsu; test menentukan hasil unduh/share dan memeriksa panggilan. */
class FakePromoActions : PromoActions {

    var downloadResult: PromoDownloadResult = PromoDownloadResult.Success
    var shareResult: PromoShareResult = PromoShareResult.Failed

    val downloadCalls = mutableListOf<Pair<String, String>>()
    val shareCalls = mutableListOf<Pair<String, String>>()

    override suspend fun download(
        imageUrl: String,
        baseFileName: String
    ): PromoDownloadResult {
        downloadCalls += imageUrl to baseFileName
        return downloadResult
    }

    override suspend fun prepareShare(
        imageUrl: String,
        baseFileName: String
    ): PromoShareResult {
        shareCalls += imageUrl to baseFileName
        return shareResult
    }
}
