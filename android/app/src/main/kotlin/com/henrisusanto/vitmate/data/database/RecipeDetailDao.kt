package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.RecipeDetail

@Dao
interface RecipeDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(detail: RecipeDetail)

    @Query("SELECT * FROM recipe_detail WHERE id = :id")
    fun getById(id: String): RecipeDetail?

    @Query("DELETE FROM recipe_detail")
    fun clearAll()

    @Query("DELETE FROM recipe_detail WHERE fetchedAt < :cutoff")
    fun deleteOlderThan(cutoff: Long)
}