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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.henrisusanto.vitmate.ui.components.FavoritesEmptyState
import com.henrisusanto.vitmate.ui.components.RecipeCard
import com.henrisusanto.vitmate.ui.components.SearchEmptyState
import com.henrisusanto.vitmate.ui.components.StickyBannerSlot

private const val TABLET_CONTENT_MAX_WIDTH_DP = 840

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    adManager: AdManager,
    onNavigateToDetail: (String) -> Unit,
    onExploreRecipes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                FavoriteUiEvent.Added -> context.getString(R.string.snackbar_saved)
                FavoriteUiEvent.Removed -> context.getString(R.string.snackbar_removed)
                FavoriteUiEvent.NeedsInternet -> context.getString(R.string.snackbar_need_internet)
                is FavoriteUiEvent.Error -> event.message
            }
            if (event == FavoriteUiEvent.Removed || event == FavoriteUiEvent.Added) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                FavoritesHeader(
                    totalCount = state.totalCount,
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    onClearQuery = viewModel::onClearSearch
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        state.totalCount == 0 -> {
                            FavoritesEmptyState(onExplore = onExploreRecipes)
                        }

                        state.recipes.isEmpty() && state.query.isNotBlank() -> {
                            SearchEmptyState(
                                query = state.query,
                                suggestions = emptyList(),
                                onSuggestionSelected = {},
                                onShowAll = viewModel::onClearSearch
                            )
                        }

                        else -> {
                            FavoritesList(
                                recipes = state.recipes,
                                adManager = adManager,
                                onRecipeOpened = { recipeId ->
                                    viewModel.onRecipeOpened(recipeId)
                                    onNavigateToDetail(recipeId)
                                },
                                onRemoveFavorite = viewModel::onRemoveFavorite
                            )
                        }
                    }
                }

                StickyBannerSlot(
                    adManager = adManager,
                    screen = AdManager.SCREEN_FAVORITES
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader(
    totalCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.favorites_header, totalCount),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.favorites_search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.search_clear_content_desc)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
    }
}

@Composable
private fun FavoritesList(
    recipes: List<RecipeSummary>,
    adManager: AdManager,
    onRecipeOpened: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
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
                onActionClick = { onRemoveFavorite(recipe.id) },
                actionContentDescription = stringResource(R.string.button_remove_favorite),
                actionIcon = Icons.Default.Delete,
                actionTint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if ((index + 1) % nativeEveryN == 0) {
                NativeAdCard(
                    adManager = adManager,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
