package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.Unlock

@Dao
interface UnlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg unlocks: Unlock)

    @Query("SELECT * FROM unlocks WHERE recipeId = :recipeId AND feature = :feature")
    fun getByRecipeAndFeature(recipeId: String, feature: String): Unlock?

    @Query("SELECT * FROM unlocks WHERE recipeId = :recipeId")
    fun getByRecipe(recipeId: String): List<Unlock>
}