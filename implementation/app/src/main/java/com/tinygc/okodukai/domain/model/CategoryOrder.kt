package com.tinygc.okodukai.domain.model

/**
 * カテゴリ並び順ドメインモデル
 */
data class CategoryOrder(
    val id: String,
    val categoryId: String,
    val parentId: String? = null,
    val displayOrder: Int,
    val createdAt: String,
    val updatedAt: String
)
