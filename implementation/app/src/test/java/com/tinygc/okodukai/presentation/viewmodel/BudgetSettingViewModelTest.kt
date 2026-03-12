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
 * 検証項目（FR-1: 毎月固定の予算管理）：
 * - 予算が正確に保存されること
 * - 予算の読み込みが正常に動作すること
 * - エラーハンドリングが適切であること
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
        assertEquals("毎月の予算を保存しました", state.successMessage)
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
        assertEquals("毎月の予算を保存しました", state.successMessage)
    }

    @Test
    fun `複数回保存しても固定予算として1件更新されること`() = runTest {
        // Given
        viewModel = BudgetSettingViewModel(getBudgetByMonthUseCase, saveBudgetUseCase)
        advanceUntilIdle()

        // 1回目を保存
        viewModel.onBudgetAmountChange("30000")
        viewModel.saveBudget()
        advanceUntilIdle()

        // 2回目を保存
        viewModel.onBudgetAmountChange("40000")
        viewModel.saveBudget()
        advanceUntilIdle()

        // Then: 固定予算のため1件のみ保持されること
        assertEquals(1, fakeBudgetRepository.savedBudgets.size)
        assertEquals(40000, fakeBudgetRepository.savedBudgets.values.first().amount)
    }
}
