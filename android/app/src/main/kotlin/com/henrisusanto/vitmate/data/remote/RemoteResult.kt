package com.henrisusanto.vitmate.data.remote

/**
 * Hasil akses jaringan yang selalu bertipe. Tidak ada exception yang bocor ke
 * pemanggil; kegagalan jaringan/HTTP/parse dilaporkan sebagai [Failure].
 */
sealed interface RemoteResult<out T> {
    data class Success<T>(val value: T) : RemoteResult<T>
    data object NoData : RemoteResult<Nothing>
    data class Failure(
        val kind: FailureKind,
        val message: String,
        val cause: Throwable? = null
    ) : RemoteResult<Nothing>
}

enum class FailureKind {
    NETWORK,
    HTTP,
    PARSE
}
