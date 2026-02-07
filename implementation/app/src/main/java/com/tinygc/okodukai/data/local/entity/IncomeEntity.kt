package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 臨時収入テーブル (incomes)
 * 臨時収入データを管理する（集計対象外）
 */
@Entity(
    tableName = "incomes",
    indices = [Index(value = ["date"])]
)
data class IncomeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD

    @ColumnInfo(name = "amount")
    val amount: Int, // 臨時収入金額（円）

    @ColumnInfo(name = "memo")
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
