package com.tinygc.okodukai.domain.model

/**
 * 貯金目標
 */
data class SavingGoal(
    val id: String,
    val name: String,
    val targetAmount: Int,
    val isActive: Boolean,
    val displayOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 貯金目標の進捗
 */
data class SavingGoalProgress(
    val goal: SavingGoal,
    val remainingAmount: Int,
    val isAchieved: Boolean
)
