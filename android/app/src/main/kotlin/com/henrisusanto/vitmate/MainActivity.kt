package com.henrisusanto.vitmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.henrisusanto.vitmate.data.sync.SyncOutcome
import com.henrisusanto.vitmate.timer.TimerIntents
import com.henrisusanto.vitmate.ui.navigation.Screen
import com.henrisusanto.vitmate.ui.screens.DetailScreen
import com.henrisusanto.vitmate.ui.screens.DetailViewModel
import com.henrisusanto.vitmate.ui.screens.FavoritesScreen
import com.henrisusanto.vitmate.ui.screens.FavoritesViewModel
import com.henrisusanto.vitmate.ui.screens.HomeScreen
import com.henrisusanto.vitmate.ui.screens.HomeViewModel
import com.henrisusanto.vitmate.ui.screens.SettingsScreen
import com.henrisusanto.vitmate.ui.theme.JajanCuanTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    private val container: AppContainer
        get() = (application as JajanCuanApplication).container

    /** Diisi dari notifikasi timer; dikonsumsi setelah navigasi ke Detail. */
    private val pendingRecipeId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        pendingRecipeId.value = TimerIntents.recipeIdFrom(intent)

        setContent {
            JajanCuanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigationApp(
                        container = container,
                        pendingRecipeId = pendingRecipeId,
                        onPendingRecipeConsumed = {
                            pendingRecipeId.value = null
                            intent.removeExtra(TimerIntents.EXTRA_RECIPE_ID)
                        }
                    )
                }
            }
        }

        // UMP dulu; MobileAds baru diinisialisasi setelah canRequestAds() true.
        container.consentManager.gatherConsent(this) {
            container.adsInitializer.initializeIfAllowed()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        TimerIntents.recipeIdFrom(intent)?.let { pendingRecipeId.value = it }
    }
}

@Composable
fun MainNavigationApp(
    container: AppContainer,
    pendingRecipeId: StateFlow<String?>,
    onPendingRecipeConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val recipeIdToOpen by pendingRecipeId.collectAsStateWithLifecycle()

    LaunchedEffect(recipeIdToOpen) {
        val recipeId = recipeIdToOpen ?: return@LaunchedEffect
        navController.navigate(Screen.Detail.createRoute(recipeId)) {
            launchSingleTop = true
        }
        onPendingRecipeConsumed()
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, R.string.tab_home, Icons.Default.Home),
        BottomNavItem(Screen.Favorites.route, R.string.tab_favorites, Icons.Default.Favorite),
        BottomNavItem(Screen.Settings.route, R.string.tab_settings, Icons.Default.Settings)
    )

    // Bottom bar is hidden on Detail screen
    val shouldShowBottomBar = bottomNavItems.any { it.route == currentRoute }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route
            if (route != null) {
                val screen = route.substringBefore("/")
                container.analytics.screenView(screen)
                container.adManager.setCurrentScreen(screen)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(
                        repository = container.recipeRepository,
                        analytics = container.analytics,
                        pageSize = container.remoteConfigManager.values.value.pageSize,
                        refresh = { force ->
                            container.syncNow(force = force) !is SyncOutcome.Failed
                        }
                    )
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    adManager = container.adManager,
                    onNavigateToDetail = { recipeId ->
                        navController.navigate(Screen.Detail.createRoute(recipeId))
                    }
                )
            }
            composable(Screen.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel {
                    FavoritesViewModel(
                        repository = container.recipeRepository,
                        analytics = container.analytics
                    )
                }
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    adManager = container.adManager,
                    onNavigateToDetail = { recipeId ->
                        navController.navigate(Screen.Detail.createRoute(recipeId))
                    },
                    onExploreRecipes = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Detail.route) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()
                val detailViewModel: DetailViewModel = viewModel(key = recipeId) {
                    DetailViewModel(
                        recipeId = recipeId,
                        repository = container.recipeRepository,
                        timers = container.timerRepository,
                        analytics = container.analytics,
                        unlocks = container.unlockRepository,
                        rewardedSession = container.adManager.createRewardedController(),
                        promoActions = container.promoActions,
                        imageUrlResolver = container.imageUrlResolver,
                        fallbackUnlockEnabled = {
                            container.adManager.rewardedFallbackUnlockOnFailure()
                        }
                    )
                }
                DetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
