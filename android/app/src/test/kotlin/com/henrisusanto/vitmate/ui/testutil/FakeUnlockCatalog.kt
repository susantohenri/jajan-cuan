package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.repository.UnlockCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Unlock palsu untuk unit test gate. [unlocked] meniru hasil gabungan baris
 * permanen dan unlock sesi, sehingga test bisa mengatur status tanpa Room.
 */
class FakeUnlockCatalog : UnlockCatalog {

    val unlocked = MutableStateFlow<Set<UnlockFeature>>(emptySet())
    val persisted = mutableListOf<Pair<String, UnlockFeature>>()
    val sessionUnlocks = mutableListOf<Pair<String, UnlockFeature>>()
    var failPersist: Boolean = false

    override fun observeUnlockedFeatures(recipeId: String): Flow<Set<UnlockFeature>> =
        unlocked

    override suspend fun isUnlocked(recipeId: String, feature: UnlockFeature): Boolean =
        feature in unlocked.value

    override suspend fun persistUnlock(recipeId: String, feature: UnlockFeature) {
        if (failPersist) error("gagal menyimpan unlock")
        persisted += recipeId to feature
        unlocked.update { it + feature }
    }

    override fun unlockForSession(recipeId: String, feature: UnlockFeature) {
        sessionUnlocks += recipeId to feature
        unlocked.update { it + feature }
    }
}
