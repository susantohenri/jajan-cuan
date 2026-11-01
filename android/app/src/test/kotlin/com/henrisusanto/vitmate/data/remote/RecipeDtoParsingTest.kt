package com.henrisusanto.vitmate.data.remote

import com.henrisusanto.vitmate.data.remote.dto.MetaDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto
import com.henrisusanto.vitmate.data.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeDtoParsingTest {

    @Test
    fun `meta fixture terbaca`() {
        val meta = Fixtures.meta()

        assertEquals(209, meta.count)
        assertEquals(1, meta.schema)
        assertTrue(meta.version.isNotBlank())
    }

    @Test
    fun `index fixture berisi 209 resep dan nilai kartu web`() {
        val index = Fixtures.index()

        assertEquals(209, index.size)
        val churros = checkNotNull(index["1065-churros-frozen-ide-jualan"])
        assertEquals("Churros Frozen Crispy", churros.title)
        assertEquals("kue", churros.category)
        assertEquals(listOf("kue", "modal-kecil", "viral"), churros.tags)
        assertEquals(27_500, churros.estCapital)
        assertEquals(3, churros.portions)
        assertTrue(churros.isViral)
        assertEquals(
            "img/recipes/thumbs/1065-churros-frozen-ide-jualan.webp",
            churros.thumb
        )
        assertTrue(churros.cover.startsWith("https://"))
        assertTrue(churros.ingredientsText.contains("terigu"))
    }

    @Test
    fun `detail churros terbaca lengkap`() {
        val post = Fixtures.post("churros").dto

        assertEquals("1065-churros-frozen-ide-jualan", post.id)
        assertEquals("Churros Frozen Crispy", post.title)
        assertEquals("kue", post.category)
        assertEquals(3, post.defaultPortions)
        assertEquals(27_500, post.businessAnalysis.estimatedInitialCapital)
        assertEquals(8, post.ingredients.size)
        assertEquals(3, post.cookingSteps.size)
        assertEquals("ready_for_review", post.media?.status)
        assertEquals(
            "img/recipes/polished/1065-churros-frozen-ide-jualan.jpg",
            post.media?.aiPolishedPhoto
        )
        assertEquals("true", post.titlePolished.toString())
        assertNotNull(post.media?.sourcePhoto)
        assertTrue(post.promoImages.isEmpty())
    }

    @Test
    fun `detail martabak dan es bongko terbaca`() {
        val martabak = Fixtures.post("martabak").dto
        assertEquals("gorengan", martabak.category)
        assertEquals(18, martabak.defaultPortions)
        assertEquals(15_500, martabak.businessAnalysis.estimatedInitialCapital)

        val esBongko = Fixtures.post("es_bongko").dto
        assertEquals("minuman", esBongko.category)
        assertEquals(4, esBongko.defaultPortions)
        assertEquals(11, esBongko.cookingSteps.size)
        assertEquals(41_500, esBongko.businessAnalysis.estimatedInitialCapital)
    }

    @Test
    fun `field opsional dan promo yang tidak ada memakai default`() {
        val post = Fixtures.json.decodeFromString(
            PostDto.serializer(),
            """{"id":"x","title":"Tanpa promo"}"""
        )

        assertEquals(1, post.defaultPortions)
        assertTrue(post.promoImages.isEmpty())
        assertTrue(post.tags.isEmpty())
        assertTrue(post.ingredients.isEmpty())
        assertNull(post.media)
    }

    @Test
    fun `thumb null dan tags null di index tidak membuat parse gagal`() {
        val entry = Fixtures.json.decodeFromString(
            SearchIndexEntryDto.serializer(),
            """{"id":"x","cover":"img/x.jpg","thumb":null,"tags":null}"""
        )

        assertNull(entry.thumb)
        assertTrue(entry.tags.isEmpty())
        assertEquals(1, entry.portions)
    }

    @Test
    fun `field baru yang belum dikenal diabaikan`() {
        val meta = Fixtures.json.decodeFromString(
            MetaDto.serializer(),
            """{"version":"v1","count":2,"schema":1,"future_field":true}"""
        )

        assertEquals("v1", meta.version)
        assertEquals(2, meta.count)
    }
}
