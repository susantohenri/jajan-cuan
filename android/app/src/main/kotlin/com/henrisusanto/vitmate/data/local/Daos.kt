package com.henrisusanto.vitmate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.Flow

/**
 * Escape karakter khusus LIKE agar pencarian literal. Backslash di-escape
 * lebih dulu supaya escape berikutnya tidak dianggap bagian dari pola.
 */
internal fun escapeLikePattern(raw: String): String = buildString(raw.length) {
    raw.forEach { character ->
        when (character) {
            '\\', '%', '_' -> {
                append('\\')
                append(character)
            }
            else -> append(character)
        }
    }
}

@Dao
interface RecipeIndexDao {
    @Query("SELECT COUNT(*) FROM recipe_index")
    suspend fun count(): Int

    @Query("SELECT * FROM recipe_index WHERE id = :recipeId")
    suspend fun get(recipeId: String): RecipeIndexEntity?

    @Query("SELECT * FROM recipe_index WHERE id = :recipeId")
    fun observe(recipeId: String): Flow<RecipeIndexEntity?>

    @Query(
        """
        SELECT * FROM recipe_index
        WHERE (:category IS NULL OR category = :category)
          AND (
            :escapedQuery = ''
            OR title LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR ingredientsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR tagsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR category LIKE '%' || :escapedQuery || '%' ESCAPE '\'
          )
        ORDER BY orderIdx ASC, id ASC
        LIMIT :limit
        """
    )
    fun observeList(
        category: String?,
        escapedQuery: String,
        limit: Int
    ): Flow<List<RecipeIndexEntity>>

    /**
     * Kandidat untuk urutan acak: sama seperti [observeList] tetapi tanpa
     * `LIMIT` dan memakai `ORDER BY orderIdx, id` biasa agar index SQLite
     * tetap terpakai.
     *
     * F-013: pengacakan deterministik dilakukan di repository (Kotlin), bukan
     * lewat ekspresi `ORDER BY` aritmetika yang mematikan index. Pemanggil
     * memotong hasil setelah diurutkan sehingga paginasi tetap stabil.
     */
    @Query(
        """
        SELECT * FROM recipe_index
        WHERE (:category IS NULL OR category = :category)
          AND (
            :escapedQuery = ''
            OR title LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR ingredientsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR tagsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR category LIKE '%' || :escapedQuery || '%' ESCAPE '\'
          )
        ORDER BY orderIdx ASC, id ASC
        """
    )
    fun observeListShuffled(
        category: String?,
        escapedQuery: String
    ): Flow<List<RecipeIndexEntity>>

    @Query("SELECT DISTINCT category FROM recipe_index ORDER BY category ASC")
    fun observeCategories(): Flow<List<String>>

    @Query(
        """
        SELECT COUNT(*) FROM recipe_index
        WHERE (:category IS NULL OR category = :category)
          AND (
            :escapedQuery = ''
            OR title LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR ingredientsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR tagsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
            OR category LIKE '%' || :escapedQuery || '%' ESCAPE '\'
          )
        """
    )
    suspend fun countFiltered(category: String?, escapedQuery: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeIndexEntity>)

    @Query("DELETE FROM recipe_index")
    suspend fun clear()
}

@Dao
interface RecipeDetailDao {
    @Query("SELECT * FROM recipe_detail WHERE id = :recipeId")
    suspend fun get(recipeId: String): RecipeDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(detail: RecipeDetailEntity)

    @Query("DELETE FROM recipe_detail WHERE id = :recipeId")
    suspend fun delete(recipeId: String)

    @Query("DELETE FROM recipe_detail")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM recipe_detail")
    suspend fun count(): Int

    /**
     * Menyisakan [limit] detail terbaru. Dipanggil setelah setiap penyimpanan
     * detail agar cache non-favorit tidak tumbuh tanpa batas.
     */
    @Query(
        """
        DELETE FROM recipe_detail
        WHERE id NOT IN (
            SELECT id FROM recipe_detail
            ORDER BY fetchedAt DESC
            LIMIT :limit
        )
        """
    )
    suspend fun pruneTo(limit: Int)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY favoritedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query(
        """
        SELECT * FROM favorites
        WHERE :escapedQuery = ''
           OR title LIKE '%' || :escapedQuery || '%' ESCAPE '\'
           OR ingredientsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
           OR tagsText LIKE '%' || :escapedQuery || '%' ESCAPE '\'
           OR category LIKE '%' || :escapedQuery || '%' ESCAPE '\'
        ORDER BY favoritedAt DESC
        """
    )
    fun observeFiltered(escapedQuery: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY favoritedAt DESC")
    suspend fun getAll(): List<FavoriteEntity>

    @Query("SELECT id FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT id FROM favorites")
    suspend fun getIds(): List<String>

    @Query("SELECT * FROM favorites WHERE id = :recipeId")
    suspend fun get(recipeId: String): FavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :recipeId)")
    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :recipeId)")
    suspend fun isFavorite(recipeId: String): Boolean

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :recipeId")
    suspend fun delete(recipeId: String): Int
}

@Dao
interface UnlockDao {
    /**
     * Idempoten: unlock yang sudah ada tidak ditimpa, sehingga waktu unlock
     * pertama tetap tercatat.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(unlock: UnlockEntity): Long

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM unlocks
            WHERE recipeId = :recipeId AND feature = :feature
        )
        """
    )
    suspend fun isUnlocked(recipeId: String, feature: UnlockFeature): Boolean

    @Query("SELECT * FROM unlocks WHERE recipeId = :recipeId")
    fun observeForRecipe(recipeId: String): Flow<List<UnlockEntity>>

    @Query("SELECT * FROM unlocks WHERE recipeId = :recipeId AND feature = :feature")
    suspend fun get(recipeId: String, feature: UnlockFeature): UnlockEntity?

    @Query("SELECT COUNT(*) FROM unlocks")
    suspend fun count(): Int
}

@Dao
interface CalcStateDao {
    @Query("SELECT * FROM calc_state WHERE recipeId = :recipeId")
    suspend fun get(recipeId: String): CalcStateEntity?

    @Query("SELECT * FROM calc_state WHERE recipeId = :recipeId")
    fun observe(recipeId: String): Flow<CalcStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: CalcStateEntity)

    @Query("DELETE FROM calc_state WHERE recipeId = :recipeId")
    suspend fun delete(recipeId: String)

    /**
     * Housekeeping: hapus entri [calc_state] yang [recipeId]-nya tidak lagi
     * ada di [recipe_index] maupun [favorites]. Penggunaan biasa adalah
     * pembersihan berkala (mis. saat sync) untuk mencegah kebocoran memori
     * storage dari data kalkulator lama.
     */
    @Query(
        "DELETE FROM calc_state " +
        "WHERE recipeId NOT IN (SELECT id FROM recipe_index) " +
        "  AND recipeId NOT IN (SELECT id FROM favorites)"
    )
    suspend fun deleteOrphaned()
}

@Dao
interface ActiveTimerDao {
    @Query("SELECT * FROM active_timers ORDER BY endAtEpochMillis ASC")
    fun observeAll(): Flow<List<ActiveTimerEntity>>

    @Query("SELECT * FROM active_timers ORDER BY endAtEpochMillis ASC")
    suspend fun getAll(): List<ActiveTimerEntity>

    @Query("SELECT * FROM active_timers WHERE id = :timerId")
    suspend fun get(timerId: String): ActiveTimerEntity?

    @Query("SELECT MAX(notificationId) FROM active_timers")
    suspend fun maxNotificationId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(timer: ActiveTimerEntity)

    @Query("DELETE FROM active_timers WHERE id = :timerId")
    suspend fun delete(timerId: String)

    @Query("DELETE FROM active_timers WHERE endAtEpochMillis <= :nowMillis")
    suspend fun deleteExpired(nowMillis: Long)
}
