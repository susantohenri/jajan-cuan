package com.henrisusanto.vitmate.domain

import kotlin.math.roundToInt

/**
 * Parser durasi pada langkah memasak.
 *
 * Mendukung menit/jam/detik, rentang "10-15 menit" (memakai nilai terbesar),
 * dan gabungan seperti "1 jam 30 menit". Mengembalikan null bila langkah tidak
 * menyebut durasi.
 */
object CookingDurationParser {
    private const val SECONDS_PER_HOUR = 3_600.0
    private const val SECONDS_PER_MINUTE = 60.0

    private val rangePattern = Regex(
        """(\d+(?:[.,]\d+)?)\s*(?:-|–|—|sampai|s/d)\s*(\d+(?:[.,]\d+)?)\s*(jam|menit|detik)\b""",
        RegexOption.IGNORE_CASE
    )
    private val singlePattern = Regex(
        """(\d+(?:[.,]\d+)?)\s*(jam|menit|detik)\b""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String?): Int? {
        if (text.isNullOrBlank()) return null

        var remaining: String = text
        var totalSeconds = 0.0

        rangePattern.findAll(remaining).toList().forEach { match ->
            val first = match.groupValues[1].toDecimalOrNull() ?: return@forEach
            val second = match.groupValues[2].toDecimalOrNull() ?: return@forEach
            val unit = match.groupValues[3]
            totalSeconds += maxOf(first, second) * secondsFor(unit)
            remaining = remaining.replaceRange(
                match.range,
                " ".repeat(match.value.length)
            )
        }

        singlePattern.findAll(remaining).forEach { match ->
            val value = match.groupValues[1].toDecimalOrNull() ?: return@forEach
            totalSeconds += value * secondsFor(match.groupValues[2])
        }

        if (totalSeconds <= 0.0) return null
        return totalSeconds
            .roundToInt()
            .coerceAtMost(Int.MAX_VALUE)
    }

    private fun secondsFor(unit: String): Double = when (unit.lowercase()) {
        "jam" -> SECONDS_PER_HOUR
        "menit" -> SECONDS_PER_MINUTE
        else -> 1.0
    }

    private fun String.toDecimalOrNull(): Double? =
        replace(',', '.').toDoubleOrNull()
}
