package com.henrisusanto.vitmate.data.model

data class PostResponse(
    val id: String?,
    val slug: String?,
    val title: String?,
    val category: String?,
    val tags: List<String>? = null,
    val published: Boolean?,
    val createdAt: String?,
    val excerpt: String?,
    val defaultPortions: Int?,
    val businessAnalysis: BusinessAnalysis?,
    val ingredients: List<Ingredient>? = null,
    val cookingSteps: List<String>? = null,
    val media: Media?,
    val promoImages: List<String>? = null,
    val ogImage: String?,
    val lastUpdated: String?
)

data class BusinessAnalysis(
    val marketDemand: String?,
    val easeOfMaking: String?,
    val ingredientAvailability: String?,
    val estimatedInitialCapital: Int?
)

data class Ingredient(
    val name: String?,
    val rawText: String?,
    val quantity: String?,
    val defaultPrice: Int?
)

data class Media(
    val sourcePhoto: String?,
    val aiPolishedPhoto: String?,
    val status: String?,
    val notes: String?,
    val mode: String?,
    val polishedAt: String?
)