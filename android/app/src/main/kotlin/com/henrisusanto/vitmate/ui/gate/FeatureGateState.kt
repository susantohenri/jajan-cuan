package com.henrisusanto.vitmate.ui.gate

/** Tiga status visual FeatureGate di layar Detail. */
enum class FeatureGateStatus {
    /** Fitur sudah terbuka; konten ditampilkan. */
    OPEN,

    /** Terkunci dan iklan belum siap; tombol nonaktif + indikator. */
    PREPARING,

    /** Terkunci dan iklan siap ditonton; tombol aktif. */
    READY
}

data class FeatureGateUiState(
    val status: FeatureGateStatus = FeatureGateStatus.PREPARING,
    val dialogVisible: Boolean = false
)
