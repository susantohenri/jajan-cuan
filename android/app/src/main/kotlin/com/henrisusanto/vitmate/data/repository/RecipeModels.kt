package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.remote.dto.PostDto

data class RecipeSummary(
    val id: String,
    val slug: String,
    val title: String,
    val category: String,
    val tags: List<String>,
    val ingredientsText: String,
    val coverUrl: String?,
    val thumbUrl: String?,
    val estCapital: Int,
    val portions: Int,
    val isViral: Boolean,
    val isFavorite: Boolean
)

enum class DetailSource {
    FAVORITE,
    CACHE,
    NETWORK
}

data class RecipeDetailData(
    val post: PostDto,
    val rawJson: String,
    val source: DetailSource,
    /** Path file lokal untuk favorit, atau URL penuh untuk cache/jaringan. */
    val coverUrl: String? = null
)

sealed interface DetailResult {
    data class Success(val detail: RecipeDetailData) : DetailResult
    data object NotFound : DetailResult
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : DetailResult
}

sealed interface FavoriteToggleResult {
    data object Added : FavoriteToggleResult
    data object Removed : FavoriteToggleResult
    data object NeedsInternet : FavoriteToggleResult
    data class Error(val message: String) : FavoriteToggleResult
}
