package com.henrisusanto.vitmate.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MetaDto(
    val version: String = "",
    val count: Int = 0,
    val schema: Int = 1
)

@Serializable
data class SearchIndexEntryDto(
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("ingredients_text") val ingredientsText: String = "",
    val cover: String = "",
    val thumb: String? = null,
    @SerialName("est_capital") val estCapital: Int = 0,
    val portions: Int = 1,
    @SerialName("is_viral") val isViral: Boolean = false
)

@Serializable
data class BusinessAnalysisDto(
    @SerialName("market_demand") val marketDemand: String = "",
    @SerialName("ease_of_making") val easeOfMaking: String = "",
    @SerialName("ingredient_availability") val ingredientAvailability: String = "",
    @SerialName("estimated_initial_capital") val estimatedInitialCapital: Int = 0
)

@Serializable
data class IngredientDto(
    val name: String = "",
    @SerialName("raw_text") val rawText: String = "",
    val quantity: String = "",
    @SerialName("default_price") val defaultPrice: Int = 0
)

@Serializable
data class MediaDto(
    @SerialName("source_photo") val sourcePhoto: String? = null,
    @SerialName("ai_polished_photo") val aiPolishedPhoto: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val mode: String? = null,
    @SerialName("polished_at") val polishedAt: String? = null
)

@Serializable
data class PostDto(
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val excerpt: String = "",
    @SerialName("source_url") val sourceUrl: String? = null,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val published: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("yield_text") val yieldText: String? = null,
    @SerialName("default_portions") val defaultPortions: Int = 1,
    @SerialName("business_analysis") val businessAnalysis: BusinessAnalysisDto = BusinessAnalysisDto(),
    val ingredients: List<IngredientDto> = emptyList(),
    @SerialName("cooking_steps") val cookingSteps: List<String> = emptyList(),
    val media: MediaDto? = null,
    @SerialName("promo_images") val promoImages: List<String> = emptyList(),
    @SerialName("original_title") val originalTitle: String? = null,
    // Data live memakai title_polished bertipe boolean (209/209), bukan string.
    // JsonElement menjaga DTO tetap terbaca bila bentuknya berubah.
    @SerialName("title_polished") val titlePolished: JsonElement? = null,
    @SerialName("title_polished_at") val titlePolishedAt: String? = null,
    @SerialName("og_image") val ogImage: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null
)

/**
 * Detail mentah sekaligus hasil parse. [rawJson] disimpan apa adanya ke cache
 * dan snapshot favorit agar tidak perlu membangun ulang JSON dari DTO.
 */
data class PostPayload(
    val dto: PostDto,
    val rawJson: String
)
