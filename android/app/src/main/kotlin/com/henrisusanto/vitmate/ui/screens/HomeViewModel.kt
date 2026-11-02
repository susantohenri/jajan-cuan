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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class HomeUiState(
    val query: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val recipes: List<RecipeSummary> = emptyList(),
    val totalMatched: Int = 0,
    val isRefreshing: Boolean = false,
    val isInitialLoading: Boolean = true,
    val loadFailed: Boolean = false
) {
    /** Masih ada halaman berikutnya bila jumlah yang tampil belum mencapai total. */
    val hasMore: Boolean get() = recipes.size < totalMatched
}

/**
 * Home + pencarian.
 *
 * Semua paginasi/pengacakan lokal: query Room memakai LIMIT yang bertambah dan
 * seed sesi hanya dipakai saat tidak ada kata kunci, sehingga scroll tidak
 * menduplikasi kartu. Seed diperbarui cold start (instance baru) dan
 * pull-to-refresh.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: RecipeCatalog,
    private val analytics: AnalyticsLogger,
    private val pageSize: Flow<Int> = emptyFlow(),
    private val refresh: suspend (force: Boolean) -> Boolean = { true },
    private val sessionSeedFactory: () -> Long = {
        System.currentTimeMillis() xor Random.nextLong()
    }
) : ViewModel() {

    private val _effectivePageSize = MutableStateFlow(DEFAULT_PAGE_SIZE)

    private val _query = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _limit = MutableStateFlow(_effectivePageSize.value)
    private val _sessionSeed = MutableStateFlow(sessionSeedFactory())
    private val _totalMatched = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isInitialLoading = MutableStateFlow(true)
    private val _loadFailed = MutableStateFlow(false)

    private val _events = MutableSharedFlow<FavoriteUiEvent>(extraBufferCapacity = 8)
    val events: Flow<FavoriteUiEvent> = _events.asSharedFlow()

    private val debouncedQuery: Flow<String> = _query
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .map { it.trim() }
        .distinctUntilChanged()

    private val filter: Flow<Filter> = combine(debouncedQuery, _selectedCategory) { query, category ->
        Filter(query = query, category = category)
    }.distinctUntilChanged()

    private val recipesFlow: Flow<List<RecipeSummary>> = combine(
        filter,
        _limit,
        _sessionSeed
    ) { filter, limit, seed ->
        Triple(filter, limit, seed)
    }.flatMapLatest { (filter, limit, seed) ->
        repository.observeRecipes(
            category = filter.category,
            query = filter.query,
            limit = limit,
            // Pengacakan hanya untuk eksplorasi tanpa kata kunci.
            sessionSeed = if (filter.query.isEmpty()) seed else null
        )
    }

    private val categoriesFlow: Flow<List<String>> = repository.observeCategories()

    private val content = combine(
        recipesFlow,
        categoriesFlow,
        debouncedQuery,
        _selectedCategory
    ) { recipes, categories, query, category ->
        Content(
            recipes = recipes,
            categories = categories,
            query = query,
            selectedCategory = category
        )
    }

    val state: StateFlow<HomeUiState> = combine(
        content,
        _totalMatched,
        _isRefreshing,
        _isInitialLoading,
        _loadFailed
    ) { content, total, refreshing, initialLoading, failed ->
        HomeUiState(
            query = content.query,
            selectedCategory = content.selectedCategory,
            categories = content.categories,
            recipes = content.recipes,
            totalMatched = total,
            isRefreshing = refreshing,
            isInitialLoading = initialLoading,
            loadFailed = failed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    init {
        // pageSize dari Remote Config dapat berubah; terapkan ulang tanpa
        // menggeser daftar agar scroll tidak lompat.
        viewModelScope.launch {
            pageSize.collect { size ->
                _effectivePageSize.value = size.coerceAtLeast(1)
                _limit.value = _effectivePageSize.value
            }
        }

        // Limit kembali ke halaman pertama dan jumlah hasil dihitung ulang
        // setiap kategori/kata kunci final berubah.
        viewModelScope.launch {
            filter.collectLatest { current ->
                _limit.value = _effectivePageSize.value
                _totalMatched.value = repository.countRecipes(
                    category = current.category,
                    query = current.query
                )
            }
        }

        // Log satu kali per term final (setelah debounce + distinct).
        viewModelScope.launch {
            debouncedQuery.collectLatest { term ->
                if (term.isNotEmpty()) {
                    analytics.recipeSearch(
                        searchTerm = term,
                        resultCount = repository.countRecipes(
                            category = _selectedCategory.value,
                            query = term
                        )
                    )
                }
            }
        }

        // Cold start tanpa cache: tarik index dulu agar layar tidak kosong
        // tanpa penjelasan. Sync lokal lain ditangani ProcessLifecycle.
        viewModelScope.launch {
            val hasCache = repository.countRecipes(category = null, query = "") > 0
            if (!hasCache) {
                val success = refresh(false)
                if (!success) {
                    _loadFailed.value = true
                }
            }
            _isInitialLoading.value = false
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
        analytics.changeCategory(category ?: CATEGORY_ALL_EVENT_VALUE)
    }

    fun onShowAllRecipes() {
        _query.value = ""
        _selectedCategory.value = null
    }

    fun onRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            _sessionSeed.value = sessionSeedFactory()
            val success = refresh(true)
            _loadFailed.value = !success && state.value.recipes.isEmpty()
            _isRefreshing.value = false
        }
    }

    fun onRetry() = onRefresh()

    fun onLoadMore() {
        if (_isRefreshing.value || !state.value.hasMore) return
        _limit.update { current -> current + _effectivePageSize.value }
    }

    fun onRecipeOpened(recipeId: String) {
        analytics.viewRecipe(recipeId)
    }

    fun onFavoriteToggle(recipeId: String) {
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(recipeId)) {
                FavoriteToggleResult.Added -> {
                    analytics.addFavorite(recipeId)
                    _events.emit(FavoriteUiEvent.Added)
                }
                FavoriteToggleResult.Removed -> {
                    analytics.removeFavorite(recipeId)
                    _events.emit(FavoriteUiEvent.Removed)
                }
                FavoriteToggleResult.NeedsInternet -> _events.emit(FavoriteUiEvent.NeedsInternet)
                is FavoriteToggleResult.Error -> _events.emit(FavoriteUiEvent.Error(result.message))
            }
        }
    }

    private data class Filter(val query: String, val category: String?)

    private data class Content(
        val recipes: List<RecipeSummary>,
        val categories: List<String>,
        val query: String,
        val selectedCategory: String?
    )

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val CATEGORY_ALL_EVENT_VALUE = "Semua"
    }
}
