package com.tinygc.okodukai.domain.usecase.summary

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.category.FakeCategoryRepository
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GetMonthlySummaryUseCase のテスト
 * 
 * 検証項目（FR-4: 支出の可視化）：
 * - 月次サマリが正確に集計されること
 * - カテゴリ別の支出額が正確に計算されること
 * - 予算と支出の差分が正確に表示されること
 * - 未分類支出が含まれること
 */
class GetMonthlySummaryUseCaseTest {

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var getMonthlySummaryUseCase: GetMonthlySummaryUseCase

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        getMonthlySummaryUseCase = GetMonthlySummaryUseCase(
            fakeBudgetRepository,
            fakeExpenseRepository,
            fakeCategoryRepository
        )
    }

    @Test
    fun `月次サマリが正確に集計されること`() = runTest {
        // Given
        val month = "2026-02"
        val budget = Budget("b1", month, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val expense1 = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        val expense2 = Expense("e2", "2026-02-05", 2000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(expense1, expense2))

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(month, summary.month)
        assertEquals(50000, summary.budget)
        assertEquals(3000, summary.totalExpense)
        assertEquals(47000, summary.remainingBudget)
        assertEquals(2, summary.expenses.size)
    }

    @Test
    fun `予算が未設定の場合でもサマリが取得できること`() = runTest {
        // Given
        val month = "2026-02"
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val expense = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertNull(summary.budget)
        assertEquals(1000, summary.totalExpense)
        assertNull(summary.remainingBudget)
    }

    @Test
    fun `カテゴリ別支出が正確に集計されること`() = runTest {
        // Given
        val month = "2026-02"
        
        val cat1 = Category("cat1", "食費", null, "", "")
        val cat2 = Category("cat2", "交通費", null, "", "")
        fakeCategoryRepository.addCategory(cat1)
        fakeCategoryRepository.addCategory(cat2)
        
        val expense1 = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        val expense2 = Expense("e2", "2026-02-02", 500, "cat1", null, null, false, "", "")
        val expense3 = Expense("e3", "2026-02-03", 2000, "cat2", null, null, false, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(expense1, expense2, expense3))

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(2, summary.categoryTotals.size)
        
        // 合計金額の多い順にソートされていること
        assertEquals("cat2", summary.categoryTotals[0].category.id)
        assertEquals(2000, summary.categoryTotals[0].total)
        assertEquals("cat1", summary.categoryTotals[1].category.id)
        assertEquals(1500, summary.categoryTotals[1].total)
    }

    @Test
    fun `未分類支出は支出合計に含まれないこと`() = runTest {
        // Given
        val month = "2026-02"
        val budget = Budget("b1", month, 10000, "", "")
        fakeBudgetRepository.addBudget(budget)
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val categorizedExpense = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        val uncategorizedExpense = Expense("e2", "2026-02-02", 500, null, null, null, true, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(categorizedExpense, uncategorizedExpense))

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(1000, summary.totalExpense) // 未分類除外
        assertEquals(9000, summary.remainingBudget)
        assertEquals(2, summary.expenses.size) // expensesには両方含まれる
    }

    @Test
    fun `支出がない月でも正常に動作すること`() = runTest {
        // Given
        val month = "2026-02"
        val budget = Budget("b1", month, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(50000, summary.budget)
        assertEquals(0, summary.totalExpense)
        assertEquals(50000, summary.remainingBudget)
        assertTrue(summary.categoryTotals.isEmpty())
        assertTrue(summary.expenses.isEmpty())
    }
}
