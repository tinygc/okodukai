package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UpdateExpenseUseCase のテスト
 * 
 * 検証項目（FR-2: 支出記録）：
 * - 支出が正確に更新されること
 * - 金額が正の整数であること
 * - 日付が必須であること
 * - 履歴が正確に更新されること
 */
class UpdateExpenseUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var updateExpenseUseCase: UpdateExpenseUseCase

    @Before
    fun setUp() {
        fakeExpenseRepository = FakeExpenseRepository()
        updateExpenseUseCase = UpdateExpenseUseCase(fakeExpenseRepository)
    }

    @Test
    fun `既存の支出を更新できること`() = runTest {
        // Given
        val originalExpense = Expense("e1", "2026-02-01", 1000, "cat1", null, "ランチ", false, "2026-02-01T12:00:00", "2026-02-01T12:00:00")
        fakeExpenseRepository.expenses.add(originalExpense)
        
        val newDate = "2026-02-05"
        val newAmount = 1500
        val newMemo = "ディナー"

        // When
        val result = updateExpenseUseCase("e1", newDate, newAmount, "cat1", null, newMemo)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeExpenseRepository.expenses.size)
        val updatedExpense = fakeExpenseRepository.expenses.first()
        assertEquals("e1", updatedExpense.id)
        assertEquals(newDate, updatedExpense.date)
        assertEquals(newAmount, updatedExpense.amount)
        assertEquals(newMemo, updatedExpense.memo)
    }

    @Test
    fun `金額が0の場合はエラーになること`() = runTest {
        // Given
        val expense = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        val result = updateExpenseUseCase("e1", "2026-02-01", 0, "cat1", null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `日付が空の場合はエラーになること`() = runTest {
        // Given
        val expense = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        val result = updateExpenseUseCase("e1", "", 1000, "cat1", null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("日付を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `存在しない支出IDの場合はエラーになること`() = runTest {
        // When
        val result = updateExpenseUseCase("nonexistent", "2026-02-01", 1000, "cat1", null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("指定された支出が見つかりません", result.exceptionOrNull()?.message)
    }

    @Test
    fun `カテゴリをnullに変更すると未分類フラグがtrueになること`() = runTest {
        // Given
        val expense = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        val result = updateExpenseUseCase("e1", "2026-02-01", 1000, null, null, null)

        // Then
        assertTrue(result.isSuccess)
        val updatedExpense = fakeExpenseRepository.expenses.first()
        assertNull(updatedExpense.categoryId)
        assertTrue(updatedExpense.isUncategorized)
    }

    @Test
    fun `未分類からカテゴリありに変更できること`() = runTest {
        // Given
        val expense = Expense("e1", "2026-02-01", 1000, null, null, null, true, "", "")
        fakeExpenseRepository.expenses.add(expense)

        // When
        val result = updateExpenseUseCase("e1", "2026-02-01", 1000, "cat1", null, null)

        // Then
        assertTrue(result.isSuccess)
        val updatedExpense = fakeExpenseRepository.expenses.first()
        assertEquals("cat1", updatedExpense.categoryId)
        assertFalse(updatedExpense.isUncategorized)
    }
}
