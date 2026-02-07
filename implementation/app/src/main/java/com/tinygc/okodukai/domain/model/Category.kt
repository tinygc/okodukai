package com.tinygc.okodukai.domain.model

/**
 * カテゴリドメインモデル
 */
data class Category(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: String,
    val updatedAt: String
)
