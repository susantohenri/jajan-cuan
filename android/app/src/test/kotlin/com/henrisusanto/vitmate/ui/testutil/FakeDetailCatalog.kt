package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.data.remote.dto.BusinessAnalysisDto
import com.henrisusanto.vitmate.data.remote.dto.IngredientDto
import com.henrisusanto.vitmate.data.remote.dto.PostDto
import com.henrisusanto.vitmate.data.repository.DetailCatalog
import com.henrisusanto.vitmate.data.repository.DetailResult
import com.henrisusanto.vitmate.data.repository.DetailSource
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeDetailData
import com.henrisusanto.vitmate.data.repository.SavedCalcState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Detail palsu untuk unit test ViewModel: sumber detail, status favorit, dan
 * state kalkulator dikendalikan langsung oleh test.
 */
class FakeDetailCatalog : DetailCatalog {

    var detailResult: DetailResult = DetailResult.NotFound
    var getDetailCalls: Int = 0
    var toggleResult: FavoriteToggleResult = FavoriteToggleResult.Added
    var savedState: SavedCalcState? = null
    var saveCalls: Int = 0

    val favorite = MutableStateFlow(false)

    override suspend fun getDetail(recipeId: String): DetailResult {
        getDetailCalls += 1
        return detailResult
    }

    override fun observeIsFavorite(recipeId: String): Flow<Boolean> = favorite

    override suspend fun toggleFavorite(recipeId: String): FavoriteToggleResult {
        if (toggleResult == FavoriteToggleResult.Added) favorite.value = true
        if (toggleResult == FavoriteToggleResult.Removed) favorite.value = false
        return toggleResult
    }

    override suspend fun loadCalcState(recipeId: String): SavedCalcState? = savedState

    override suspend fun saveCalcState(recipeId: String, state: SavedCalcState) {
        saveCalls += 1
        savedState = state
    }
}

/** Resep golden DATA_SCHEMA.md: 10 bahan, modal Rp 39.000, 25 porsi. */
fun goldenDetail(
    source: DetailSource = DetailSource.FAVORITE,
    coverUrl: String? = "/data/user/0/covers/recipe-1/cover"
): RecipeDetailData = RecipeDetailData(
    post = PostDto(
        id = "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
        slug = "mini-spaghetti-ekonomis-untuk-jualan-2ribuan",
        title = "Mini Spaghetti Ekonomis",
        excerpt = "Resep ide jualan modal ~Rp 39.000, hasil 25 porsi (25 mika).",
        category = "gorengan",
        tags = listOf("gorengan", "modal-kecil", "viral"),
        defaultPortions = 25,
        businessAnalysis = BusinessAnalysisDto(
            marketDemand = "Tinggi — cocok untuk kantin sekolah.",
            easeOfMaking = "Mudah — cukup satu wajan.",
            ingredientAvailability = "Semua bahan ada di warung terdekat.",
            estimatedInitialCapital = 39_000
        ),
        ingredients = listOf(
            IngredientDto("Pasta spaghetti", "250 gr pasta", "250 gr", 9_000),
            IngredientDto("Sosis", "5 buah sosis", "5 buah", 3_000),
            IngredientDto("Saus tomat", "1 botol saus", "1 botol", 3_000),
            IngredientDto("Saus cabe", "1 botol saus", "1 botol", 2_000),
            IngredientDto("Garam", "1 sdt", "1 sdt", 500),
            IngredientDto("Oregano", "1 sdt", "1 sdt", 2_000),
            IngredientDto("Kecap manis", "2 sdm", "2 sdm", 1_500),
            IngredientDto("Bawang putih", "3 siung", "3 siung", 1_000),
            IngredientDto("Minyak goreng", "500 ml", "500 ml", 7_000),
            IngredientDto("Keju parut", "50 gr", "50 gr", 10_000)
        ),
        cookingSteps = listOf(
            "Tumis bawang putih 5 menit.",
            "Rebus pasta 10-15 menit.",
            "Sajikan selagi hangat."
        )
    ),
    rawJson = "{}",
    source = source,
    coverUrl = coverUrl
)
