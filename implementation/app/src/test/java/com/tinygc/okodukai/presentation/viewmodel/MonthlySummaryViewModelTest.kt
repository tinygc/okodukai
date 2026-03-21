package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.MainDispatcherRule
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.category.FakeCategoryRepository
import com.tinygc.okodukai.domain.usecase.category.GetCategoryByIdUseCase
import com.tinygc.okodukai.domain.usecase.expense.DeleteExpenseUseCase
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import com.tinygc.okodukai.domain.usecase.expense.UpdateExpenseUseCase
import com.tinygc.okodukai.domain.usecase.income.FakeIncomeRepository
import com.tinygc.okodukai.domain.usecase.income.GetTotalIncomeByMonthUseCase
import com.tinygc.okodukai.domain.usecase.saving.FakeSavingGoalRepository
import com.tinygc.okodukai.domain.usecase.saving.GetSavingsProgressUseCase
import com.tinygc.okodukai.domain.usecase.summary.GetMonthlySummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * MonthlySummaryViewModel のテスト
 * 
 * 検証項目（FR-4: 支出の可視化）：
 * - 月次サマリが正確に表示されること
 * - カテゴリ別集計が正確に表示されること
 * - 支出削除が正常に動作すること
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MonthlySummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var fakeSavingGoalRepository: FakeSavingGoalRepository
    private lateinit var getMonthlySummaryUseCase: GetMonthlySummaryUseCase
    private lateinit var getSavingsProgressUseCase: GetSavingsProgressUseCase
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private lateinit var updateExpenseUseCase: UpdateExpenseUseCase
    private lateinit var getCategoryByIdUseCase: GetCategoryByIdUseCase
    private lateinit var getTotalIncomeByMonthUseCase: GetTotalIncomeByMonthUseCase
    private lateinit var viewModel: MonthlySummaryViewModel

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        fakeIncomeRepository = FakeIncomeRepository()
        fakeSavingGoalRepository = FakeSavingGoalRepository()
        
        getMonthlySummaryUseCase = GetMonthlySummaryUseCase(
            fakeBudgetRepository,
            fakeExpenseRepository,
            fakeCategoryRepository,
            fakeIncomeRepository
        )
        getSavingsProgressUseCase = GetSavingsProgressUseCase(
            fakeBudgetRepository,
            fakeExpenseRepository,
            fakeIncomeRepository,
            fakeSavingGoalRepository
        )
        deleteExpenseUseCase = DeleteExpenseUseCase(fakeExpenseRepository)
        updateExpenseUseCase = UpdateExpenseUseCase(fakeExpenseRepository)
        getCategoryByIdUseCase = GetCategoryByIdUseCase(fakeCategoryRepository)
        getTotalIncomeByMonthUseCase = GetTotalIncomeByMonthUseCase(fakeIncomeRepository)
    }

    @Test
    fun `初期化時に当月のサマリーを読み込むこと`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val budget = Budget("b1", currentMonth, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val expense = Expense("e1", "$currentMonth-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(currentMonth, state.month)
        assertEquals(50000, state.budget)
        assertEquals(1000, state.totalExpense)
        assertEquals(49000, state.remainingBudget)
        assertEquals(1, state.expenseItems.size)
        assertEquals(1, state.categoryTotals.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `予算未設定でもサマリーが表示されること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        val expense = Expense("e1", "$currentMonth-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.budget)
        assertEquals(1000, state.totalExpense)
        assertNull(state.remainingBudget)
    }

    @Test
    fun `カテゴリ別の集計が正確に表示されること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        
        val cat1 = Category("cat1", "食費", null, "", "")
        val cat2 = Category("cat2", "交通費", null, "", "")
        fakeCategoryRepository.addCategory(cat1)
        fakeCategoryRepository.addCategory(cat2)
        
        val expense1 = Expense("e1", "$currentMonth-01", 1000, "cat1", null, null, false, "", "")
        val expense2 = Expense("e2", "$currentMonth-02", 500, "cat1", null, null, false, "", "")
        val expense3 = Expense("e3", "$currentMonth-03", 2000, "cat2", null, null, false, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(expense1, expense2, expense3))

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.categoryTotals.size)
        
        // 合計金額の多い順にソート
        assertEquals("交通費", state.categoryTotals[0].categoryName)
        assertEquals(2000, state.categoryTotals[0].totalAmount)
        assertEquals("食費", state.categoryTotals[1].categoryName)
        assertEquals(1500, state.categoryTotals[1].totalAmount)
    }

    @Test
    fun `月を変更するとサマリーが更新されること`() = runTest {
        // Given
        val jan = "2026-01"
        val feb = "2026-02"
        
        fakeBudgetRepository.addBudget(Budget("b1", jan, 30000, "", "2026-01-01T00:00:00"))
        fakeBudgetRepository.addBudget(Budget("b2", feb, 50000, "", "2026-02-01T00:00:00"))
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        fakeExpenseRepository.expenses.add(Expense("e1", "$jan-01", 1000, "cat1", null, null, false, "", ""))
        fakeExpenseRepository.expenses.add(Expense("e2", "$feb-01", 2000, "cat1", null, null, false, "", ""))
        
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.onMonthChange(jan)
        advanceUntilIdle()

        // Then
        val state1 = viewModel.uiState.value
        assertEquals(jan, state1.month)
        assertEquals(50000, state1.budget)
        assertEquals(1000, state1.totalExpense)

        // When
        viewModel.onMonthChange(feb)
        advanceUntilIdle()

        // Then
        val state2 = viewModel.uiState.value
        assertEquals(feb, state2.month)
        assertEquals(50000, state2.budget)
        assertEquals(2000, state2.totalExpense)
    }

    @Test
    fun `支出がない月でも正常に表示されること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val budget = Budget("b1", currentMonth, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(50000, state.budget)
        assertEquals(0, state.totalExpense)
        assertEquals(50000, state.remainingBudget)
        assertTrue(state.categoryTotals.isEmpty())
        assertTrue(state.expenseItems.isEmpty())
    }

    @Test
    fun `カテゴリTop3とその他が正しく算出されること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

        val cat1 = Category("cat1", "食費", null, "", "")
        val cat2 = Category("cat2", "交通費", null, "", "")
        val cat3 = Category("cat3", "日用品", null, "", "")
        val cat4 = Category("cat4", "趣味", null, "", "")
        fakeCategoryRepository.addCategory(cat1)
        fakeCategoryRepository.addCategory(cat2)
        fakeCategoryRepository.addCategory(cat3)
        fakeCategoryRepository.addCategory(cat4)

        fakeExpenseRepository.expenses.addAll(
            listOf(
                Expense("e1", "$currentMonth-01", 1000, "cat1", null, null, false, "", ""),
                Expense("e2", "$currentMonth-02", 3000, "cat2", null, null, false, "", ""),
                Expense("e3", "$currentMonth-03", 2000, "cat3", null, null, false, "", ""),
                Expense("e4", "$currentMonth-04", 500, "cat4", null, null, false, "", "")
            )
        )

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.topCategories.size)
        assertEquals("交通費", state.topCategories[0].categoryName)
        assertEquals(3000, state.topCategories[0].totalAmount)
        assertEquals("日用品", state.topCategories[1].categoryName)
        assertEquals(2000, state.topCategories[1].totalAmount)
        assertEquals("食費", state.topCategories[2].categoryName)
        assertEquals(1000, state.topCategories[2].totalAmount)
        assertEquals(500, state.otherTotal)
    }

    @Test
    fun `支出を更新すると月次サマリーが再読込されること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        fakeExpenseRepository.expenses.add(
            Expense("e1", "$currentMonth-01", 1000, "cat1", null, null, false, "", "")
        )

        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        val target = viewModel.uiState.value.expenseItems.first()

        // When
        viewModel.onUpdateExpense(
            expenseItem = target,
            date = "$currentMonth-02",
            amount = 1500,
            memo = "更新メモ"
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1500, state.totalExpense)
        assertEquals("$currentMonth-02", state.expenseItems.first().date)
        assertEquals(1500, state.expenseItems.first().amount)
        assertEquals("更新メモ", state.expenseItems.first().memo)
    }

    @Test
    fun `空月の判定が正しく行われること`() = runTest {
        // Given
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEmptyMonth)
    }

    @Test
    fun `臨時収入が残予算に反映されること`() = runTest {
        // Given
        val month = "2026-02"
        val budget = Budget("b1", month, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)

        val expense = Expense("e1", "2026-02-01", 30000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        val income = com.tinygc.okodukai.domain.model.Income("inc1", "2026-02-10", 20000, "ボーナス", "", "")
        fakeIncomeRepository.incomes.add(income)

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        viewModel.onMonthChange(month)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        // 残額 = 予算 - 支出 + 臨時収入 = 50000 - 30000 + 20000 = 40000
        assertEquals(40000, state.remainingBudget)
    }

    @Test
    fun `繰越があっても予算表示は固定額のままであること`() = runTest {
        // Given
        val budget = Budget("b1", "2026-01", 50000, "2026-01-01T00:00:00", "2026-01-01T00:00:00")
        fakeBudgetRepository.addBudget(budget)

        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)

        // 1月: 支出 30000 + 臨時収入 20000 => 残額 40000
        fakeExpenseRepository.expenses.add(
            Expense("e1", "2026-01-10", 30000, "cat1", null, null, false, "", "")
        )
        fakeIncomeRepository.incomes.add(
            com.tinygc.okodukai.domain.model.Income("inc1", "2026-01-20", 20000, "ボーナス", "", "")
        )

        // When
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            updateExpenseUseCase,
            getCategoryByIdUseCase,
            getSavingsProgressUseCase,
            getTotalIncomeByMonthUseCase
        )
        viewModel.onMonthChange("2026-02")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(50000, state.budget)
        assertEquals(90000, state.carryOverBalance)
    }
}

