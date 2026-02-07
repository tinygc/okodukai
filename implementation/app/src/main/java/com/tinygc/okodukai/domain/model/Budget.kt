package com.tinygc.okodukai.domain.model

/**
 * 予算ドメインモデル
 */
data class Budget(
    val id: String,
    val month: String, // YYYY-MM
    val amount: Int,
    val createdAt: String,
    val updatedAt: String
)
