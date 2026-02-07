package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月次予算テーブル (budgets)
 * 月ごとの予算を管理する
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["month"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "month")
    val month: String, // YYYY-MM

    @ColumnInfo(name = "amount")
    val amount: Int, // 予算金額（円）

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
