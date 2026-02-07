package com.tinygc.okodukai.domain.usecase.summary

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.MonthlySummary
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import javax.inject.Inject
import com.tinygc.okodukai.domain.model.CategoryTotal as DomainCategoryTotal

/**
 * 月次サマリ取得ユースケース
 * 
 * 指定月の予算、支出合計、残額、カテゴリ別集計を取得する
 */
class GetMonthlySummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) {
    /**
     * 指定月の月次サマリを取得する
     * 
     * @param month 対象月（YYYY-MM）
     * @return 月次サマリ
     */
    suspend operator fun invoke(month: String): Result<MonthlySummary> = runCatching {
        // 予算を取得
        val budgetResult = budgetRepository.getBudgetByMonth(month)
        val budget = budgetResult.getOrNull()
        
        // 支出合計を取得（未分類除外）
        val totalExpenseResult = expenseRepository.getTotalExpenseByMonth(month)
        val totalExpense = totalExpenseResult.getOrThrow()
        
        // 残予算を計算
        val remainingBudget = budget?.let { it.amount - totalExpense }
        
        // カテゴリ別合計を取得
        val categoryTotalsResult = getCategoryTotals(month)
        val categoryTotals = categoryTotalsResult.getOrThrow()
        
        // 全支出を取得（未分類含む）
        val expensesResult = expenseRepository.getExpensesByMonth(month)
        val expenses = expensesResult.getOrThrow()
        
        MonthlySummary(
            month = month,
            budget = budget?.amount,
            totalExpense = totalExpense,
            remainingBudget = remainingBudget,
            categoryTotals = categoryTotals,
            expenses = expenses
        )
    }
    
    /**
     * カテゴリ別合計を取得する
     */
    private suspend fun getCategoryTotals(month: String): Result<List<DomainCategoryTotal>> = runCatching {
        // 分類済み支出を取得
        val expensesResult = expenseRepository.getCategorizedExpensesByMonth(month)
        val expenses = expensesResult.getOrThrow()
        
        // カテゴリIDでグルーピングして合計を計算
        val categoryTotalMap = expenses
            .filter { it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        
        // カテゴリ情報を取得してマージ
        val categoryMap = mutableMapOf<String, Category>()
        for (categoryId in categoryTotalMap.keys) {
            val categoryResult = categoryRepository.getCategoryById(categoryId)
            categoryResult.getOrNull()?.let { category ->
                categoryMap[categoryId] = category
            }
        }
        
        // CategoryTotalリストを作成
        categoryTotalMap.mapNotNull { (categoryId, total) ->
            categoryMap[categoryId]?.let { category ->
                DomainCategoryTotal(
                    category = category,
                    total = total
                )
            }
        }.sortedByDescending { it.total }
    }
}
