package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 支出追加ユースケース
 * 
 * ビジネスルール：
 * - 未分類フラグはcategoryIdがnullの場合に自動的にtrueになる
 * - 金額は正の整数のみ許可
 */
class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * 支出を追加する
     * 
     * @param date 支出日（YYYY-MM-DD）
     * @param amount 金額（円）
     * @param categoryId カテゴリID（nullの場合は未分類）
     * @param subCategoryId サブカテゴリID（任意）
     * @param memo メモ（任意）
     * @return 追加結果
     */
    suspend operator fun invoke(
        date: String,
        amount: Int,
        categoryId: String? = null,
        subCategoryId: String? = null,
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
        val expense = Expense(
            id = DateTimeUtil.generateId(),
            date = date,
            amount = amount,
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            memo = memo?.takeIf { it.isNotBlank() },
            isUncategorized = categoryId == null,
            createdAt = currentDateTime,
            updatedAt = currentDateTime
        )
        
        return expenseRepository.saveExpense(expense)
    }
}
