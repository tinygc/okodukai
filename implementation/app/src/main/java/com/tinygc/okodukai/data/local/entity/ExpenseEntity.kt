package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 支出テーブル (expenses)
 * 支出データを管理する
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category_id"]),
        Index(value = ["sub_category_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["sub_category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD

    @ColumnInfo(name = "amount")
    val amount: Int, // 支出金額（円）

    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,

    @ColumnInfo(name = "sub_category_id")
    val subCategoryId: String? = null,

    @ColumnInfo(name = "memo")
    val memo: String? = null,

    @ColumnInfo(name = "is_uncategorized")
    val isUncategorized: Boolean = false, // 未分類フラグ

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
