package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.Favorite

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY favoritedAt DESC")
    fun getAll(): List<Favorite>

    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getById(id: String): Favorite?

    @Query("SELECT * FROM favorites WHERE title LIKE %:query% OR ingredientsText LIKE %:query% OR tagsText LIKE %:query% OR category LIKE %:query%")
    fun search(query: String): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg favorites: Favorite)

    @Query("DELETE FROM favorites WHERE id = :id")
    fun deleteById(id: String)
}