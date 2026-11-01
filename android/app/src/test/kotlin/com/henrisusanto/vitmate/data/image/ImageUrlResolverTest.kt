package com.henrisusanto.vitmate.data.image

import com.henrisusanto.vitmate.data.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlResolverTest {

    private val resolver = ImageUrlResolver()

    @Test
    fun `cover http dipakai apa adanya`() {
        val cover = Fixtures.indexEntry("1065-churros-frozen-ide-jualan").cover

        assertEquals(cover, resolver.resolveCover(cover))
    }

    @Test
    fun `thumb relatif memakai origin hosting`() {
        val entry = Fixtures.indexEntry("1065-churros-frozen-ide-jualan")

        assertEquals(
            "https://jajan-cuan.web.app/img/recipes/thumbs/1065-churros-frozen-ide-jualan.webp",
            resolver.resolveThumb(entry.thumb, entry.cover)
        )
    }

    @Test
    fun `thumb null atau kosong jatuh ke cover`() {
        val cover = Fixtures.indexEntry("1065-churros-frozen-ide-jualan").cover

        assertEquals(cover, resolver.resolveThumb(null, cover))
        assertEquals(cover, resolver.resolveThumb("", cover))
        assertEquals(cover, resolver.resolveThumb("   ", cover))
    }

    @Test
    fun `path gambar detail memakai origin hosting`() {
        assertEquals(
            "https://jajan-cuan.web.app/img/recipes/polished/1065-churros-frozen-ide-jualan.jpg",
            resolver.resolveCover("img/recipes/polished/1065-churros-frozen-ide-jualan.jpg")
        )
    }

    @Test
    fun `cover detail mengikuti aturan approved web`() {
        val post = Fixtures.post("churros").dto

        assertEquals(
            post.media?.sourcePhoto,
            resolver.coverFor(post)
        )

        val approved = post.copy(media = post.media?.copy(status = "approved"))
        assertEquals(
            "https://jajan-cuan.web.app/img/recipes/polished/1065-churros-frozen-ide-jualan.jpg",
            resolver.coverFor(approved)
        )
    }

    @Test
    fun `promo menerima path relatif dan URL absolut`() {
        assertEquals(
            "https://jajan-cuan.web.app/img/promo/1.jpg",
            resolver.resolvePromo("img/promo/1.jpg")
        )
        assertEquals(
            "https://cdn.example.com/promo.jpg",
            resolver.resolvePromo("https://cdn.example.com/promo.jpg")
        )
        assertNull(resolver.resolvePromo(null))
        assertNull(resolver.resolvePromo("  "))
    }

    @Test
    fun `daftar promo membuang nilai kosong`() {
        assertEquals(
            listOf(
                "https://jajan-cuan.web.app/img/promo/1.jpg",
                "https://cdn.example.com/promo.jpg"
            ),
            resolver.resolvePromoImages(
                listOf("img/promo/1.jpg", "", "https://cdn.example.com/promo.jpg")
            )
        )
    }

    @Test
    fun `base url tanpa slash tetap menghasilkan satu slash`() {
        val withoutTrailingSlash = ImageUrlResolver("https://jajan-cuan.web.app")

        assertEquals(
            "https://jajan-cuan.web.app/img/x.jpg",
            withoutTrailingSlash.resolveCover("/img/x.jpg")
        )
    }
}
