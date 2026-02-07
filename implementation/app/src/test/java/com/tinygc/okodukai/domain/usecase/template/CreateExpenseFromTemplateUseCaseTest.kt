package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.usecase.expense.AddExpenseUseCase
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * CreateExpenseFromTemplateUseCase のテスト
 * 
 * 検証項目（FR-9: テンプレ管理）：
 * - テンプレから支出が正確に作成されること
 * - テンプレートの金額・カテゴリ情報が引き継がれること
 * - 日付を指定できること
 */
class CreateExpenseFromTemplateUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var createExpenseFromTemplateUseCase: CreateExpenseFromTemplateUseCase

    @Before
    fun setUp() {
        fakeExpenseRepository = FakeExpenseRepository()
        addExpenseUseCase = AddExpenseUseCase(fakeExpenseRepository)
        createExpenseFromTemplateUseCase = CreateExpenseFromTemplateUseCase(addExpenseUseCase)
    }

    @Test
    fun `テンプレから支出を作成できること`() = runTest {
        // Given
        val categoryId = "cat1"
        val amount = 1000
        val date = "2026-02-07"

        // When
        val result = createExpenseFromTemplateUseCase(categoryId, null, amount, date, null)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeExpenseRepository.expenses.size)
        val expense = fakeExpenseRepository.expenses.first()
        assertEquals(date, expense.date)
        assertEquals(1000, expense.amount)
        assertEquals("cat1", expense.categoryId)
        assertNull(expense.subCategoryId)
        assertFalse(expense.isUncategorized)
    }

    @Test
    fun `サブカテゴリ付きテンプレから支出を作成できること`() = runTest {
        // Given
        val categoryId = "cat1"
        val subCategoryId = "subcat1"
        val amount = 600
        val date = "2026-02-07"

        // When
        val result = createExpenseFromTemplateUseCase(categoryId, subCategoryId, amount, date, null)

        // Then
        assertTrue(result.isSuccess)
        val expense = fakeExpenseRepository.expenses.first()
        assertEquals("cat1", expense.categoryId)
        assertEquals("subcat1", expense.subCategoryId)
    }

    @Test
    fun `メモを追加して支出を作成できること`() = runTest {
        // Given
        val categoryId = "cat1"
        val amount = 1000
        val date = "2026-02-07"
        val memo = "会社近くのカフェ"

        // When
        val result = createExpenseFromTemplateUseCase(categoryId, null, amount, date, memo)

        // Then
        assertTrue(result.isSuccess)
        val expense = fakeExpenseRepository.expenses.first()
        assertEquals(memo, expense.memo)
    }

    @Test
    fun `同じテンプレから複数の支出を作成できること`() = runTest {
        // Given
        val categoryId = "cat1"
        val amount = 1000

        // When
        createExpenseFromTemplateUseCase(categoryId, null, amount, "2026-02-07", null)
        createExpenseFromTemplateUseCase(categoryId, null, amount, "2026-02-08", null)
        createExpenseFromTemplateUseCase(categoryId, null, amount, "2026-02-09", null)

        // Then
        assertEquals(3, fakeExpenseRepository.expenses.size)
        assertEquals("2026-02-07", fakeExpenseRepository.expenses[0].date)
        assertEquals("2026-02-08", fakeExpenseRepository.expenses[1].date)
        assertEquals("2026-02-09", fakeExpenseRepository.expenses[2].date)
    }
}
