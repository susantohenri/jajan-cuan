package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.domain.HppMarginMode
import kotlinx.coroutines.flow.Flow

/**
 * Data yang dibutuhkan layar Detail: detail resep, status favorit, dan state
 * kalkulator. Dipisah dari [RecipeCatalog] agar Home/Favorit tidak ikut
 * bergantung pada kalkulator.
 */
interface DetailCatalog {
    suspend fun getDetail(recipeId: String): DetailResult

    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult

    suspend fun loadCalcState(recipeId: String): SavedCalcState?

    suspend fun saveCalcState(recipeId: String, state: SavedCalcState)
}

/**
 * State kalkulator yang tersimpan. [prices] selaras dengan urutan bahan pada
 * detail; bila jumlah bahan berubah, harga yang tidak ada memakai harga acuan.
 */
data class SavedCalcState(
    val prices: List<Long>,
    val portions: Int,
    val marginMode: HppMarginMode,
    val marginValue: Double
)
