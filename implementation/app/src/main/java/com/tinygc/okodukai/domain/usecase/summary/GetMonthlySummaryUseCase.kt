package com.tinygc.okodukai.domain.usecase.summary

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.MonthlySummary
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.repository.IncomeRepository
import javax.inject.Inject
import java.time.YearMonth
import com.tinygc.okodukai.domain.model.CategoryTotal as DomainCategoryTotal

/**
 * 月次サマリ取得ユースケース
 * 
 * 指定月の予算、支出合計、残額、カテゴリ別集計を取得する
 */
class GetMonthlySummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val incomeRepository: IncomeRepository
) {
    /**
     * 指定月の月次サマリを取得する
     * 
     * @param month 対象月（YYYY-MM）
     * @return 月次サマリ
     */
    suspend operator fun invoke(month: String): Result<MonthlySummary> = runCatching {
        // 仕様変更により、毎月予算は固定額として扱う。
        val latestBudget = budgetRepository.getAllBudgets()
            .getOrThrow()
            .maxByOrNull { it.updatedAt }

        val allExpenses = expenseRepository.getAllExpenses().getOrThrow()
        val categorizedExpenseByMonth = allExpenses
            .filter { !it.isUncategorized }
            .groupBy { it.date.substring(0, 7) }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        
        // 支出合計を取得（未分類除外）
        val totalExpenseResult = expenseRepository.getTotalExpenseByMonth(month)
        val totalExpense = totalExpenseResult.getOrThrow()
        
        // 臨時収入合計を取得
        val totalIncomeResult = incomeRepository.getTotalIncomeByMonth(month)
        val totalIncome = totalIncomeResult.getOrThrow()
        
        val fallbackStartMonth = latestBudget?.let {
            val createdMonth = it.createdAt.take(7)
            if (createdMonth.matches(Regex("\\d{4}-\\d{2}"))) createdMonth else it.month
        }
        val oldestExpenseMonth = categorizedExpenseByMonth.keys.minOrNull()
        val budgetStartMonth = oldestExpenseMonth ?: fallbackStartMonth
        val isBudgetActiveMonth = latestBudget != null && budgetStartMonth != null && month >= budgetStartMonth

        val effectiveBudget = if (isBudgetActiveMonth) {
            val carryOver = calculateCarryOverBudget(
                startMonth = budgetStartMonth!!,
                targetMonth = month,
                baseBudget = latestBudget.amount,
                categorizedExpenseByMonth = categorizedExpenseByMonth,
                incomeByMonth = buildIncomeByMonthMap(budgetStartMonth)
            )
            latestBudget.amount + carryOver
        } else {
            null
        }

        // 残予算を計算（臨時収入を加算）
        val remainingBudget = effectiveBudget?.let { it - totalExpense + totalIncome }
        
        // カテゴリ別合計を取得
        val categoryTotalsResult = getCategoryTotals(month)
        val categoryTotals = categoryTotalsResult.getOrThrow()
        
        // 全支出を取得（未分類含む）
        val expensesResult = expenseRepository.getExpensesByMonth(month)
        val expenses = expensesResult.getOrThrow()
        
        MonthlySummary(
            month = month,
            budget = effectiveBudget,
            totalExpense = totalExpense,
            remainingBudget = remainingBudget,
            categoryTotals = categoryTotals,
            expenses = expenses
        )
    }

    private suspend fun calculateCarryOverBudget(
        startMonth: String,
        targetMonth: String,
        baseBudget: Int,
        categorizedExpenseByMonth: Map<String, Int>,
        incomeByMonth: Map<String, Int>
    ): Int {
        if (targetMonth <= startMonth) return 0

        var carryOver = 0
        var current = YearMonth.parse(startMonth)
        val target = YearMonth.parse(targetMonth)

        while (current < target) {
            val monthKey = current.toString()
            val monthExpense = categorizedExpenseByMonth[monthKey] ?: 0
            val monthIncome = incomeByMonth[monthKey] ?: 0
            val remaining = baseBudget + carryOver - monthExpense + monthIncome
            carryOver = remaining.coerceAtLeast(0)
            current = current.plusMonths(1)
        }

        return carryOver
    }
    
    /**
     * 月別収入マップを構築する
     * スタート月からすべての月の収入を取得
     */
    private suspend fun buildIncomeByMonthMap(startMonth: String): Map<String, Int> {
        return try {
            val allIncomes = incomeRepository.getAllIncomes().getOrThrow()
            allIncomes
                .filter { it.date.substring(0, 7) >= startMonth }
                .groupBy { it.date.substring(0, 7) }
                .mapValues { (_, incomes) -> incomes.sumOf { it.amount } }
        } catch (e: Exception) {
            emptyMap()
        }
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
