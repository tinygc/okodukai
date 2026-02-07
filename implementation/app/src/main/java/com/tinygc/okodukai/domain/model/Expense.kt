package com.tinygc.okodukai.domain.model

/**
 * 支出ドメインモデル
 */
data class Expense(
    val id: String,
    val date: String, // YYYY-MM-DD
    val amount: Int,
    val categoryId: String? = null,
    val subCategoryId: String? = null,
    val memo: String? = null,
    val isUncategorized: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)
