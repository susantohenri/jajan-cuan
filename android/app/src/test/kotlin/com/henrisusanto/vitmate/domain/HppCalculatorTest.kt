package com.henrisusanto.vitmate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden test memakai contoh numerik DATA_SCHEMA.md bagian d, ditambah kasus
 * tepi yang tidak boleh menghasilkan NaN/Infinity.
 */
class HppCalculatorTest {

    private val goldenPrices = listOf(
        9_000L, 3_000L, 3_000L, 2_000L, 500L,
        2_000L, 1_500L, 1_000L, 7_000L, 10_000L
    )

    @Test
    fun `golden mode persen 100 identik dengan web`() {
        val result = HppCalculator.calculate(
            prices = goldenPrices,
            portions = 25,
            marginMode = HppMarginMode.PERCENT,
            marginValue = 100.0
        )

        assertEquals(39_000.0, result.totalModal, DELTA)
        assertEquals(25, result.portions)
        assertEquals(1_560.0, result.hppPerPortion, DELTA)
        assertEquals(3_120.0, result.sellingPrice, DELTA)
        assertEquals(3_500L, result.roundedSellingPrice)
        assertEquals(1_560.0, result.profitPerPortion, DELTA)
        assertEquals(39_000.0, result.totalProfit, DELTA)
    }

    @Test
    fun `golden mode nominal laba 1000 per porsi`() {
        val result = HppCalculator.calculate(
            prices = goldenPrices,
            portions = 25,
            marginMode = HppMarginMode.NOMINAL,
            marginValue = 1_000.0
        )

        assertEquals(1_560.0, result.hppPerPortion, DELTA)
        assertEquals(2_560.0, result.sellingPrice, DELTA)
        assertEquals(3_000L, result.roundedSellingPrice)
        assertEquals(1_000.0, result.profitPerPortion, DELTA)
        assertEquals(25_000.0, result.totalProfit, DELTA)
    }

    @Test
    fun `porsi nol dianggap satu`() {
        val result = HppCalculator.calculate(
            prices = listOf(10_000L),
            portions = 0,
            marginMode = HppMarginMode.PERCENT,
            marginValue = 100.0
        )

        assertEquals(1, result.portions)
        assertEquals(10_000.0, result.hppPerPortion, DELTA)
        assertEquals(20_000.0, result.sellingPrice, DELTA)
        assertEquals(20_000L, result.roundedSellingPrice)
    }

    @Test
    fun `input kosong dan negatif dihitung nol`() {
        val result = HppCalculator.calculate(
            prices = emptyList(),
            portions = 5,
            marginMode = HppMarginMode.PERCENT,
            marginValue = 100.0
        )

        assertEquals(0.0, result.totalModal, DELTA)
        assertEquals(0.0, result.hppPerPortion, DELTA)
        assertEquals(0.0, result.sellingPrice, DELTA)
        assertEquals(0L, result.roundedSellingPrice)
        assertEquals(0.0, result.totalProfit, DELTA)

        val negative = HppCalculator.calculate(
            prices = listOf(-5_000L, 2_000L),
            portions = 2,
            marginMode = HppMarginMode.NOMINAL,
            marginValue = 0.0
        )
        assertEquals(2_000.0, negative.totalModal, DELTA)
    }

    @Test
    fun `margin kosong atau NaN tidak menghasilkan NaN`() {
        val nan = HppCalculator.calculate(
            prices = listOf(3_000L),
            portions = 3,
            marginMode = HppMarginMode.PERCENT,
            marginValue = Double.NaN
        )
        assertEquals(1_000.0, nan.hppPerPortion, DELTA)
        assertEquals(1_000.0, nan.sellingPrice, DELTA)
        assertFalse(nan.sellingPrice.isNaN())
        assertFalse(nan.sellingPrice.isInfinite())

        val infinite = HppCalculator.calculate(
            prices = listOf(3_000L),
            portions = 3,
            marginMode = HppMarginMode.NOMINAL,
            marginValue = Double.POSITIVE_INFINITY
        )
        assertFalse(infinite.sellingPrice.isInfinite())
        assertFalse(infinite.totalProfit.isNaN())
    }

    @Test
    fun `margin negatif dijepit ke nol`() {
        val result = HppCalculator.calculate(
            prices = listOf(7_500L),
            portions = 5,
            marginMode = HppMarginMode.PERCENT,
            marginValue = -50.0
        )

        assertEquals(1_500.0, result.hppPerPortion, DELTA)
        assertEquals(1_500.0, result.sellingPrice, DELTA)
        assertEquals(0.0, result.profitPerPortion, DELTA)
    }

    @Test
    fun `pembulatan selalu ke atas kelipatan lima ratus`() {
        assertEquals(
            1_500L,
            HppCalculator.calculate(listOf(1_500L), 1, marginValue = 0.0)
                .roundedSellingPrice
        )
        assertEquals(
            3_500L,
            HppCalculator.calculate(listOf(3_121L), 1, marginValue = 0.0)
                .roundedSellingPrice
        )
        assertEquals(
            500L,
            HppCalculator.calculate(listOf(1L), 1, marginValue = 0.0)
                .roundedSellingPrice
        )
    }

    @Test
    fun `nilai sangat besar tetap finite`() {
        val result = HppCalculator.calculate(
            prices = listOf(Long.MAX_VALUE, Long.MAX_VALUE),
            portions = 1,
            marginMode = HppMarginMode.PERCENT,
            marginValue = 100.0
        )

        assertTrue(result.totalModal.isFinite())
        assertTrue(result.hppPerPortion.isFinite())
        assertTrue(result.sellingPrice.isFinite())
        assertTrue(result.totalProfit.isFinite())
        assertEquals(Long.MAX_VALUE, result.roundedSellingPrice)
    }

    @Test
    fun `harga jual sama dengan hpp saat margin nol`() {
        val result = HppCalculator.calculate(
            prices = goldenPrices,
            portions = 25,
            marginMode = HppMarginMode.NOMINAL,
            marginValue = 0.0
        )

        assertEquals(result.hppPerPortion, result.sellingPrice, DELTA)
        assertEquals(0.0, result.totalProfit, DELTA)
        assertEquals(2_000L, result.roundedSellingPrice)
    }

    private companion object {
        private const val DELTA = 0.0001
    }
}
