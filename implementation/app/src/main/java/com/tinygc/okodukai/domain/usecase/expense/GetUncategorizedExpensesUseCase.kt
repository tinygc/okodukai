package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 未分類支出取得ユースケース
 * 
 * ウィジェットから入力された未分類支出を取得する
 */
class GetUncategorizedExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * 指定月の未分類支出を監視（リアルタイム更新）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 未分類支出リストのFlow
     */
    fun observe(month: String): Flow<List<Expense>> {
        return expenseRepository.observeUncategorizedExpensesByMonth(month)
    }
}
