package com.henrisusanto.vitmate.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.repository.FavoriteToggleResult
import com.henrisusanto.vitmate.data.repository.RecipeCatalog
import com.henrisusanto.vitmate.data.repository.RecipeSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val query: String = "",
    val recipes: List<RecipeSummary> = emptyList(),
    val totalCount: Int = 0
) {
    val isSearching: Boolean get() = query.isNotBlank()
}

/**
 * Favorit dibaca dari snapshot lokal (tabel favorites + file di filesDir),
 * jadi tetap tampil lengkap saat offline. Pencarian hanya menyaring snapshot.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val repository: RecipeCatalog,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _events = MutableSharedFlow<FavoriteUiEvent>(extraBufferCapacity = 8)
    val events: Flow<FavoriteUiEvent> = _events.asSharedFlow()

    private val debouncedQuery: Flow<String> = _query
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .map { it.trim() }
        .distinctUntilChanged()

    private val recipesFlow = debouncedQuery.flatMapLatest { query ->
        repository.observeFavoriteRecipes(query)
    }

    val state: StateFlow<FavoritesUiState> = combine(
        recipesFlow,
        _query,
        repository.observeFavoriteCount()
    ) { recipes, query, total ->
        FavoritesUiState(query = query, recipes = recipes, totalCount = total)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = FavoritesUiState()
    )

    init {
        viewModelScope.launch {
            val totalFavorites = repository.observeFavoriteCount().first()
            analytics.viewFavoritesTab(totalFavorites)
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onClearSearch() {
        _query.value = ""
    }

    fun onRecipeOpened(recipeId: String) {
        analytics.viewRecipe(recipeId)
    }

    fun onRemoveFavorite(recipeId: String) {
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(recipeId)) {
                FavoriteToggleResult.Removed -> {
                    analytics.removeFavorite(recipeId)
                    _events.emit(FavoriteUiEvent.Removed)
                }
                FavoriteToggleResult.Added -> {
                    analytics.addFavorite(recipeId)
                    _events.emit(FavoriteUiEvent.Added)
                }
                FavoriteToggleResult.NeedsInternet -> _events.emit(FavoriteUiEvent.NeedsInternet)
                is FavoriteToggleResult.Error -> _events.emit(FavoriteUiEvent.Error(result.message))
            }
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
