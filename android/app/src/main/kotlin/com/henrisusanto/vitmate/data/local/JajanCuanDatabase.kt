package com.henrisusanto.vitmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

        /**
         * F-018: `favorites` menyimpan `slug` sendiri agar kartu favorit tidak
         * lagi memakai `id` sebagai slug. Baris lama diisi dari `recipe_index`
         * bila masih tersedia; bila tidak, string kosong dan repository
         * memakai `id` sebagai fallback.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE favorites ADD COLUMN slug TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    """
                    UPDATE favorites
                    SET slug = COALESCE(
                        (SELECT slug FROM recipe_index WHERE recipe_index.id = favorites.id),
                        ''
                    )
                    WHERE slug = ''
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var instance: JajanCuanDatabase? = null

        fun getInstance(context: Context): JajanCuanDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JajanCuanDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    // Rilis tanpa migrasi eksplisit tidak boleh crash di perangkat
                    // pengguna; fallback ini hanya terpakai bila tidak ada jalur
                    // migrasi yang cocok (mis. database korup / versi lebih baru).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
