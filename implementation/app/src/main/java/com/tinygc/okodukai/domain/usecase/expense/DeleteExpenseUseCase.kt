package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import javax.inject.Inject

/**
 * 支出削除ユースケース
 */
class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * 支出を削除する
     * 
     * @param expense 削除対象の支出
     * @return 削除結果
     */
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        return expenseRepository.deleteExpense(expense)
    }
}
