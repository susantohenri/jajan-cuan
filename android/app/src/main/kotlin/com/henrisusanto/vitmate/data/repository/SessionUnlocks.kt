package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.model.UnlockFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SessionUnlockKey(
    val recipeId: String,
    val feature: UnlockFeature
)

/**
 * Unlock sementara yang hanya hidup selama proses aplikasi berjalan.
 *
 * Dipakai saat iklan timeout/gagal dan fallback diaktifkan. Tidak pernah
 * menyentuh Room, sehingga setelah aplikasi dimatikan fitur terkunci lagi.
 */
class SessionUnlocks {
    private val _unlocked = MutableStateFlow<Set<SessionUnlockKey>>(emptySet())
    val unlocked: StateFlow<Set<SessionUnlockKey>> = _unlocked.asStateFlow()

    fun unlock(recipeId: String, feature: UnlockFeature) {
        _unlocked.update { current -> current + SessionUnlockKey(recipeId, feature) }
    }

    fun isUnlocked(recipeId: String, feature: UnlockFeature): Boolean =
        SessionUnlockKey(recipeId, feature) in _unlocked.value
}
