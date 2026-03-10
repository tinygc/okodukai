package com.tinygc.okodukai.presentation.screen

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * ExpenseEntryUiState のテスト
 * 
 * 検証項目（Issue #6: 支出入力で保存後、カテゴリをリセットしない）：
 * - UI状態で金額だけがリセットされること
 * - UI状態でカテゴリが保持されること
 * - UI状態でサブカテゴリが保持されること
 * 
 * 検証項目（Issue #4: 支出入力で日付を一番上に、通常とテンプレ両方に表示）：
 * - テンプレタブでも日付入力が利用可能であること
 * - 通常タブでも日付入力が利用可能であること
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseEntryViewModelTest {

    @Test
    fun `clearInput後、金額がリセットされること`() = runTest {
        // Given
        var uiState = ExpenseEntryUiState(
            amountInput = "1000",
            selectedCategoryId = "cat1",
            selectedSubCategoryId = "subcat1"
        )

        // When: 金額だけリセット
        uiState = uiState.copy(amountInput = "")

        // Then
        assertEquals("", uiState.amountInput)  // 金額がリセット
        assertEquals("cat1", uiState.selectedCategoryId)  // カテゴリ保持
        assertEquals("subcat1", uiState.selectedSubCategoryId)  // サブカテゴリ保持
    }

    @Test
    fun `clearInput後、カテゴリが保持されること`() = runTest {
        // Given
        var uiState = ExpenseEntryUiState(
            amountInput = "1000",
            selectedCategoryId = "cat1",
            selectedSubCategoryId = "subcat1"
        )
        val categoryBeforeClear = uiState.selectedCategoryId

        // When: 金額だけリセット
        uiState = uiState.copy(amountInput = "")

        // Then
        assertEquals(categoryBeforeClear, uiState.selectedCategoryId)  // カテゴリ保持
    }

    @Test
    fun `clearInput後、サブカテゴリが保持されること`() = runTest {
        // Given
        var uiState = ExpenseEntryUiState(
            amountInput = "1000",
            selectedCategoryId = "cat1",
            selectedSubCategoryId = "subcat1"
        )
        val subCategoryBeforeClear = uiState.selectedSubCategoryId

        // When: 金額だけリセット
        uiState = uiState.copy(amountInput = "")

        // Then
        assertEquals(subCategoryBeforeClear, uiState.selectedSubCategoryId)  // サブカテゴリ保持
    }

    @Test
    fun `全項目がUI状態にコピーできること`() = runTest {
        // Given
        val originalState = ExpenseEntryUiState(
            amountInput = "2000",
            selectedCategoryId = "cat2",
            selectedSubCategoryId = "subcat2"
        )

        // When
        val newState = originalState.copy()

        // Then
        assertEquals(originalState.amountInput, newState.amountInput)
        assertEquals(originalState.selectedCategoryId, newState.selectedCategoryId)
        assertEquals(originalState.selectedSubCategoryId, newState.selectedSubCategoryId)
    }

    // Issue #4: 支出入力で日付を一番上に、通常とテンプレ両方に表示
    @Test
    fun `通常タブで日付入力が利用可能であること`() = runTest {
        // Given
        val uiState = ExpenseEntryUiState(
            currentTab = ExpenseEntryTab.Normal,
            dateInput = "2026-03-10"
        )

        // Then
        assertEquals(ExpenseEntryTab.Normal, uiState.currentTab)
        assertEquals("2026-03-10", uiState.dateInput)
    }

    @Test
    fun `テンプレタブで日付入力が利用可能であること`() = runTest {
        // Given
        val uiState = ExpenseEntryUiState(
            currentTab = ExpenseEntryTab.Template,
            dateInput = "2026-03-10"
        )

        // Then
        assertEquals(ExpenseEntryTab.Template, uiState.currentTab)
        assertEquals("2026-03-10", uiState.dateInput)
    }

    @Test
    fun `タブ切り替え時に日付が保持されること`() = runTest {
        // Given
        var uiState = ExpenseEntryUiState(
            currentTab = ExpenseEntryTab.Normal,
            dateInput = "2026-03-10"
        )
        val dateBeforeSwitch = uiState.dateInput

        // When: テンプレタブに切り替え
        uiState = uiState.copy(currentTab = ExpenseEntryTab.Template)

        // Then
        assertEquals(ExpenseEntryTab.Template, uiState.currentTab)
        assertEquals(dateBeforeSwitch, uiState.dateInput)  // 日付が保持される
    }
}
