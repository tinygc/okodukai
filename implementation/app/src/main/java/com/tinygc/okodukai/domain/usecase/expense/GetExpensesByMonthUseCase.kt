package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 月別支出取得ユースケース
 * 
 * 指定月の全支出（未分類含む）を取得する
 */
class GetExpensesByMonthUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * 指定月の支出を取得（一回限り）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 支出リスト
     */
    suspend operator fun invoke(month: String): Result<List<Expense>> {
        return expenseRepository.getExpensesByMonth(month)
    }
    
    /**
     * 指定月の支出を監視（リアルタイム更新）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 支出リストのFlow
     */
    fun observe(month: String): Flow<List<Expense>> {
        return expenseRepository.observeExpensesByMonth(month)
    }
}
