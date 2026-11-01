package com.henrisusanto.vitmate.domain

import java.util.Locale

/**
 * Perhitungan tampilan timer. Sisa waktu SELALU dihitung dari waktu selesai
 * absolut, bukan dari hitung mundur lokal, sehingga tetap benar setelah app
 * di-background atau proses dibuka lagi.
 */
object TimerCountdown {

    /** Detik tersisa, dibulatkan ke atas agar 00:00 hanya muncul tepat di akhir. */
    fun remainingSeconds(endAtEpochMillis: Long, nowMillis: Long): Long {
        val remainingMillis = endAtEpochMillis - nowMillis
        if (remainingMillis <= 0L) return 0L
        return (remainingMillis + MILLIS_PER_SECOND - 1L) / MILLIS_PER_SECOND
    }

    /** Format mm:ss, atau h:mm:ss bila satu jam atau lebih. */
    fun format(remainingSeconds: Long): String {
        val total = remainingSeconds.coerceAtLeast(0L)
        val hours = total / SECONDS_PER_HOUR
        val minutes = (total % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = total % SECONDS_PER_MINUTE
        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 3_600L
}
