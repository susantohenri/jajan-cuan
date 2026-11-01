package com.henrisusanto.vitmate.data.sync

import android.content.Context
import androidx.core.content.edit

/**
 * Menyimpan versi index yang sudah diterapkan dan waktu cek /meta terakhir.
 * Data ini bukan konten katalog, jadi tidak perlu menjadi tabel Room.
 */
interface SyncStateStore {
    fun metaVersion(): String?
    fun lastMetaCheckAtMillis(): Long
    fun saveMetaVersion(version: String)
    fun saveLastMetaCheckAt(millis: Long)
}

class SharedPreferencesSyncStateStore(context: Context) : SyncStateStore {
    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun metaVersion(): String? = preferences.getString(KEY_META_VERSION, null)

    override fun lastMetaCheckAtMillis(): Long =
        preferences.getLong(KEY_LAST_META_CHECK, 0L)

    override fun saveMetaVersion(version: String) {
        preferences.edit { putString(KEY_META_VERSION, version) }
    }

    override fun saveLastMetaCheckAt(millis: Long) {
        preferences.edit { putLong(KEY_LAST_META_CHECK, millis) }
    }

    private companion object {
        const val PREFERENCES_NAME = "sync_state"
        const val KEY_META_VERSION = "meta_version"
        const val KEY_LAST_META_CHECK = "last_meta_check_at"
    }
}
