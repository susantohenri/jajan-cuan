package com.henrisusanto.vitmate.ui.util

import java.text.NumberFormat
import java.util.Locale

private val indonesiaLocale = Locale.forLanguageTag("id-ID")

/**
 * Format Rupiah tanpa desimal dengan pemisah ribuan id-ID, mis. 12500 -> "Rp 12.500".
 * Dipakai kartu Home/Favorit dan (S7) kalkulator HPP.
 */
fun formatRupiah(amount: Int): String = formatRupiah(amount.toLong())

fun formatRupiah(amount: Long): String =
    "Rp " + NumberFormat.getIntegerInstance(indonesiaLocale).format(amount)

/** Pembulatan mengikuti web (`Math.round`) agar tampilan identik. */
fun formatRupiah(amount: Double): String {
    if (!amount.isFinite()) return formatRupiah(0L)
    return formatRupiah(Math.round(amount))
}
