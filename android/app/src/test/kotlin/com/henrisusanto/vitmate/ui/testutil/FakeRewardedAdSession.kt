package com.henrisusanto.vitmate.ui.testutil

import android.app.Activity
import com.henrisusanto.vitmate.ads.RewardItem
import com.henrisusanto.vitmate.ads.RewardedAdSession
import com.henrisusanto.vitmate.ads.RewardedAdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sesi rewarded palsu tanpa SDK iklan. Test mengatur [state] dan memanggil
 * [earnReward] untuk meniru OnUserEarnedRewardListener.
 */
class FakeRewardedAdSession : RewardedAdSession {

    private val _state = MutableStateFlow<RewardedAdState>(RewardedAdState.Idle)
    override val state: StateFlow<RewardedAdState> = _state

    var preloadCalls: Int = 0
        private set
    var showCalls: Int = 0
        private set
    var destroyed: Boolean = false
        private set

    private var rewardListener: (RewardItem) -> Unit = {}

    override fun setRewardListener(listener: (RewardItem) -> Unit) {
        rewardListener = listener
    }

    override fun preload() {
        preloadCalls += 1
    }

    override fun show(activity: Activity) {
        showCalls += 1
    }

    override fun reset() {
        _state.value = RewardedAdState.Idle
    }

    override fun destroy() {
        destroyed = true
        rewardListener = {}
    }

    fun setState(state: RewardedAdState) {
        _state.value = state
    }

    fun earnReward() {
        rewardListener(RewardItem(type = "reward", amount = 1))
    }
}
