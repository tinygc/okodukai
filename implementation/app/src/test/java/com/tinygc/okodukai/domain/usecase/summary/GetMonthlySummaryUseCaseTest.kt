package com.tinygc.okodukai.domain.usecase.summary

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.category.FakeCategoryRepository
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var getMonthlySummaryUseCase: GetMonthlySummaryUseCase

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeIncomeRepository = FakeIncomeRepository()
        getMonthlySummaryUseCase = GetMonthlySummaryUseCase(
            fakeBudgetRepository,
            fakeExpenseRepository,
            fakeCategoryRepository,
            fakeIncomeRepository
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

    @Test
    fun `前月残額があっても当月予算は固定額のまま表示されること`() = runTest {
        // Given
        val budget = Budget("b1", "2026-01", 50000, "2026-01-01T00:00:00", "2026-01-01T00:00:00")
        fakeBudgetRepository.addBudget(budget)

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)

        // 1月: 10,000円使用
        fakeExpenseRepository.expenses.add(
            Expense("e1", "2026-01-10", 10000, "cat1", null, null, false, "", "")
        )
        // 2月: 20,000円使用
        fakeExpenseRepository.expenses.add(
            Expense("e2", "2026-02-05", 20000, "cat1", null, null, false, "", "")
        )

        // When
        val result = getMonthlySummaryUseCase("2026-02")

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(50000, summary.budget)
        assertEquals(20000, summary.totalExpense)
        assertEquals(30000, summary.remainingBudget)
    }

    @Test
    fun `前月が予算超過でも当月予算は固定額のまま表示されること`() = runTest {
        // Given
        val budget = Budget("b1", "2026-01", 30000, "2026-01-01T00:00:00", "2026-01-01T00:00:00")
        fakeBudgetRepository.addBudget(budget)

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)

        // 1月: 50,000円使用
        fakeExpenseRepository.expenses.add(
            Expense("e1", "2026-01-10", 50000, "cat1", null, null, false, "", "")
        )

        // When
        val result = getMonthlySummaryUseCase("2026-02")

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(30000, summary.budget)
        assertEquals(0, summary.totalExpense)
        assertEquals(30000, summary.remainingBudget)
    }

    @Test
    fun `getAllExpensesが失敗しても当月サマリ取得は成功すること`() = runTest {
        // Given
        val month = "2026-02"
        fakeBudgetRepository.addBudget(Budget("b1", month, 50000, "", ""))

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        fakeExpenseRepository.expenses.add(
            Expense("e1", "$month-01", 1200, "cat1", null, null, false, "", "")
        )
        fakeExpenseRepository.shouldFailGetAllExpenses = true

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(50000, summary.budget)
        assertEquals(1200, summary.totalExpense)
        assertEquals(48800, summary.remainingBudget)
    }

    @Test
    fun `臨時収入が残予算に反映されること`() = runTest {
        // Given
        val month = "2026-02"
        fakeBudgetRepository.addBudget(Budget("b1", month, 50000, "", ""))

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val expense = Expense("e1", "$month-01", 30000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)
        
        val income = com.tinygc.okodukai.domain.model.Income("inc1", "$month-10", 20000, "ボーナス", "", "")
        fakeIncomeRepository.incomes.add(income)

        // When
        val result = getMonthlySummaryUseCase(month)

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(50000, summary.budget)
        assertEquals(30000, summary.totalExpense)
        assertEquals(40000, summary.remainingBudget) // 50000 - 30000 + 20000
    }

    @Test
    fun `予算設定が3月でも最古支出が2月なら2月を開始月として扱うこと`() = runTest {
        // Given
        // 3月に予算設定したケース
        val budget = Budget("b1", "2026-03", 40000, "2026-03-01T00:00:00", "2026-03-01T00:00:00")
        fakeBudgetRepository.addBudget(budget)

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)

        // 2月の支出データが存在
        fakeExpenseRepository.expenses.add(
            Expense("e1", "2026-02-10", 5000, "cat1", null, null, false, "", "")
        )

        // When
        val result = getMonthlySummaryUseCase("2026-02")

        // Then
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(40000, summary.budget)
        assertEquals(5000, summary.totalExpense)
        assertEquals(35000, summary.remainingBudget)
    }
}

class FakeIncomeRepository : IncomeRepository {
    val incomes = mutableListOf<Income>()

    override suspend fun saveIncome(income: Income): Result<Unit> {
        val index = incomes.indexOfFirst { it.id == income.id }
        if (index != -1) {
            incomes[index] = income
        } else {
            incomes.add(income)
        }
        return Result.success(Unit)
    }

    override suspend fun getIncomeById(id: String): Result<Income?> {
        return Result.success(incomes.find { it.id == id })
    }

    override suspend fun getAllIncomes(): Result<List<Income>> {
        return Result.success(incomes)
    }

    override suspend fun deleteIncome(income: Income): Result<Unit> {
        incomes.remove(income)
        return Result.success(Unit)
    }

    override suspend fun getIncomesByMonth(month: String): Result<List<Income>> {
        return Result.success(incomes.filter { it.date.startsWith(month) })
    }

    override suspend fun getTotalIncomeByMonth(month: String): Result<Int> {
        val total = incomes
            .filter { it.date.startsWith(month) }
            .sumOf { it.amount }
        return Result.success(total)
    }

    override fun observeIncomesByMonth(month: String): Flow<List<Income>> {
        return flowOf(incomes.filter { it.date.startsWith(month) })
    }

    override fun observeTotalIncomeByMonth(month: String): Flow<Int> {
        val total = incomes
            .filter { it.date.startsWith(month) }
            .sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeAllIncomes(): Flow<List<Income>> {
        return flowOf(incomes)
    }
}
