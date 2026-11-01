package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.Flow

/**
 * Sumber kebenaran status buka fitur per resep.
 *
 * Fitur hanya [UnlockFeature.CALCULATOR] dan [UnlockFeature.PROMO]. Bila
 * Remote Config mematikan iklan, seluruh fitur dianggap terbuka tanpa menulis
 * baris permanen.
 */
interface UnlockCatalog {
    /** Fitur yang sedang terbuka untuk [recipeId], termasuk buka-sementara sesi. */
    fun observeUnlockedFeatures(recipeId: String): Flow<Set<UnlockFeature>>

    suspend fun isUnlocked(recipeId: String, feature: UnlockFeature): Boolean

    /**
     * Menyimpan buka permanen. HANYA boleh dipanggil dari callback reward
     * OnUserEarnedRewardListener.
     */
    suspend fun persistUnlock(recipeId: String, feature: UnlockFeature)

    /** Membuka fitur hanya di memori untuk sesi aplikasi berjalan. */
    fun unlockForSession(recipeId: String, feature: UnlockFeature)
}
