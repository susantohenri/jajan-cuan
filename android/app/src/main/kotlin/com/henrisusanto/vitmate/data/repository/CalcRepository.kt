package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.database.CalcStateDao
import com.henrisusanto.vitmate.data.model.CalcState

class CalcRepository(private val db: AppDatabase) {
    private val dao: CalcStateDao = db.calcStateDao()

    fun get(recipeId: String): CalcState? = dao.getByRecipeId(recipeId)

    fun save(calcState: CalcState) {
        dao.insert(calcState)
    }
}