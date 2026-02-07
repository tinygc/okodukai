package com.tinygc.okodukai.domain.model

/**
 * 臨時収入ドメインモデル
 */
data class Income(
    val id: String,
    val date: String, // YYYY-MM-DD
    val amount: Int,
    val memo: String? = null,
    val createdAt: String,
    val updatedAt: String
)
