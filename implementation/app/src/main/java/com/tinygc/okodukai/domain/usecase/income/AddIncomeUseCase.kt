package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 臨時収入追加ユースケース
 * 
 * ビジネスルール：
 * - 金額は正の整数のみ許可
 * - 臨時収入は支出や予算の集計対象外
 */
class AddIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 臨時収入を追加する
     * 
     * @param date 収入日（YYYY-MM-DD）
     * @param amount 金額（円）
     * @param memo メモ（任意）
     * @return 追加結果
     */
    suspend operator fun invoke(
        date: String,
        amount: Int,
        memo: String? = null
    ): Result<Unit> {
        // バリデーション
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("金額は正の整数を入力してください"))
        }
        
        if (date.isBlank()) {
            return Result.failure(IllegalArgumentException("日付を入力してください"))
        }
        
        val currentDateTime = DateTimeUtil.getCurrentDateTime()
        val income = Income(
            id = DateTimeUtil.generateId(),
            date = date,
            amount = amount,
            memo = memo?.takeIf { it.isNotBlank() },
            createdAt = currentDateTime,
            updatedAt = currentDateTime
        )
        
        return incomeRepository.saveIncome(income)
    }
}
