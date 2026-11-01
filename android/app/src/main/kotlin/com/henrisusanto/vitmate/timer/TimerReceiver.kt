package com.henrisusanto.vitmate.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.henrisusanto.vitmate.JajanCuanApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Dipanggil AlarmManager saat timer berbunyi: hapus baris active_timers lalu
 * tampilkan notifikasi yang membuka resep terkait.
 */
class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMER_FIRED) return
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID)?.takeIf { it.isNotBlank() } ?: return

        val application = context.applicationContext as? JajanCuanApplication ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val timer = application.container.timerRepository.consume(timerId)
                if (timer != null) application.container.timerNotifier.notify(timer)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TIMER_FIRED = "com.henrisusanto.vitmate.action.TIMER_FIRED"
        const val EXTRA_TIMER_ID = "com.henrisusanto.vitmate.extra.TIMER_ID"
    }
}
