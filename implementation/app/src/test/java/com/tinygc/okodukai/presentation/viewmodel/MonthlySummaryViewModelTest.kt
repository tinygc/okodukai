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
    private lateinit var getMonthlySummaryUseCase: GetMonthlySummaryUseCase
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private lateinit var getCategoryByIdUseCase: GetCategoryByIdUseCase
    private lateinit var viewModel: MonthlySummaryViewModel

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
        deleteExpenseUseCase = DeleteExpenseUseCase(fakeExpenseRepository)
        getCategoryByIdUseCase = GetCategoryByIdUseCase(fakeCategoryRepository)
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
            getCategoryByIdUseCase
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
            getCategoryByIdUseCase
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
            getCategoryByIdUseCase
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
        
        fakeBudgetRepository.addBudget(Budget("b1", jan, 30000, "", ""))
        fakeBudgetRepository.addBudget(Budget("b2", feb, 50000, "", ""))
        
        val category = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(category)
        
        fakeExpenseRepository.expenses.add(Expense("e1", "$jan-01", 1000, "cat1", null, null, false, "", ""))
        fakeExpenseRepository.expenses.add(Expense("e2", "$feb-01", 2000, "cat1", null, null, false, "", ""))
        
        viewModel = MonthlySummaryViewModel(
            getMonthlySummaryUseCase,
            deleteExpenseUseCase,
            getCategoryByIdUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.onMonthChange(jan)
        advanceUntilIdle()

        // Then
        val state1 = viewModel.uiState.value
        assertEquals(jan, state1.month)
        assertEquals(30000, state1.budget)
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
            getCategoryByIdUseCase
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
}
