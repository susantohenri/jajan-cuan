package com.henrisusanto.vitmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.henrisusanto.vitmate.data.model.UnlockFeature

class Converters {
    @TypeConverter
    fun fromUnlockFeature(feature: UnlockFeature): String = feature.name

    @TypeConverter
    fun toUnlockFeature(value: String): UnlockFeature = UnlockFeature.valueOf(value)
}

@Database(
    entities = [
        RecipeIndexEntity::class,
        RecipeDetailEntity::class,
        FavoriteEntity::class,
        UnlockEntity::class,
        CalcStateEntity::class,
        ActiveTimerEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JajanCuanDatabase : RoomDatabase() {
    abstract fun recipeIndexDao(): RecipeIndexDao
    abstract fun recipeDetailDao(): RecipeDetailDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun unlockDao(): UnlockDao
    abstract fun calcStateDao(): CalcStateDao
    abstract fun activeTimerDao(): ActiveTimerDao

    companion object {
        private const val DATABASE_NAME = "jajan_cuan.db"

        @Volatile
        private var instance: JajanCuanDatabase? = null

        fun getInstance(context: Context): JajanCuanDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JajanCuanDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
