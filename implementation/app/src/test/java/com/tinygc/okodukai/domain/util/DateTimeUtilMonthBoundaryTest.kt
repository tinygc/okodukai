package com.tinygc.okodukai.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateTimeUtilMonthBoundaryTest {

    @Test
    fun `開始日29以上は28として扱うこと`() {
        assertEquals(1, DateTimeUtil.normalizeMonthStartDay(1))
        assertEquals(15, DateTimeUtil.normalizeMonthStartDay(15))
        assertEquals(28, DateTimeUtil.normalizeMonthStartDay(29))
        assertEquals(28, DateTimeUtil.normalizeMonthStartDay(31))
    }

    @Test
    fun `日付から論理月を解決できること`() {
        assertEquals(
            "2026-02",
            DateTimeUtil.resolveMonthLabel(LocalDate.parse("2026-03-05"), 10)
        )
        assertEquals(
            "2026-03",
            DateTimeUtil.resolveMonthLabel(LocalDate.parse("2026-03-10"), 10)
        )
    }

    @Test
    fun `開始日31のときは28日境界で月範囲を作ること`() {
        val (start, endExclusive) = DateTimeUtil.getMonthDateRange("2026-02", 31)

        assertEquals("2026-02-28", start)
        assertEquals("2026-03-28", endExclusive)
    }
}
