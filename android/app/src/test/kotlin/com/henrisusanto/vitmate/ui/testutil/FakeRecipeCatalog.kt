package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeCatalog
import com.henrisusanto.vitmate.data.repository.RecipeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * Katalog palsu untuk unit test ViewModel. Filter/paginasi/pengacakan ditiru
 * sederhana agar kontrak (query, limit, seed) bisa diverifikasi.
 */
class FakeRecipeCatalog(initial: List<RecipeSummary> = emptyList()) : RecipeCatalog {

    val all = MutableStateFlow(initial)
    val favoriteItems = MutableStateFlow(initial.filter { it.isFavorite })
    val categories = MutableStateFlow(initial.map { it.category }.distinct().sorted())

    var lastCategory: String? = null
    var lastQuery: String = ""
    var lastLimit: Int = 0
    var lastSessionSeed: Long? = null
    var lastFavoriteQuery: String = ""

    var toggleHandler: (String) -> FavoriteToggleResult = ::defaultToggle

    override fun observeRecipes(
        category: String?,
        query: String,
        limit: Int,
        sessionSeed: Long?
    ): Flow<List<RecipeSummary>> {
        lastCategory = category
        lastQuery = query
        lastLimit = limit
        lastSessionSeed = sessionSeed
        return all.map { list -> filter(list, category, query).shuffledOrNot(sessionSeed).take(limit) }
    }

    override fun observeCategories(): Flow<List<String>> = categories

    override fun observeFavoriteRecipes(query: String): Flow<List<RecipeSummary>> {
        lastFavoriteQuery = query
        return favoriteItems.map { list -> filter(list, category = null, query = query) }
    }

    override fun observeFavoriteCount(): Flow<Int> = favoriteItems.map { it.size }

    override fun observeIsFavorite(recipeId: String): Flow<Boolean> =
        favoriteItems.map { list -> list.any { it.id == recipeId } }

    override suspend fun countRecipes(category: String?, query: String): Int =
        filter(all.value, category, query).size

    override suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult =
        toggleHandler(recipeId)

    private fun defaultToggle(recipeId: String): FavoriteToggleResult {
        val isFavorite = favoriteItems.value.any { it.id == recipeId }
        return if (isFavorite) {
            favoriteItems.update { list -> list.filterNot { it.id == recipeId } }
            all.update { list -> list.map { if (it.id == recipeId) it.copy(isFavorite = false) else it } }
            FavoriteToggleResult.Removed
        } else {
            val recipe = all.value.firstOrNull { it.id == recipeId }
                ?: return FavoriteToggleResult.Error("Resep tidak ditemukan.")
            favoriteItems.update { list -> list + recipe.copy(isFavorite = true) }
            all.update { list -> list.map { if (it.id == recipeId) it.copy(isFavorite = true) else it } }
            FavoriteToggleResult.Added
        }
    }

    private fun filter(
        list: List<RecipeSummary>,
        category: String?,
        query: String
    ): List<RecipeSummary> {
        val normalizedQuery = query.trim().lowercase()
        return list.filter { recipe ->
            (category == null || recipe.category == category) &&
                (
                    normalizedQuery.isEmpty() ||
                        recipe.title.lowercase().contains(normalizedQuery) ||
                        recipe.ingredientsText.lowercase().contains(normalizedQuery) ||
                        recipe.tags.any { it.lowercase().contains(normalizedQuery) } ||
                        recipe.category.lowercase().contains(normalizedQuery)
                    )
        }
    }

    private fun List<RecipeSummary>.shuffledOrNot(seed: Long?): List<RecipeSummary> =
        if (seed == null) this else shuffled(Random(seed))
}
