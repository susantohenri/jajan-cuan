package com.henrisusanto.vitmate.ui.gate

import android.app.Activity
import com.henrisusanto.vitmate.ads.RewardedAdSession
import com.henrisusanto.vitmate.ads.RewardedAdState
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.data.repository.UnlockCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * State machine gate Rewarded untuk Kalkulator dan Promo pada satu resep.
 *
 * Satu [RewardedAdSession] dibagi dua fitur sehingga hanya satu iklan dimuat
 * pada satu waktu. Baris `unlocks` hanya ditulis dari callback reward; timeout
 * atau kegagalan setelah retry hanya membuka fitur di memori (fallback).
 */
class FeatureGateCoordinator(
    private val recipeId: String,
    private val unlocks: UnlockCatalog,
    private val adSession: RewardedAdSession,
    private val analytics: AnalyticsLogger,
    private val fallbackEnabled: () -> Boolean,
    private val scope: CoroutineScope,
    private val onFallback: () -> Unit = {}
) {
    private val availableFeatures = MutableStateFlow<Set<UnlockFeature>>(emptySet())
    private val dialogFeature = MutableStateFlow<UnlockFeature?>(null)
    private val _states = MutableStateFlow<Map<UnlockFeature, FeatureGateUiState>>(emptyMap())
    val states: StateFlow<Map<UnlockFeature, FeatureGateUiState>> = _states.asStateFlow()

    private var pendingFeature: UnlockFeature? = null
    private var fallbackHandled = false
    private var destroyed = false

    private data class Snapshot(
        val unlocked: Set<UnlockFeature>,
        val adState: RewardedAdState,
        val available: Set<UnlockFeature>
    )

    private var latestSnapshot = Snapshot(
        unlocked = emptySet(),
        adState = adSession.state.value,
        available = emptySet()
    )

    init {
        adSession.setRewardListener { onRewardEarned() }
        scope.launch {
            combine(
                unlocks.observeUnlockedFeatures(recipeId),
                adSession.state,
                availableFeatures
            ) { unlocked, adState, available ->
                Snapshot(unlocked, adState, available)
            }.collect { snapshot ->
                if (destroyed) return@collect
                latestSnapshot = snapshot
                reactToAdState(snapshot)
                _states.value = buildStates(snapshot)
            }
        }
        scope.launch {
            dialogFeature.collect {
                if (destroyed) return@collect
                _states.value = buildStates(latestSnapshot)
            }
        }
    }

    /**
     * Dipanggil setelah detail diketahui. Promo tidak dimasukkan bila resep
     * tidak punya foto promo, sehingga tidak ada gate kosong.
     */
    fun updateAvailableFeatures(features: Set<UnlockFeature>) {
        if (destroyed) return
        availableFeatures.value = features
        dialogFeature.value = dialogFeature.value?.takeIf { it in features }
    }

    fun onGateClick(feature: UnlockFeature) {
        if (destroyed) return
        if (_states.value[feature]?.status != FeatureGateStatus.READY) return
        dialogFeature.value = feature
    }

    fun dismissDialog() {
        if (destroyed) return
        dialogFeature.value = null
    }

    fun confirmDialog(activity: Activity) {
        if (destroyed) return
        val feature = dialogFeature.value ?: return
        dialogFeature.value = null
        pendingFeature = feature
        adSession.show(activity)
    }

    fun destroy() {
        destroyed = true
        adSession.setRewardListener {}
    }

    private fun onRewardEarned() {
        val feature = pendingFeature ?: return
        pendingFeature = null
        scope.launch {
            val persisted = runCatching {
                unlocks.persistUnlock(recipeId, feature)
            }.isSuccess
            if (persisted) {
                analytics.rewardedUnlock(feature.name, recipeId, viaFallback = false)
            }
        }
    }

    private fun reactToAdState(snapshot: Snapshot) {
        val locked = snapshot.available - snapshot.unlocked
        when (snapshot.adState) {
            is RewardedAdState.Idle -> {
                fallbackHandled = false
                // Iklan ditutup tanpa reward: jangan simpan fitur tertunda.
                pendingFeature = null
                if (locked.isNotEmpty()) adSession.preload()
            }
            is RewardedAdState.TimedOut, is RewardedAdState.Failed -> {
                if (!fallbackHandled) {
                    fallbackHandled = true
                    pendingFeature = null
                    if (fallbackEnabled() && locked.isNotEmpty()) {
                        fallbackUnlock(locked)
                    }
                }
            }
            else -> fallbackHandled = false
        }
    }

    private fun fallbackUnlock(locked: Set<UnlockFeature>) {
        locked.forEach { feature ->
            unlocks.unlockForSession(recipeId, feature)
            analytics.rewardedUnlock(feature.name, recipeId, viaFallback = true)
        }
        onFallback()
    }

    private fun buildStates(snapshot: Snapshot): Map<UnlockFeature, FeatureGateUiState> {
        if (snapshot.available.isEmpty()) return emptyMap()
        val dialog = dialogFeature.value
        return snapshot.available.associateWith { feature ->
            val status = when {
                feature in snapshot.unlocked -> FeatureGateStatus.OPEN
                snapshot.adState is RewardedAdState.Ready -> FeatureGateStatus.READY
                else -> FeatureGateStatus.PREPARING
            }
            FeatureGateUiState(
                status = status,
                dialogVisible = status == FeatureGateStatus.READY && dialog == feature
            )
        }
    }
}
