package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.database.UnlockDao
import com.henrisusanto.vitmate.data.model.Unlock

class UnlockRepository(private val db: AppDatabase) {
    private val dao: UnlockDao = db.unlockDao()

    fun isUnlocked(recipeId: String, feature: String): Boolean {
        return dao.getByRecipeAndFeature(recipeId, feature) != null
    }

    fun unlock(recipeId: String, feature: String) {
        val existing = dao.getByRecipeAndFeature(recipeId, feature)
        if (existing == null) {
            dao.insert(Unlock(recipeId = recipeId, feature = feature))
        }
    }
}