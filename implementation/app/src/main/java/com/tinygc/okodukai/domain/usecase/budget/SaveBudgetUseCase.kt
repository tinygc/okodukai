package com.tinygc.okodukai.domain.usecase.budget

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 予算保存ユースケース（追加または更新）
 *
 * 毎月固定額の予算を1件として保存する
 */
class SaveBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    /**
     * 予算を保存する
     * 
    * @param month 互換性維持のために受け取る月（YYYY-MM）
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
        
        // 仕様変更により、予算は毎月固定額を1件だけ保持する。
        val existingBudget = budgetRepository.getAllBudgets()
            .getOrThrow()
            .maxByOrNull { it.updatedAt }
        
        val budget = if (existingBudget != null) {
            // 更新
            Budget(
                id = existingBudget.id,
                month = existingBudget.month,
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
