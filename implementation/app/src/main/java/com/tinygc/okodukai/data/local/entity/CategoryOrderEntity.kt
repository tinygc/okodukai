package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * カテゴリ並び順テーブル (category_orders)
 * カテゴリとサブカテゴリの表示順を管理する
 * parent_id が NULL の場合は親カテゴリ間の並び順、値がある場合はサブカテゴリ間の並び順
 */
@Entity(
    tableName = "category_orders",
    indices = [
        Index(value = ["category_id"], unique = true),
        Index(value = ["parent_id"]),
        Index(value = ["parent_id", "display_order"])
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
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategoryOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String, // categories.id

    @ColumnInfo(name = "parent_id")
    val parentId: String? = null, // 並び順のスコープ（NULL=親カテゴリ、値あり=サブカテゴリ）

    @ColumnInfo(name = "display_order")
    val displayOrder: Int, // 表示順序（0以上の整数）

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
