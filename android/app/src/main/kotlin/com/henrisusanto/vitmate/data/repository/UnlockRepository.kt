package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.config.RemoteConfigValues
import com.henrisusanto.vitmate.data.local.UnlockDao
import com.henrisusanto.vitmate.data.local.UnlockEntity
import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Implementasi [UnlockCatalog] di atas tabel `unlocks` + [SessionUnlocks].
 *
 * Aturan buka:
 * `isAdsEnabled == false` ATAU `rewardedEnabled == false` ATAU ada baris
 * permanen ATAU ada unlock sesi. Flag RC dibaca dari StateFlow sehingga
 * perubahan konfigurasi langsung terlihat tanpa menulis apa pun.
 */
class UnlockRepository(
    private val dao: UnlockDao,
    private val sessionUnlocks: SessionUnlocks,
    private val config: StateFlow<RemoteConfigValues>,
    private val clock: () -> Long = System::currentTimeMillis
) : UnlockCatalog {

    override fun observeUnlockedFeatures(recipeId: String): Flow<Set<UnlockFeature>> =
        combine(
            dao.observeForRecipe(recipeId),
            sessionUnlocks.unlocked,
            config
        ) { rows, session, values ->
            if (values.featuresAreOpen) {
                UnlockFeature.entries.toSet()
            } else {
                buildSet {
                    rows.forEach { add(it.feature) }
                    session.filter { it.recipeId == recipeId }
                        .forEach { add(it.feature) }
                }
            }
        }.distinctUntilChanged()

    override suspend fun isUnlocked(recipeId: String, feature: UnlockFeature): Boolean {
        if (config.value.featuresAreOpen) return true
        if (dao.isUnlocked(recipeId, feature)) return true
        return sessionUnlocks.isUnlocked(recipeId, feature)
    }

    override suspend fun persistUnlock(recipeId: String, feature: UnlockFeature) {
        dao.insert(
            UnlockEntity(
                recipeId = recipeId,
                feature = feature,
                unlockedAt = clock()
            )
        )
    }

    override fun unlockForSession(recipeId: String, feature: UnlockFeature) {
        sessionUnlocks.unlock(recipeId, feature)
    }

    private val RemoteConfigValues.featuresAreOpen: Boolean
        get() = !isAdsEnabled || !rewardedEnabled
}
