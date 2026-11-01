package com.henrisusanto.vitmate.data.testutil

import com.henrisusanto.vitmate.data.sync.SyncStateStore

class FakeSyncStateStore : SyncStateStore {
    var version: String? = null
    var lastCheckAt: Long = 0L

    override fun metaVersion(): String? = version

    override fun lastMetaCheckAtMillis(): Long = lastCheckAt

    override fun saveMetaVersion(version: String) {
        this.version = version
    }

    override fun saveLastMetaCheckAt(millis: Long) {
        lastCheckAt = millis
    }
}
