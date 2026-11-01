package com.henrisusanto.vitmate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerCountdownTest {

    @Test
    fun `sisa waktu dihitung dari waktu selesai absolut`() {
        assertEquals(60L, TimerCountdown.remainingSeconds(60_000L, 0L))
        assertEquals(1L, TimerCountdown.remainingSeconds(60_000L, 59_001L))
        assertEquals(1L, TimerCountdown.remainingSeconds(60_000L, 59_000L))
        assertEquals(2L, TimerCountdown.remainingSeconds(60_000L, 58_500L))
    }

    @Test
    fun `timer yang sudah lewat bernilai nol`() {
        assertEquals(0L, TimerCountdown.remainingSeconds(60_000L, 60_000L))
        assertEquals(0L, TimerCountdown.remainingSeconds(60_000L, 90_000L))
    }

    @Test
    fun `format memakai menit detik atau jam menit detik`() {
        assertEquals("00:00", TimerCountdown.format(0L))
        assertEquals("00:59", TimerCountdown.format(59L))
        assertEquals("01:00", TimerCountdown.format(60L))
        assertEquals("59:59", TimerCountdown.format(3_599L))
        assertEquals("1:00:00", TimerCountdown.format(3_600L))
        assertEquals("1:01:01", TimerCountdown.format(3_661L))
    }

    @Test
    fun `format nilai negatif tetap nol`() {
        assertEquals("00:00", TimerCountdown.format(-5L))
    }
}
