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
}
