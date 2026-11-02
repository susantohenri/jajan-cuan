package com.henrisusanto.vitmate.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{recipeId}") {
        fun createRoute(recipeId: String) = "detail/${Uri.encode(recipeId)}"
    }
}
