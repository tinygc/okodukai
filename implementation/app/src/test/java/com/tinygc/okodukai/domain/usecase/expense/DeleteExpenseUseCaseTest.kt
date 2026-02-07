package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * DeleteExpenseUseCase のテスト
 * 
 * 検証項目（FR-2: 支出記録）：
 * - 支出が正確に削除されること
 * - 存在しない支出IDの場合の処理
 */
class DeleteExpenseUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase

    @Before
    fun setUp() {
        fakeExpenseRepository = FakeExpenseRepository()
        deleteExpenseUseCase = DeleteExpenseUseCase(fakeExpenseRepository)
    }

    @Test
    fun `支出を削除できること`() = runTest {
        // Given
        val expense1 = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        val expense2 = Expense("e2", "2026-02-02", 2000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(expense1, expense2))

        // When
        val result = deleteExpenseUseCase(expense1)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeExpenseRepository.expenses.size)
        assertEquals("e2", fakeExpenseRepository.expenses.first().id)
    }

    @Test
    fun `存在しない支出でもエラーにならないこと`() = runTest {
        // Given
        val nonExistentExpense = Expense("nonexistent", "2026-02-01", 1000, "cat1", null, null, false, "", "")

        // When
        val result = deleteExpenseUseCase(nonExistentExpense)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `複数の支出を順次削除できること`() = runTest {
        // Given
        val expense1 = Expense("e1", "2026-02-01", 1000, "cat1", null, null, false, "", "")
        val expense2 = Expense("e2", "2026-02-02", 2000, "cat1", null, null, false, "", "")
        val expense3 = Expense("e3", "2026-02-03", 3000, "cat1", null, null, false, "", "")
        fakeExpenseRepository.expenses.addAll(listOf(expense1, expense2, expense3))

        // When
        deleteExpenseUseCase(expense1)
        deleteExpenseUseCase(expense3)

        // Then
        assertEquals(1, fakeExpenseRepository.expenses.size)
        assertEquals("e2", fakeExpenseRepository.expenses.first().id)
    }
}
