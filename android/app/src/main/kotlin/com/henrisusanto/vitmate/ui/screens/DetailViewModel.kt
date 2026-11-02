package com.henrisusanto.vitmate.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrisusanto.vitmate.ads.RewardedAdSession
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.image.ImageUrlResolver
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.repository.ActiveTimer
import com.henrisusanto.vitmate.data.repository.CookingTimers
import com.henrisusanto.vitmate.data.repository.DetailCatalog
import com.henrisusanto.vitmate.data.repository.DetailResult
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeDetailData
import com.henrisusanto.vitmate.data.repository.SavedCalcState
import com.henrisusanto.vitmate.data.repository.UnlockCatalog
import com.henrisusanto.vitmate.domain.CookingDurationParser
import com.henrisusanto.vitmate.domain.HppCalculator
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.domain.HppResult
import com.henrisusanto.vitmate.promo.PromoActions
import com.henrisusanto.vitmate.promo.PromoDownloadResult
import com.henrisusanto.vitmate.promo.PromoShareResult
import com.henrisusanto.vitmate.ui.gate.FeatureGateCoordinator
import com.henrisusanto.vitmate.ui.gate.FeatureGateUiState
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
    val isSaving: Boolean = false,
    val activeTimers: List<ActiveTimer> = emptyList(),
    val promoImages: List<String> = emptyList(),
    val calculatorGate: FeatureGateUiState = FeatureGateUiState(),
    val promoGate: FeatureGateUiState = FeatureGateUiState()
)

sealed interface DetailUiEvent {
    data object FavoriteAdded : DetailUiEvent
    data object FavoriteRemoved : DetailUiEvent
    data object NeedsInternet : DetailUiEvent
    data object CalculationSaved : DetailUiEvent
    data object SaveFailed : DetailUiEvent
    data object TimerFailed : DetailUiEvent
    data object RewardedFallback : DetailUiEvent
    data object PromoDownloaded : DetailUiEvent
    data object PromoDownloadFailed : DetailUiEvent
    data object PromoPermissionDenied : DetailUiEvent
    data object PromoNoInternet : DetailUiEvent
    data object PromoShareFailed : DetailUiEvent
    data class PromoShareReady(val uri: Uri, val mimeType: String) : DetailUiEvent
    data class Error(val message: String) : DetailUiEvent
}

/**
 * Detail resep. Sumber data dan foto ditentukan repository (favorit lokal ->
 * cache -> jaringan), jadi layar ini tetap penuh saat offline untuk favorit.
 */
class DetailViewModel(
    private val recipeId: String,
    private val repository: DetailCatalog,
    private val timers: CookingTimers,
    private val analytics: AnalyticsLogger,
    private val unlocks: UnlockCatalog,
    private val rewardedSession: RewardedAdSession,
    private val promoActions: PromoActions,
    private val imageUrlResolver: ImageUrlResolver,
    private val fallbackUnlockEnabled: () -> Boolean
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DetailUiEvent>(extraBufferCapacity = 8)
    val events: Flow<DetailUiEvent> = _events.asSharedFlow()

    private val gateCoordinator = FeatureGateCoordinator(
        recipeId = recipeId,
        unlocks = unlocks,
        adSession = rewardedSession,
        analytics = analytics,
        fallbackEnabled = fallbackUnlockEnabled,
        scope = viewModelScope,
        onFallback = { _events.tryEmit(DetailUiEvent.RewardedFallback) }
    )

    /** calculate_hpp dicatat sekali per sesi edit, bukan per ketukan. */
    private var hppLogged = false

    init {
        viewModelScope.launch {
            gateCoordinator.states.collect { gates ->
                _state.update { current ->
                    current.copy(
                        calculatorGate = gates[UnlockFeature.CALCULATOR]
                            ?: FeatureGateUiState(),
                        promoGate = gates[UnlockFeature.PROMO] ?: FeatureGateUiState()
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeIsFavorite(recipeId).collect { isFavorite ->
                _state.update { it.copy(isFavorite = isFavorite) }
            }
        }
        viewModelScope.launch {
            timers.observeActive().collect { activeTimers ->
                _state.update { it.copy(activeTimers = activeTimers) }
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
        val promoImages = imageUrlResolver.resolvePromoImages(post.promoImages)

        gateCoordinator.updateAvailableFeatures(
            buildSet {
                add(UnlockFeature.CALCULATOR)
                if (promoImages.isNotEmpty()) add(UnlockFeature.PROMO)
            }
        )

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
            isFavorite = _state.value.isFavorite,
            activeTimers = _state.value.activeTimers,
            promoImages = promoImages,
            calculatorGate = _state.value.calculatorGate,
            promoGate = _state.value.promoGate
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

    fun onGateClick(feature: UnlockFeature) {
        gateCoordinator.onGateClick(feature)
    }

    fun onGateDialogDismiss() {
        gateCoordinator.dismissDialog()
    }

    fun onGateDialogConfirm(activity: Activity) {
        gateCoordinator.confirmDialog(activity)
    }

    fun onPromoDownload(index: Int) {
        val url = _state.value.promoImages.getOrNull(index) ?: return
        viewModelScope.launch {
            when (promoActions.download(url, promoBaseName(index))) {
                PromoDownloadResult.Success -> {
                    analytics.promoDownload(recipeId)
                    _events.emit(DetailUiEvent.PromoDownloaded)
                }
                PromoDownloadResult.PermissionDenied ->
                    _events.emit(DetailUiEvent.PromoPermissionDenied)
                PromoDownloadResult.NoInternet ->
                    _events.emit(DetailUiEvent.PromoNoInternet)
                PromoDownloadResult.Failed ->
                    _events.emit(DetailUiEvent.PromoDownloadFailed)
            }
        }
    }

    fun onPromoShare(index: Int) {
        val url = _state.value.promoImages.getOrNull(index) ?: return
        viewModelScope.launch {
            when (val result = promoActions.prepareShare(url, promoBaseName(index))) {
                is PromoShareResult.Ready -> {
                    analytics.promoShare(recipeId)
                    _events.emit(
                        DetailUiEvent.PromoShareReady(result.uri, result.mimeType)
                    )
                }
                PromoShareResult.NoInternet ->
                    _events.emit(DetailUiEvent.PromoNoInternet)
                PromoShareResult.Failed ->
                    _events.emit(DetailUiEvent.PromoShareFailed)
            }
        }
    }

    override fun onCleared() {
        gateCoordinator.destroy()
        rewardedSession.destroy()
        super.onCleared()
    }

    private fun promoBaseName(index: Int): String =
        "jajan-cuan-${recipeId.replace(NON_FILE_NAME_CHAR, "_")}-${index + 1}"

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

    fun onCancelTimer(timerId: String) {
        viewModelScope.launch {
            if (runCatching { timers.cancel(timerId) }.isFailure) {
                _events.emit(DetailUiEvent.TimerFailed)
            }
        }
    }

    fun startTimer(label: String, durationSeconds: Long) {
        val current = _state.value
        val effectiveLabel = label.trim().ifBlank { current.title }
        if (durationSeconds < MIN_TIMER_SECONDS) {
            viewModelScope.launch { _events.emit(DetailUiEvent.TimerFailed) }
            return
        }
        viewModelScope.launch {
            val started = runCatching {
                timers.start(
                    recipeId = recipeId,
                    recipeTitle = current.title,
                    label = effectiveLabel,
                    durationSeconds = durationSeconds
                )
            }.getOrNull()
            if (started == null) {
                _events.emit(DetailUiEvent.TimerFailed)
            } else {
                analytics.startCookingTimer(recipeId, durationSeconds)
            }
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
        private const val MIN_TIMER_SECONDS = 1L
        private val NON_FILE_NAME_CHAR = Regex("[^A-Za-z0-9_-]")
    }
}
