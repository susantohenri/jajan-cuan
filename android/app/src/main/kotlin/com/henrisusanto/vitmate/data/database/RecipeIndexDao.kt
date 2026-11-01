package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.RecipeIndex

@Dao
interface RecipeIndexDao {
    @Query("SELECT * FROM recipe_index ORDER BY is_viral DESC, orderIdx ASC LIMIT :limit")
    fun getAll(limit: Int): List<RecipeIndex>

    @Query("SELECT * FROM recipe_index WHERE id = :id")
    fun getById(id: String): RecipeIndex?

    @Query("SELECT * FROM recipe_index WHERE category = :category")
    fun getByCategory(category: String): List<RecipeIndex>

    @Query("SELECT * FROM recipe_index WHERE is_viral = 1 ORDER BY title ASC")
    fun getViralRecipes(): List<RecipeIndex>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg recipes: RecipeIndex)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipes: List<RecipeIndex>)
}