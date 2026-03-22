
package com.tinygc.okodukai.presentation.component

import com.tinygc.okodukai.domain.util.DateTimeUtil
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * DatePickerField で使用する日付変換ロジック (DateTimeUtil) のテスト
 *
 * 検証項目（Issue #18: 日付入力をカレンダーピッカー形式に変更）：
 * - DateTimeUtil.dateStringToMillis: 日付文字列→ミリ秒変換
 * - DateTimeUtil.millisToDateString: ミリ秒→日付文字列変換
 * - 不正な日付文字列の場合に現在日付にフォールバックすること
 */
class DatePickerFieldDateConversionTest {

    @Test
    fun `dateStringToMillisで正しいミリ秒に変換できること`() {
        // Given
        val dateString = "2026-03-21"
        val expected = LocalDate.of(2026, 3, 21)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        // When
        val result = DateTimeUtil.dateStringToMillis(dateString)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `millisToDateStringで正しい日付文字列に変換できること`() {
        // Given
        val millis = LocalDate.of(2026, 3, 21)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        // When
        val result = DateTimeUtil.millisToDateString(millis)

        // Then
        assertEquals("2026-03-21", result)
    }

    @Test
    fun `dateStringToMillisとmillisToDateStringで往復変換が一致すること`() {
        // Given
        val dateString = "2026-01-15"

        // When
        val millis = DateTimeUtil.dateStringToMillis(dateString)
        val result = DateTimeUtil.millisToDateString(millis)

        // Then
        assertEquals(dateString, result)
    }

    @Test
    fun `空文字の場合に現在日付にフォールバックすること`() {
        // Given
        val dateValue = ""

        // When
        val millis = DateTimeUtil.dateStringToMillis(dateValue)

        // Then
        val todayMillis = LocalDate.now(DateTimeUtil.APP_TIMEZONE)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        assertEquals(todayMillis, millis)
    }

    @Test
    fun `不正な日付フォーマットの場合にフォールバックすること`() {
        // Given
        val dateValue = "invalid-date"

        // When
        val millis = DateTimeUtil.dateStringToMillis(dateValue)

        // Then
        val todayMillis = LocalDate.now(DateTimeUtil.APP_TIMEZONE)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        assertEquals(todayMillis, millis)
    }

    @Test
    fun `年末年始の日付も正しく往復変換できること`() {
        // Given
        val dateString = "2025-12-31"

        // When
        val result = DateTimeUtil.millisToDateString(DateTimeUtil.dateStringToMillis(dateString))

        // Then
        assertEquals("2025-12-31", result)
    }

    @Test
    fun `うるう年の日付も正しく往復変換できること`() {
        // Given
        val dateString = "2028-02-29"

        // When
        val result = DateTimeUtil.millisToDateString(DateTimeUtil.dateStringToMillis(dateString))

        // Then
        assertEquals("2028-02-29", result)
    }
}
