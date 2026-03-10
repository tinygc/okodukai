package com.tinygc.okodukai.domain.model

/**
 * 貯金進捗サマリ
 */
data class SavingsProgress(
    val month: String,
    val carryOverBalance: Int,
    val availableAmount: Int,
    val achievementMode: GoalAchievementMode,
    val goals: List<SavingGoalProgress>,
    val totalTargetAmount: Int,
    val totalRemainingAmount: Int,
    val isTotalAchieved: Boolean
)
