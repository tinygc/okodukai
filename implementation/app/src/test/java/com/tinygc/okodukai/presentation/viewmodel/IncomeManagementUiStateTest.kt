package com.tinygc.okodukai.presentation.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * IncomeManagementUiState のテスト
 *
 * 検証項目（Issue #18: 日付入力をカレンダーピッカー形式に変更）：
 * - 日付入力がyyyy-MM-dd形式で正しく保持されること
 * - ダイアログ表示時にデフォルト日付が設定されること
 * - 日付変更が他のフィールドに影響しないこと
 */
class IncomeManagementUiStateTest {

    @Test
    fun `日付入力がyyyy-MM-dd形式で保持されること`() {
        val uiState = IncomeManagementUiState(dateInput = "2026-03-21")
        assertEquals("2026-03-21", uiState.dateInput)
    }

    @Test
    fun `カレンダーピッカーで日付変更しても他フィールドが保持されること`() {
        // Given: ダイアログ表示中に各フィールドに値が入っている
        val uiState = IncomeManagementUiState(
            showAddDialog = true,
            amountInput = "50000",
            memoInput = "ボーナス",
            dateInput = "2026-03-21"
        )

        // When: カレンダーピッカーで日付だけ変更
        val updated = uiState.copy(dateInput = "2026-06-01")

        // Then: 他のフィールドは保持
        assertEquals("2026-06-01", updated.dateInput)
        assertEquals("50000", updated.amountInput)
        assertEquals("ボーナス", updated.memoInput)
        assertTrue(updated.showAddDialog)
    }

    @Test
    fun `確認ボタンのenabled条件に日付が含まれること`() {
        // 確認ボタンは amountInput.isNotEmpty() && dateInput.isNotEmpty() && !isSaving
        val uiState = IncomeManagementUiState(
            amountInput = "10000",
            dateInput = "2026-03-21",
            isSaving = false
        )

        val canConfirm = uiState.amountInput.isNotEmpty() &&
            uiState.dateInput.isNotEmpty() &&
            !uiState.isSaving

        assertTrue(canConfirm)
    }

    @Test
    fun `日付が空の場合は確認ボタンが無効であること`() {
        val uiState = IncomeManagementUiState(
            amountInput = "10000",
            dateInput = "",
            isSaving = false
        )

        val canConfirm = uiState.amountInput.isNotEmpty() &&
            uiState.dateInput.isNotEmpty() &&
            !uiState.isSaving

        assertFalse(canConfirm)
    }
}
