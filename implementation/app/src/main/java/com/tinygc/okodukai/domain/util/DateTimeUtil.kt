package com.tinygc.okodukai.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 日付・時刻ユーティリティ
 */
object DateTimeUtil {

    val APP_TIMEZONE: ZoneId = ZoneId.of("Asia/Tokyo")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    /**
     * DatePicker用: 日付文字列(yyyy-MM-dd)をUTC基準のミリ秒に変換。
     * パース失敗時はアプリタイムゾーンの現在日付をUTC基準ミリ秒へ変換して返す。
     */
    fun dateStringToMillis(dateString: String): Long {
        return try {
            LocalDate.parse(dateString)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            LocalDate.now(APP_TIMEZONE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
    }

    /**
     * DatePicker用: UTC基準ミリ秒を日付文字列(yyyy-MM-dd)に変換。
     */
    fun millisToDateString(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(dateFormatter)
    }
    
    /**
     * 現在日付を取得（YYYY-MM-DD）
     */
    fun getCurrentDate(): String {
        return LocalDate.now(APP_TIMEZONE).format(dateFormatter)
    }
    
    /**
     * 現在月を取得（YYYY-MM）
     */
    fun getCurrentMonth(monthStartDay: Int = 1): String {
        val today = LocalDate.now(APP_TIMEZONE)
        return resolveMonthLabel(today, monthStartDay)
    }

    /**
     * 月開始日を正規化する。
     *
     * 入力は1〜31を許可し、集計境界は常に1〜28に収める。
     */
    fun normalizeMonthStartDay(monthStartDay: Int): Int {
        return monthStartDay.coerceIn(1, 28)
    }

    /**
     * 日付が属する論理月（YYYY-MM）を返す。
     */
    fun resolveMonthLabel(date: LocalDate, monthStartDay: Int): String {
        val boundaryDay = normalizeMonthStartDay(monthStartDay)
        val yearMonth = if (date.dayOfMonth < boundaryDay) {
            YearMonth.from(date).minusMonths(1)
        } else {
            YearMonth.from(date)
        }
        return yearMonth.format(monthFormatter)
    }

    /**
     * 論理月に対応する集計期間を返す。
     *
     * 戻り値は [開始日, 終了日(排他的)] の半開区間。
     */
    fun getMonthDateRange(month: String, monthStartDay: Int): Pair<String, String> {
        val boundaryDay = normalizeMonthStartDay(monthStartDay)
        val startYearMonth = YearMonth.parse(month)
        val startDate = startYearMonth.atDay(boundaryDay)
        val endDateExclusive = startDate.plusMonths(1)
        return startDate.format(dateFormatter) to endDateExclusive.format(dateFormatter)
    }
    
    /**
     * 現在日時を取得（YYYY-MM-DDTHH:mm:ss）
     */
    fun getCurrentDateTime(): String {
        return LocalDateTime.now(APP_TIMEZONE).format(dateTimeFormatter)
    }
    
    /**
     * 日付から月を抽出（YYYY-MM-DD -> YYYY-MM）
     */
    fun extractMonth(date: String): String {
        return date.substring(0, 7)
    }
    
    /**
     * UUIDを生成
     */
    fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}
