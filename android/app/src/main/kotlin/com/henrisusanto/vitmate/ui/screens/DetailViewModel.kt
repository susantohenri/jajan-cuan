package com.henrisusanto.vitmate.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.repository.DetailCatalog
import com.henrisusanto.vitmate.data.repository.DetailResult
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeDetailData
import com.henrisusanto.vitmate.data.repository.SavedCalcState
import com.henrisusanto.vitmate.domain.CookingDurationParser
import com.henrisusanto.vitmate.domain.HppCalculator
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.domain.HppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientInput(
    val name: String,
    val quantity: String,
    val defaultPrice: Long,
    val priceText: String
)

data class CookingStepUi(
    val text: String,
    val durationSeconds: Int?
)

data class DetailUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val title: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val excerpt: String = "",
    val coverUrl: String? = null,
    val marketDemand: String = "",
    val easeOfMaking: String = "",
    val ingredientAvailability: String = "",
    val estimatedCapital: Long = 0L,
    val ingredients: List<IngredientInput> = emptyList(),
    val steps: List<CookingStepUi> = emptyList(),
    val defaultPortions: Int = 1,
    val portionsText: String = "",
    val marginMode: HppMarginMode = HppMarginMode.PERCENT,
    val marginText: String = "100",
    val hpp: HppResult = HppCalculator.calculate(emptyList(), 1),
    val isFavorite: Boolean = false,
    val isSaving: Boolean = false
)

sealed interface DetailUiEvent {
    data object FavoriteAdded : DetailUiEvent
    data object FavoriteRemoved : DetailUiEvent
    data object NeedsInternet : DetailUiEvent
    data object CalculationSaved : DetailUiEvent
    data object SaveFailed : DetailUiEvent
    data class Error(val message: String) : DetailUiEvent
}

/**
 * Detail resep. Sumber data dan foto ditentukan repository (favorit lokal ->
 * cache -> jaringan), jadi layar ini tetap penuh saat offline untuk favorit.
 */
class DetailViewModel(
    private val recipeId: String,
    private val repository: DetailCatalog,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DetailUiEvent>(extraBufferCapacity = 8)
    val events: Flow<DetailUiEvent> = _events.asSharedFlow()

    /** calculate_hpp dicatat sekali per sesi edit, bukan per ketukan. */
    private var hppLogged = false

    init {
        viewModelScope.launch {
            repository.observeIsFavorite(recipeId).collect { isFavorite ->
                _state.update { it.copy(isFavorite = isFavorite) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, notFound = false, errorMessage = null)
            }
            when (val result = repository.getDetail(recipeId)) {
                is DetailResult.Success -> applyDetail(result.detail)
                DetailResult.NotFound -> _state.update {
                    it.copy(isLoading = false, notFound = true)
                }
                is DetailResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun retry() = load()

    private suspend fun applyDetail(detail: RecipeDetailData) {
        val post = detail.post
        val saved = repository.loadCalcState(recipeId)
        val ingredients = post.ingredients.mapIndexed { index, ingredient ->
            val price = saved?.prices?.getOrNull(index)
                ?: ingredient.defaultPrice.toLong().coerceAtLeast(0L)
            IngredientInput(
                name = ingredient.name,
                quantity = ingredient.quantity,
                defaultPrice = ingredient.defaultPrice.toLong().coerceAtLeast(0L),
                priceText = price.toString()
            )
        }
        val portions = saved?.portions?.takeIf { it > 0 }
            ?: post.defaultPortions.coerceAtLeast(1)
        val marginMode = saved?.marginMode ?: HppMarginMode.PERCENT
        val marginValue = saved?.marginValue
            ?.takeIf { it.isFinite() }
            ?: HppCalculator.DEFAULT_MARGIN_PERCENT

        val base = DetailUiState(
            isLoading = false,
            title = post.title,
            category = post.category,
            tags = post.tags,
            excerpt = post.excerpt,
            coverUrl = detail.coverUrl,
            marketDemand = post.businessAnalysis.marketDemand,
            easeOfMaking = post.businessAnalysis.easeOfMaking,
            ingredientAvailability = post.businessAnalysis.ingredientAvailability,
            estimatedCapital = post.businessAnalysis.estimatedInitialCapital
                .toLong()
                .coerceAtLeast(0L),
            ingredients = ingredients,
            steps = post.cookingSteps.map { step ->
                CookingStepUi(
                    text = step,
                    durationSeconds = CookingDurationParser.parse(step)
                )
            },
            defaultPortions = post.defaultPortions.coerceAtLeast(1),
            portionsText = portions.toString(),
            marginMode = marginMode,
            marginText = formatMargin(marginValue),
            isFavorite = _state.value.isFavorite
        )
        _state.value = base.recalculated()
    }

    fun onPriceChange(index: Int, value: String) {
        val sanitized = value.filter(Char::isDigit).take(MAX_PRICE_DIGITS)
        _state.update { current ->
            if (index !in current.ingredients.indices) return@update current
            val updated = current.ingredients.toMutableList()
            updated[index] = updated[index].copy(priceText = sanitized)
            current.copy(ingredients = updated).recalculated()
        }
        noteEdited()
    }

    fun onPortionsChange(value: String) {
        val sanitized = value.filter(Char::isDigit).take(MAX_PORTION_DIGITS)
        _state.update { it.copy(portionsText = sanitized).recalculated() }
        noteEdited()
    }

    fun onMarginChange(value: String) {
        val sanitized = sanitizeDecimal(value)
        _state.update { it.copy(marginText = sanitized).recalculated() }
        noteEdited()
    }

    fun onMarginModeChange(mode: HppMarginMode) {
        if (_state.value.marginMode == mode) return
        _state.update { it.copy(marginMode = mode).recalculated() }
        noteEdited()
    }

    fun onFavoriteToggle() {
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(recipeId)) {
                FavoriteToggleResult.Added -> {
                    analytics.addFavorite(recipeId)
                    _events.emit(DetailUiEvent.FavoriteAdded)
                }
                FavoriteToggleResult.Removed -> {
                    analytics.removeFavorite(recipeId)
                    _events.emit(DetailUiEvent.FavoriteRemoved)
                }
                FavoriteToggleResult.NeedsInternet ->
                    _events.emit(DetailUiEvent.NeedsInternet)
                is FavoriteToggleResult.Error ->
                    _events.emit(DetailUiEvent.Error(result.message))
            }
        }
    }

    fun onShareOpened() {
        analytics.shareRecipe(recipeId)
    }

    fun onSaveCalculation() {
        val current = _state.value
        if (current.isLoading || current.ingredients.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val saved = SavedCalcState(
                prices = current.ingredients.map { it.priceText.toLongOrNull() ?: 0L },
                portions = current.portionsText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                marginMode = current.marginMode,
                marginValue = current.marginText
                    .replace(',', '.')
                    .toDoubleOrNull()
                    ?.takeIf { it.isFinite() }
                    ?: 0.0
            )
            val success = runCatching {
                repository.saveCalcState(recipeId, saved)
            }.isSuccess
            if (success) {
                analytics.saveCalculation(recipeId)
                // Sesi edit berakhir di titik simpan; edit berikutnya dicatat lagi.
                hppLogged = false
                _events.emit(DetailUiEvent.CalculationSaved)
            } else {
                _events.emit(DetailUiEvent.SaveFailed)
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun noteEdited() {
        if (hppLogged) return
        hppLogged = true
        analytics.calculateHpp(recipeId)
    }

    private fun DetailUiState.recalculated(): DetailUiState {
        val prices = ingredients.map { it.priceText.toLongOrNull() ?: 0L }
        val portions = portionsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val margin = marginText.replace(',', '.').toDoubleOrNull() ?: 0.0
        return copy(
            hpp = HppCalculator.calculate(
                prices = prices,
                portions = portions,
                marginMode = marginMode,
                marginValue = margin
            )
        )
    }

    private fun sanitizeDecimal(raw: String): String {
        val builder = StringBuilder()
        var hasSeparator = false
        raw.forEach { character ->
            when {
                character.isDigit() && builder.length < MAX_MARGIN_DIGITS -> builder.append(character)
                (character == '.' || character == ',') && !hasSeparator && builder.isNotEmpty() -> {
                    hasSeparator = true
                    builder.append('.')
                }
            }
        }
        return builder.toString()
    }

    private fun formatMargin(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

    companion object {
        private const val MAX_PRICE_DIGITS = 12
        private const val MAX_PORTION_DIGITS = 6
        private const val MAX_MARGIN_DIGITS = 8
    }
}
