package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 臨時収入更新ユースケース
 */
class UpdateIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 臨時収入を更新する
     * 
     * @param incomeId 更新対象の臨時収入ID
     * @param date 収入日（YYYY-MM-DD）
     * @param amount 金額（円）
     * @param memo メモ（任意）
     * @return 更新結果
     */
    suspend operator fun invoke(
        incomeId: String,
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
        
        // 既存の臨時収入を取得
        val existingIncomeResult = incomeRepository.getIncomeById(incomeId)
        if (existingIncomeResult.isFailure) {
            return Result.failure(existingIncomeResult.exceptionOrNull()!!)
        }
        
        val existingIncome = existingIncomeResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("指定された臨時収入が見つかりません"))
        
        val updatedIncome = Income(
            id = incomeId,
            date = date,
            amount = amount,
            memo = memo?.takeIf { it.isNotBlank() },
            createdAt = existingIncome.createdAt,
            updatedAt = DateTimeUtil.getCurrentDateTime()
        )
        
        return incomeRepository.saveIncome(updatedIncome)
    }
}
