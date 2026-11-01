package com.henrisusanto.vitmate.data.repository

import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.database.ActiveTimerDao
import com.henrisusanto.vitmate.data.model.ActiveTimer

class TimerRepository(private val db: AppDatabase) {
    private val dao: ActiveTimerDao = db.activeTimerDao()

    fun startTimer(timerId: String) {
        dao.insert(ActiveTimer(id = timerId))
    }

    fun clearAll() {
        dao.clearAll()
    }
}