package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.database.RecipeIndexDao
import com.henrisusanto.vitmate.data.model.RecipeIndex
import com.henrisusanto.vitmate.util.AnalyticsLogger

class RecipeRepository(
    private val db: AppDatabase,
    private val rtdbService: RtdbApiService
) {
    private val dao: RecipeIndexDao = db.recipeIndexDao()

    fun getAll(limit: Int = 10): List<RecipeIndex> = dao.getAll(limit)

    fun getById(id: String): RecipeIndex? = dao.getById(id)

    fun getByCategory(category: String): List<RecipeIndex> = dao.getByCategory(category)

    fun getViralRecipes(): List<RecipeIndex> = dao.getViralRecipes()

    fun refreshFromServer() {
        // Pull data from RTDB /search_index and update local database
        // This would be called on pull-to-refresh or app foreground
        AnalyticsLogger.logEvent("recipe_search", analyticsLogger.bundle().apply {
            putInt("result_count", 0)
        })
    }
}