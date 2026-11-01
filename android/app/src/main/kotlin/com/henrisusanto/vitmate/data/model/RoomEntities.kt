package com.henrisusanto.vitmate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.henrisusanto.vitmate.util.AnalyticsLogger

@Entity(tableName = "recipe_index")
data class RecipeIndex(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val category: String,
    val tagsText: String,
    val ingredientsText: String,
    val cover: String?,
    val thumb: String?,
    val estCapital: Int,
    val portions: Int,
    val isViral: Boolean,
    val orderIdx: Int
)

@Entity(tableName = "recipe_detail")
data class RecipeDetail(
    @PrimaryKey val id: String,
    val json: String?,
    val fetchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class Favorite(
    val id: String,
    val favoritedAt: Long = System.currentTimeMillis(),
    val title: String,
    val category: String,
    val tagsText: String,
    val ingredientsText: String,
    val estCapital: Int,
    val portions: Int,
    val thumbLocalPath: String?,
    val coverLocalPath: String?,
    val detailJson: String
)

@Entity(tableName = "unlocks")
data class Unlock(
    val recipeId: String,
    val feature: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calc_state")
data class CalcState(
    val recipeId: String,
    val pricesJson: String,
    val portions: Int,
    val marginMode: String,
    val marginValue: Int
)

@Entity(tableName = "active_timers")
data class ActiveTimer(
    val id: String = java.util.UUID.randomUUID().toString()
)