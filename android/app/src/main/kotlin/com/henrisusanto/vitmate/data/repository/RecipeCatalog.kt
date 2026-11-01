package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.local.RecipeIndexEntity
import com.henrisusanto.vitmate.data.local.RecipeSummary
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.remote.FavoriteToggleResult
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak baca data katalog/favorit yang dimakali ViewModel layar S6/S7 tanpa
 * mengekspos implementasi Room ke layar.
 */
interface RecipeCatalog {
    fun observeRecipes(
        category: String?,
        query: String,
        limit: Int,
        sessionSeed: Long? = null
    ): Flow<List<RecipeSummary>>

    fun observeCategories(): Flow<List<String>>

    fun observeFavoriteRecipes(query: String = ""): Flow<List<RecipeSummary>>

    fun observeFavoriteCount(): Flow<Int>

    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult

    suspend fun countRecipes(category: String?, query: String): Int
}
