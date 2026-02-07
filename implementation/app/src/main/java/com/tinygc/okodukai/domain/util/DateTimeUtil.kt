package com.tinygc.okodukai.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 日付・時刻ユーティリティ
 */
object DateTimeUtil {
    
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val MONTH_FORMAT = "yyyy-MM"
    private const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    
    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val monthFormatter = SimpleDateFormat(MONTH_FORMAT, Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
    
    /**
     * 現在日付を取得（YYYY-MM-DD）
     */
    fun getCurrentDate(): String {
        return dateFormatter.format(Date())
    }
    
    /**
     * 現在月を取得（YYYY-MM）
     */
    fun getCurrentMonth(): String {
        return monthFormatter.format(Date())
    }
    
    /**
     * 現在日時を取得（YYYY-MM-DDTHH:mm:ss）
     */
    fun getCurrentDateTime(): String {
        return dateTimeFormatter.format(Date())
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
