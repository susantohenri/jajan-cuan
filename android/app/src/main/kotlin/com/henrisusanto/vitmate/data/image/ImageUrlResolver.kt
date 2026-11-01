package com.henrisusanto.vitmate.data.image

import com.henrisusanto.vitmate.data.remote.dto.MediaDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto

/**
 * Meniru logika URL gambar web:
 * - path relatif (mis. img/recipes/...) memakai origin Hosting;
 * - URL http/https dipakai apa adanya;
 * - thumbnail null/kosong jatuh ke cover.
 */
class ImageUrlResolver(
    private val baseUrl: String = DEFAULT_BASE_URL
) {
    private val normalizedBaseUrl = baseUrl.trimEnd('/')

    fun resolveCover(raw: String?): String? = resolve(raw)

    fun resolveThumb(thumb: String?, cover: String?): String? =
        resolve(thumb) ?: resolve(cover)

    fun resolvePromo(raw: String?): String? = resolve(raw)

    fun resolvePromoImages(raw: List<String>): List<String> =
        raw.mapNotNull(::resolvePromo)

    /**
     * Cover detail mengikuti release.js: foto polesan hanya dipakai bila
     * status approved; selain itu source_photo, lalu og_image.
     */
    fun coverFor(post: PostDto): String? {
        val media: MediaDto? = post.media
        val raw = if (media?.status == STATUS_APPROVED && !media.aiPolishedPhoto.isNullOrBlank()) {
            media.aiPolishedPhoto
        } else {
            media?.sourcePhoto?.takeIf { it.isNotBlank() } ?: post.ogImage
        }
        return resolve(raw)
    }

    private fun resolve(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
        ) {
            return value
        }
        return "$normalizedBaseUrl/${value.trimStart('/')}"
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://jajan-cuan.web.app/"
        private const val STATUS_APPROVED = "approved"
    }
}
