package com.henrisusanto.vitmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.ads.AdManager
import com.henrisusanto.vitmate.ads.NativeAdCard
import com.henrisusanto.vitmate.data.repository.RecipeSummary
import com.henrisusanto.vitmate.ui.components.EmptyCatalogState
import com.henrisusanto.vitmate.ui.components.ErrorState
import com.henrisusanto.vitmate.ui.components.RecipeCard
import com.henrisusanto.vitmate.ui.components.SearchEmptyState
import com.henrisusanto.vitmate.ui.components.StickyBannerSlot

private const val TABLET_CONTENT_MAX_WIDTH_DP = 840
private const val LOAD_MORE_THRESHOLD = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    adManager: AdManager,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var searchExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.query) {
        if (state.query.isNotEmpty()) searchExpanded = true
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                FavoriteUiEvent.Added -> context.getString(R.string.snackbar_saved)
                FavoriteUiEvent.Removed -> context.getString(R.string.snackbar_removed)
                FavoriteUiEvent.NeedsInternet -> context.getString(R.string.snackbar_need_internet)
                is FavoriteUiEvent.Error -> event.message
            }
            when (event) {
                FavoriteUiEvent.Added, FavoriteUiEvent.Removed ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                else -> Unit
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = TABLET_CONTENT_MAX_WIDTH_DP.dp)
                    .fillMaxWidth()
            ) {
                HomeHeader(
                    query = state.query,
                    expanded = searchExpanded,
                    onQueryChange = viewModel::onQueryChange,
                    onExpand = { searchExpanded = true },
                    onCollapse = {
                        viewModel.onQueryChange("")
                        searchExpanded = false
                    }
                )

                CategoryRow(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onSelect = viewModel::onCategorySelected
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HomeContent(
                            state = state,
                            adManager = adManager,
                            onRetry = viewModel::onRetry,
                            onShowAll = viewModel::onShowAllRecipes,
                            onSuggestionSelected = { category ->
                                viewModel.onQueryChange("")
                                viewModel.onCategorySelected(category)
                            },
                            onRecipeOpened = { recipeId ->
                                viewModel.onRecipeOpened(recipeId)
                                onNavigateToDetail(recipeId)
                            },
                            onFavoriteToggle = viewModel::onFavoriteToggle,
                            onLoadMore = viewModel::onLoadMore
                        )
                    }
                }

                StickyBannerSlot(
                    adManager = adManager,
                    screen = AdManager.SCREEN_HOME
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeader(
    query: String,
    expanded: Boolean,
    onQueryChange: (String) -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (expanded) {
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.home_search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    val hasQuery = query.isNotEmpty()
                    IconButton(
                        onClick = { if (hasQuery) onQueryChange("") else onCollapse() }
                    ) {
                        Icon(
                            imageVector = if (hasQuery) Icons.Default.Clear else Icons.Default.Close,
                            contentDescription = stringResource(
                                if (hasQuery) {
                                    R.string.search_clear_content_desc
                                } else {
                                    R.string.search_close_content_desc
                                }
                            )
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_header_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onExpand) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_expand_content_desc)
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.chip_all)) }
            )
        }
        items(items = categories, key = { it }) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    adManager: AdManager,
    onRetry: () -> Unit,
    onShowAll: () -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onRecipeOpened: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    when {
        state.recipes.isEmpty() && state.isInitialLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.recipes.isEmpty() && state.loadFailed -> {
            ErrorState(
                message = stringResource(R.string.error_loading),
                onRetry = onRetry
            )
        }

        state.recipes.isEmpty() && state.query.isNotBlank() -> {
            SearchEmptyState(
                query = state.query,
                suggestions = state.categories.take(SUGGESTION_COUNT),
                onSuggestionSelected = onSuggestionSelected,
                onShowAll = onShowAll
            )
        }

        state.recipes.isEmpty() -> {
            EmptyCatalogState(onRetry = onRetry)
        }

        else -> {
            RecipeList(
                recipes = state.recipes,
                hasMore = state.hasMore,
                adManager = adManager,
                onRecipeOpened = onRecipeOpened,
                onFavoriteToggle = onFavoriteToggle,
                onLoadMore = onLoadMore
            )
        }
    }
}

@Composable
private fun RecipeList(
    recipes: List<RecipeSummary>,
    hasMore: Boolean,
    adManager: AdManager,
    onRecipeOpened: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val nativeEveryN = adManager.nativeEveryN()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = recipes,
            key = { _, recipe -> recipe.id }
        ) { index, recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeOpened(recipe.id) },
                onActionClick = { onFavoriteToggle(recipe.id) },
                actionContentDescription = stringResource(
                    if (recipe.isFavorite) {
                        R.string.favorite_remove_content_desc
                    } else {
                        R.string.favorite_add_content_desc
                    }
                ),
                actionIcon = if (recipe.isFavorite) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                }
            )
            if ((index + 1) % nativeEveryN == 0) {
                NativeAdCard(
                    adManager = adManager,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            if (index >= recipes.lastIndex - LOAD_MORE_THRESHOLD && hasMore) {
                LaunchedEffect(recipes.size) {
                    onLoadMore()
                }
            }
        }
    }
}

private const val SUGGESTION_COUNT = 4
