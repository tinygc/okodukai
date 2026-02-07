package com.tinygc.okodukai.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * カテゴリテーブル (categories)
 * カテゴリとサブカテゴリを管理する
 * parent_id が NULL の場合は親カテゴリ、値がある場合はサブカテゴリ
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["parent_id"])],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "parent_id")
    val parentId: String? = null, // NULL: 親カテゴリ, 値あり: サブカテゴリ

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
