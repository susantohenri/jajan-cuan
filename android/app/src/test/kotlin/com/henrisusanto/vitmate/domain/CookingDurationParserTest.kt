package com.henrisusanto.vitmate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookingDurationParserTest {

    @Test
    fun `rentang memakai nilai terbesar`() {
        assertEquals(900, CookingDurationParser.parse("Tumis selama 10-15 menit."))
        assertEquals(
            900,
            CookingDurationParser.parse("Diamkan 10 sampai 15 menit sampai matang.")
        )
        assertEquals(
            2700,
            CookingDurationParser.parse("Kukus 30-45 menit dengan api sedang.")
        )
    }

    @Test
    fun `gabungan jam dan menit dijumlahkan`() {
        assertEquals(5400, CookingDurationParser.parse("Rebus 1 jam 30 menit."))
        assertEquals(8100, CookingDurationParser.parse("Simpan 2 jam 15 menit."))
        assertEquals(5400, CookingDurationParser.parse("Istirahatkan 1,5 jam."))
    }

    @Test
    fun `contoh nyata data resep`() {
        assertEquals(420, CookingDurationParser.parse("Kukus adonan sekitar 7menit."))
        assertEquals(2400, CookingDurationParser.parse("Kukus selama 40 menit."))
        assertEquals(2100, CookingDurationParser.parse("Goreng 35 menit dengan api kecil."))
    }

    @Test
    fun `beberapa durasi dalam satu langkah dijumlahkan`() {
        assertEquals(
            420,
            CookingDurationParser.parse("Rebus 5 menit, lalu diamkan 2 menit.")
        )
    }

    @Test
    fun `detik didukung`() {
        assertEquals(45, CookingDurationParser.parse("Blender 45 detik."))
        assertEquals(90, CookingDurationParser.parse("Tunggu 1-1,5 menit."))
    }

    @Test
    fun `tanpa durasi menghasilkan null`() {
        assertNull(CookingDurationParser.parse("Aduk adonan hingga rata."))
        assertNull(CookingDurationParser.parse(""))
        assertNull(CookingDurationParser.parse(null))
        assertNull(CookingDurationParser.parse("Sajikan selagi hangat."))
    }
}
