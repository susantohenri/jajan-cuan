package com.henrisusanto.vitmate.data.testutil

import com.henrisusanto.vitmate.data.image.ImageDownloader
import java.io.File

class FakeImageDownloader : ImageDownloader {
    val downloadedUrls = mutableListOf<String>()
    var failWhen: (String) -> Boolean = { false }

    override suspend fun download(url: String, destination: File): Boolean {
        downloadedUrls += url
        if (failWhen(url)) return false
        destination.parentFile?.mkdirs()
        destination.writeBytes(byteArrayOf(1, 2, 3))
        return true
    }
}
