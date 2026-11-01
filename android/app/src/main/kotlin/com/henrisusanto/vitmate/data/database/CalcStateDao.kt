package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.CalcState

@Dao
interface CalcStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg calcStates: CalcState)

    @Query("SELECT * FROM calc_state WHERE recipeId = :recipeId")
    fun getByRecipeId(recipeId: String): CalcState?
}