package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.MainDispatcherRule
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.budget.GetBudgetByMonthUseCase
import com.tinygc.okodukai.domain.usecase.budget.SaveBudgetUseCase
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
 * BudgetSettingViewModel のテスト
 * 
 * 検証項目（FR-1: 月単位の予算管理）：
 * - 予算が正確に保存されること
 * - 予算の読み込みが正常に動作すること
 * - エラーハンドリングが適切であること
 * - 月の切り替えが正常に動作すること
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetSettingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var getBudgetByMonthUseCase: GetBudgetByMonthUseCase
    private lateinit var saveBudgetUseCase: SaveBudgetUseCase
    private lateinit var viewModel: BudgetSettingViewModel

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        getBudgetByMonthUseCase = GetBudgetByMonthUseCase(fakeBudgetRepository)
        saveBudgetUseCase = SaveBudgetUseCase(fakeBudgetRepository)
    }

    @Test
    fun `初期化時に当月の予算を読み込むこと`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val budget = Budget("b1", currentMonth, 50000, "", "")
        fakeBudgetRepository.addBudget(budget)

        // When
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(currentMonth, state.month)
        assertEquals(50000, state.currentBudget)
        assertEquals("50000", state.budgetAmount)
        assertFalse(state.isLoading)
    }

    @Test
    fun `予算未設定の月を読み込むこと`() = runTest {
        // When
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.currentBudget)
        assertEquals("", state.budgetAmount)
    }

    @Test
    fun `予算を保存できること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onBudgetAmountChange("50000")
        viewModel.saveBudget()
        
        // 保存完了を待つ（3秒のdelay前に確認）
        testScheduler.advanceTimeBy(100)

        // Then
        val state = viewModel.uiState.value
        assertEquals(50000, state.currentBudget)
        assertEquals("予算を保存しました", state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun `無効な金額で保存しようとするとエラーになること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onBudgetAmountChange("0")
        viewModel.saveBudget()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("予算は0より大きい数値を入力してください", state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `数値以外の入力で保存しようとするとエラーになること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onBudgetAmountChange("abc")
        viewModel.saveBudget()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("予算は0より大きい数値を入力してください", state.errorMessage)
    }

    @Test
    fun `月を切り替えると対応する予算が読み込まれること`() = runTest {
        // Given
        val jan = "2026-01"
        val feb = "2026-02"
        fakeBudgetRepository.addBudget(Budget("b1", jan, 30000, "", ""))
        fakeBudgetRepository.addBudget(Budget("b2", feb, 50000, "", ""))
        
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onMonthChange(jan)
        advanceUntilIdle()

        // Then
        val state1 = viewModel.uiState.value
        assertEquals(jan, state1.month)
        assertEquals(30000, state1.currentBudget)

        // When
        viewModel.onMonthChange(feb)
        advanceUntilIdle()

        // Then
        val state2 = viewModel.uiState.value
        assertEquals(feb, state2.month)
        assertEquals(50000, state2.currentBudget)
    }

    @Test
    fun `予算額を変更するとUIStateが更新されること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onBudgetAmountChange("30000")

        // Then
        val state = viewModel.uiState.value
        assertEquals("30000", state.budgetAmount)
    }

    @Test
    fun `既存の予算を更新できること`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        fakeBudgetRepository.addBudget(Budget("b1", currentMonth, 30000, "", ""))
        
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // When
        viewModel.onBudgetAmountChange("50000")
        viewModel.saveBudget()
        
        // 保存完了を待つ（3秒のdelay前に確認）
        testScheduler.advanceTimeBy(100)

        // Then
        val state = viewModel.uiState.value
        assertEquals(50000, state.currentBudget)
        assertEquals("予算を保存しました", state.successMessage)
    }

    @Test
    fun `複数の月で独立して予算を管理できること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // 1月の予算を保存
        viewModel.onMonthChange("2026-01")
        advanceUntilIdle()
        viewModel.onBudgetAmountChange("30000")
        viewModel.saveBudget()
        advanceUntilIdle()

        // 2月の予算を保存
        viewModel.onMonthChange("2026-02")
        advanceUntilIdle()
        viewModel.onBudgetAmountChange("40000")
        viewModel.saveBudget()
        advanceUntilIdle()

        // Then: 保存が成功していること
        assertEquals(2, fakeBudgetRepository.savedBudgets.size)
        assertEquals(30000, fakeBudgetRepository.savedBudgets["2026-01"]?.amount)
        assertEquals(40000, fakeBudgetRepository.savedBudgets["2026-02"]?.amount)
    }
}
