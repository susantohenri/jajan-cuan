package com.henrisusanto.vitmate.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.henrisusanto.vitmate.data.model.RecipeIndex
import com.henrisusanto.vitmate.data.model.RecipeDetail
import com.henrisusanto.vitmate.data.model.Favorite
import com.henrisusanto.vitmate.data.model.Unlock
import com.henrisusanto.vitmate.data.model.CalcState
import com.henrisusanto.vitmate.data.model.ActiveTimer

@Database(
    entities = [
        RecipeIndex::class,
        RecipeDetail::class,
        Favorite::class,
        Unlock::class,
        CalcState::class,
        ActiveTimer::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeIndexDao(): RecipeIndexDao
    abstract fun recipeDetailDao(): RecipeDetailDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun unlockDao(): UnlockDao
    abstract fun calcStateDao(): CalcStateDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jajan_cuan.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}