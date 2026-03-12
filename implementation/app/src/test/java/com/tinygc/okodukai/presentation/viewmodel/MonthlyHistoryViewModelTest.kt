package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.MainDispatcherRule
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import com.tinygc.okodukai.domain.usecase.expense.GetExpensesByMonthUseCase
import com.tinygc.okodukai.domain.usecase.income.FakeIncomeRepository
import com.tinygc.okodukai.domain.usecase.income.GetTotalIncomeByMonthUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var getBudgetByMonthUseCase: GetBudgetByMonthUseCase
    private lateinit var getExpensesByMonthUseCase: GetExpensesByMonthUseCase
    private lateinit var getTotalIncomeByMonthUseCase: GetTotalIncomeByMonthUseCase

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeIncomeRepository = FakeIncomeRepository()

        getBudgetByMonthUseCase = GetBudgetByMonthUseCase(fakeBudgetRepository)
        getExpensesByMonthUseCase = GetExpensesByMonthUseCase(fakeExpenseRepository)
        getTotalIncomeByMonthUseCase = GetTotalIncomeByMonthUseCase(fakeIncomeRepository)
    }

    @Test
    fun `臨時収入が当月残額に反映されること`() = runTest {
        // Given
        val currentMonth = YearMonth.now().toString()

        fakeBudgetRepository.addBudget(
            Budget(
                id = "b1",
                month = currentMonth,
                amount = 50000,
                createdAt = "$currentMonth-01T00:00:00",
                updatedAt = "$currentMonth-01T00:00:00"
            )
        )

        fakeExpenseRepository.expenses.add(
            Expense(
                id = "e1",
                date = "$currentMonth-10",
                amount = 30000,
                categoryId = "cat1",
                subCategoryId = null,
                memo = null,
                isUncategorized = false,
                createdAt = "",
                updatedAt = ""
            )
        )
        fakeIncomeRepository.incomes.add(
            Income(
                id = "i1",
                date = "$currentMonth-20",
                amount = 20000,
                memo = "bonus",
                createdAt = "",
                updatedAt = ""
            )
        )

        // When
        val viewModel = MonthlyHistoryViewModel(
            getBudgetByMonthUseCase,
            getExpensesByMonthUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val current = viewModel.uiState.value.histories.find { it.month == currentMonth }
        assertNotNull(current)
        assertEquals(20000, current?.totalIncome)
        assertEquals(40000, current?.remainingBudget) // 50000 - 30000 + 20000
    }

    @Test
    fun `臨時収入が翌月の繰越に反映されること`() = runTest {
        // Given
        val previousMonth = YearMonth.now().minusMonths(1).toString()
        val currentMonth = YearMonth.now().toString()

        fakeBudgetRepository.addBudget(
            Budget(
                id = "b1",
                month = currentMonth,
                amount = 50000,
                createdAt = "$currentMonth-01T00:00:00",
                updatedAt = "$currentMonth-01T00:00:00"
            )
        )

        fakeExpenseRepository.expenses.add(
            Expense(
                id = "e1",
                date = "$previousMonth-10",
                amount = 30000,
                categoryId = "cat1",
                subCategoryId = null,
                memo = null,
                isUncategorized = false,
                createdAt = "",
                updatedAt = ""
            )
        )
        fakeIncomeRepository.incomes.add(
            Income(
                id = "i1",
                date = "$previousMonth-20",
                amount = 20000,
                memo = "bonus",
                createdAt = "",
                updatedAt = ""
            )
        )

        // When
        val viewModel = MonthlyHistoryViewModel(
            getBudgetByMonthUseCase,
            getExpensesByMonthUseCase,
            getTotalIncomeByMonthUseCase
        )
        advanceUntilIdle()

        // Then
        val current = viewModel.uiState.value.histories.find { it.month == currentMonth }
        assertNotNull(current)
        assertEquals(90000, current?.budget) // 50000 + (50000 - 30000 + 20000)
    }
}
