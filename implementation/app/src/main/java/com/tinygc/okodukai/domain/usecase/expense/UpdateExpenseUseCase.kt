package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 支出更新ユースケース
 * 
 * ビジネスルール：
 * - 未分類フラグはcategoryIdがnullの場合に自動的にtrueになる
 * - 日付変更が可能
 * - 金額は正の整数のみ許可
 */
class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    /**
     * 支出を更新する
     * 
     * @param expenseId 更新対象の支出ID
     * @param date 支出日（YYYY-MM-DD）
     * @param amount 金額（円）
     * @param categoryId カテゴリID（nullの場合は未分類）
     * @param subCategoryId サブカテゴリID（任意）
     * @param memo メモ（任意）
     * @return 更新結果
     */
    suspend operator fun invoke(
        expenseId: String,
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
        
        // 既存の支出を取得
        val existingExpenseResult = expenseRepository.getExpenseById(expenseId)
        if (existingExpenseResult.isFailure) {
            return Result.failure(existingExpenseResult.exceptionOrNull()!!)
        }
        
        val existingExpense = existingExpenseResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("指定された支出が見つかりません"))
        
        val updatedExpense = Expense(
            id = expenseId,
            date = date,
            amount = amount,
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            memo = memo?.takeIf { it.isNotBlank() },
            isUncategorized = categoryId == null,
            createdAt = existingExpense.createdAt,
            updatedAt = DateTimeUtil.getCurrentDateTime()
        )
        
        return expenseRepository.saveExpense(updatedExpense)
    }
}
