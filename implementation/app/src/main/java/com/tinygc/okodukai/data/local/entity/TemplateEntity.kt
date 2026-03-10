package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * テンプレートテーブル (templates)
 * 支出入力のテンプレートを管理する
 */
@Entity(
    tableName = "templates",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["sub_category_id"]),
        Index(value = ["display_order"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["sub_category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TemplateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String,

    @ColumnInfo(name = "sub_category_id")
    val subCategoryId: String? = null,

    @ColumnInfo(name = "amount")
    val amount: Int, // 固定金額（円）

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0, // 表示順序

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
