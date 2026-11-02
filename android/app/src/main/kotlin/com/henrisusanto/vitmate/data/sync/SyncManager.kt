package com.henrisusanto.vitmate.data.sync

import androidx.room.withTransaction
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.local.RecipeIndexEntity
import com.henrisusanto.vitmate.data.remote.RecipeRemoteDataSource
import com.henrisusanto.vitmate.data.remote.RemoteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface SyncOutcome {
    data object Throttled : SyncOutcome
    data object UpToDate : SyncOutcome
    data class Updated(val indexCount: Int) : SyncOutcome
    data class Failed(val message: String) : SyncOutcome
}

/**
 * Online-first sync berbasis /meta.
 *
 * Index hanya diganti bila versi /meta berubah. Detail cache dikosongkan,
 * snapshot favorit dipertahankan (dan di-refresh di latar belakang), dan tabel
 * unlocks tidak pernah disentuh.
 */
class SyncManager(
    private val database: JajanCuanDatabase,
    private val remote: RecipeRemoteDataSource,
    private val stateStore: SyncStateStore,
    private val refreshFavorite: suspend (String) -> Unit,
    private val metaCheckIntervalHours: () -> Long,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val indexDao = database.recipeIndexDao()
    private val detailDao = database.recipeDetailDao()
    private val favoriteDao = database.favoriteDao()
    private val syncMutex = Mutex()

    suspend fun sync(force: Boolean = false): SyncOutcome = withContext(ioDispatcher) {
        syncMutex.withLock {
            val now = clock()
            if (!force && isThrottled(now)) {
                return@withLock SyncOutcome.Throttled
            }

            val metaResult = remote.fetchMeta()
            if (metaResult !is RemoteResult.Success) {
                return@withLock SyncOutcome.Failed(
                    message = when (metaResult) {
                        RemoteResult.NoData -> "Meta tidak tersedia."
                        is RemoteResult.Failure -> metaResult.message
                        is RemoteResult.Success -> ""
                    }
                )
            }

            val meta = metaResult.value
            if (meta.version.isBlank()) {
                return@withLock SyncOutcome.Failed("Meta tidak memiliki versi.")
            }

            val currentVersion = stateStore.metaVersion()
            if (currentVersion == meta.version && indexDao.count() > 0) {
                stateStore.saveLastMetaCheckAt(now)
                return@withLock SyncOutcome.UpToDate
            }

            val indexResult = remote.fetchSearchIndex()
            if (indexResult !is RemoteResult.Success) {
                return@withLock SyncOutcome.Failed(
                    message = when (indexResult) {
                        RemoteResult.NoData -> "Index tidak tersedia."
                        is RemoteResult.Failure -> indexResult.message
                        is RemoteResult.Success -> ""
                    }
                )
            }

            val entries = indexResult.value.entries
                .sortedBy { it.key }
                .mapIndexed { index, (id, entry) ->
                    RecipeIndexEntity(
                        id = id,
                        slug = entry.slug,
                        title = entry.title,
                        category = entry.category,
                        tagsText = entry.tagsText(),
                        ingredientsText = entry.ingredientsText,
                        cover = entry.cover,
                        thumb = entry.thumb,
                        estCapital = entry.estCapital,
                        portions = entry.portions,
                        isViral = entry.isViral,
                        orderIdx = index
                    )
                }

            if (entries.isEmpty()) {
                return@withLock SyncOutcome.Failed("Index kosong; cache lama dipertahankan.")
            }

            database.withTransaction {
                indexDao.clear()
                indexDao.insertAll(entries)
                detailDao.clear()
            }
            stateStore.saveMetaVersion(meta.version)
            // Timestamp baru dicatat setelah index benar-benar tersimpan, agar
            // kegagalan unduh/simpan tidak membuat sync berikutnya ter-throttle.
            stateStore.saveLastMetaCheckAt(now)

            favoriteDao.getIds().forEach { recipeId ->
                try {
                    refreshFavorite(recipeId)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    // Snapshot lama tetap dipakai bila refresh satu favorit gagal.
                }
            }

            SyncOutcome.Updated(entries.size)
        }
    }

    private fun isThrottled(now: Long): Boolean {
        val lastCheck = stateStore.lastMetaCheckAtMillis()
        if (lastCheck <= 0L) return false
        val intervalMillis = metaCheckIntervalHours().coerceAtLeast(1L) *
            MILLIS_PER_HOUR
        return now - lastCheck in 0 until intervalMillis
    }

    private fun com.henrisusanto.vitmate.data.remote.dto.SearchIndexEntryDto.tagsText(): String =
        tags.joinToString(" ")

    private companion object {
        const val MILLIS_PER_HOUR = 60L * 60L * 1000L
    }
}
