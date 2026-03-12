package com.tinygc.okodukai.domain.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 日付・時刻ユーティリティ
 */
object DateTimeUtil {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    
    /**
     * 現在日付を取得（YYYY-MM-DD）
     */
    fun getCurrentDate(): String {
        return LocalDate.now().format(dateFormatter)
    }
    
    /**
     * 現在月を取得（YYYY-MM）
     */
    fun getCurrentMonth(): String {
        return LocalDate.now().format(monthFormatter)
    }
    
    /**
     * 現在日時を取得（YYYY-MM-DDTHH:mm:ss）
     */
    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(dateTimeFormatter)
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
