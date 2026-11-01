package com.henrisusanto.vitmate.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.henrisusanto.vitmate.data.model.ActiveTimer

@Dao
interface ActiveTimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg timers: ActiveTimer)

    @Query("DELETE FROM active_timers")
    fun clearAll()
}