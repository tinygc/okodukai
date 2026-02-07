package com.tinygc.okodukai.domain.usecase.budget

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 予算保存ユースケース（追加または更新）
 * 
 * 月ごとに一つの予算のみ保存可能
 */
class SaveBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    /**
     * 予算を保存する
     * 
     * @param month 対象月（YYYY-MM）
     * @param amount 予算金額
     * @return 保存結果
     */
    suspend operator fun invoke(
        month: String,
        amount: Int
    ): Result<Unit> {
        // バリデーション
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("予算額は正の整数を入力してください"))
        }
        
        if (month.isBlank() || !month.matches(Regex("\\d{4}-\\d{2}"))) {
            return Result.failure(IllegalArgumentException("月の形式が正しくありません（YYYY-MM）"))
        }
        
        // 既存の予算を確認
        val existingBudgetResult = budgetRepository.getBudgetByMonth(month)
        val existingBudget = existingBudgetResult.getOrNull()
        
        val budget = if (existingBudget != null) {
            // 更新
            Budget(
                id = existingBudget.id,
                month = month,
                amount = amount,
                createdAt = existingBudget.createdAt,
                updatedAt = DateTimeUtil.getCurrentDateTime()
            )
        } else {
            // 新規作成
            val currentDateTime = DateTimeUtil.getCurrentDateTime()
            Budget(
                id = DateTimeUtil.generateId(),
                month = month,
                amount = amount,
                createdAt = currentDateTime,
                updatedAt = currentDateTime
            )
        }
        
        return budgetRepository.saveBudget(budget)
    }
}
