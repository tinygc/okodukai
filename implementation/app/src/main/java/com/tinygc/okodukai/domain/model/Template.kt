package com.tinygc.okodukai.domain.model

/**
 * テンプレートドメインモデル
 */
data class Template(
    val id: String,
    val name: String,
    val categoryId: String,
    val subCategoryId: String? = null,
    val amount: Int,
    val sortOrder: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
