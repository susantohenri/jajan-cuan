package com.henrisusanto.vitmate.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.henrisusanto.vitmate.data.model.UnlockFeature

@Entity(
    tableName = "recipe_index",
    indices = [Index("category"), Index("orderIdx")]
)
data class RecipeIndexEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val category: String,
    val tagsText: String,
    val ingredientsText: String,
    val cover: String,
    val thumb: String?,
    val estCapital: Int,
    val portions: Int,
    val isViral: Boolean,
    val orderIdx: Int
)

@Entity(tableName = "recipe_detail")
data class RecipeDetailEntity(
    @PrimaryKey val id: String,
    val json: String,
    val fetchedAt: Long
)

@Entity(
    tableName = "favorites",
    indices = [Index("favoritedAt")]
)
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val favoritedAt: Long,
    val title: String,
    val category: String,
    val tagsText: String,
    val ingredientsText: String,
    val estCapital: Int,
    val portions: Int,
    val thumbLocalPath: String?,
    val coverLocalPath: String,
    val detailJson: String
)

@Entity(
    tableName = "unlocks",
    primaryKeys = ["recipeId", "feature"]
)
data class UnlockEntity(
    val recipeId: String,
    val feature: UnlockFeature,
    val unlockedAt: Long
)

@Entity(tableName = "calc_state")
data class CalcStateEntity(
    @PrimaryKey val recipeId: String,
    val pricesJson: String,
    val portions: Int,
    val marginMode: String,
    val marginValue: Double
)

@Entity(tableName = "active_timers")
data class ActiveTimerEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val recipeTitle: String,
    val label: String,
    val endAtEpochMillis: Long,
    val createdAt: Long,
    val notificationId: Int
)
