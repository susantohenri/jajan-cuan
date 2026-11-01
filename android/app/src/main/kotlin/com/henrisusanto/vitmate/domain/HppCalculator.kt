package com.henrisusanto.vitmate.domain

import kotlin.math.ceil

enum class HppMarginMode {
    PERCENT,
    NOMINAL
}

data class HppResult(
    val totalModal: Double,
    val portions: Int,
    val hppPerPortion: Double,
    val sellingPrice: Double,
    val roundedSellingPrice: Long,
    val profitPerPortion: Double,
    val totalProfit: Double
)

/**
 * Kalkulator HPP murni yang mengikuti `web/js/experience.js` (recalculate).
 *
 * Harga jual murni dipakai untuk laba agar hasil identik dengan web; harga
 * bulat Rp 500 hanya nilai tambahan untuk tampilan. Semua input dibersihkan
 * agar tidak pernah menghasilkan NaN/Infinity.
 */
object HppCalculator {
    const val DEFAULT_MARGIN_PERCENT = 100.0
    private const val ROUNDING_STEP = 500.0

    fun calculate(
        prices: List<Long>,
        portions: Int,
        marginMode: HppMarginMode = HppMarginMode.PERCENT,
        marginValue: Double = DEFAULT_MARGIN_PERCENT
    ): HppResult {
        val totalModal = prices.fold(0.0) { total, price ->
            total + price.coerceAtLeast(0L).toDouble()
        }
        val targetPortions = portions.coerceAtLeast(1)
        val hppPerPortion = totalModal / targetPortions
        val margin = marginValue
            .takeIf { it.isFinite() }
            ?.coerceAtLeast(0.0)
            ?: 0.0
        val sellingPrice = when (marginMode) {
            HppMarginMode.PERCENT -> hppPerPortion * (1.0 + margin / 100.0)
            HppMarginMode.NOMINAL -> hppPerPortion + margin
        }.finiteOrZero()
        val profitPerPortion = (sellingPrice - hppPerPortion).finiteOrZero()

        return HppResult(
            totalModal = totalModal.finiteOrZero(),
            portions = targetPortions,
            hppPerPortion = hppPerPortion.finiteOrZero(),
            sellingPrice = sellingPrice,
            roundedSellingPrice = roundUpToStep(sellingPrice),
            profitPerPortion = profitPerPortion,
            totalProfit = (profitPerPortion * targetPortions).finiteOrZero()
        )
    }

    private fun roundUpToStep(value: Double): Long {
        if (!value.isFinite() || value <= 0.0) return 0L
        val rounded = ceil(value / ROUNDING_STEP) * ROUNDING_STEP
        return when {
            !rounded.isFinite() -> Long.MAX_VALUE
            rounded >= Long.MAX_VALUE.toDouble() -> Long.MAX_VALUE
            else -> rounded.toLong()
        }
    }

    private fun Double.finiteOrZero(): Double = if (isFinite()) this else 0.0
}
