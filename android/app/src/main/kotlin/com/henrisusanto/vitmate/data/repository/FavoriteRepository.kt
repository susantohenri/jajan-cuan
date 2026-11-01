package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.database.FavoriteDao
import com.henrisusanto.vitmate.data.model.Favorite
import com.henrisusanto.vitmate.util.AnalyticsLogger

class FavoriteRepository(private val db: AppDatabase) {
    private val dao: FavoriteDao = db.favoriteDao()

    fun getAll(): List<Favorite> = dao.getAll()

    fun getById(id: String): Favorite? = dao.getById(id)

    fun search(query: String): List<Favorite> = dao.search(query)

    fun toggleFavorite(favorite: Favorite?): Boolean {
        if (favorite != null) {
            dao.deleteById(favorite.id)
            AnalyticsLogger.logRecipeRemoveFavorite(favorite.id)
            return false
        }
        return true
    }
}