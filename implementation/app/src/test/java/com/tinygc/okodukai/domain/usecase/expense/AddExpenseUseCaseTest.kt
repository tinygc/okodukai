package com.tinygc.okodukai.domain.usecase.expense

import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AddExpenseUseCase のテスト
 * 
 * 検証項目（FR-2: 支出記録）：
 * - 支出が正確に保存されること
 * - 金額が正の整数であること
 * - 日付が必須であること
 * - カテゴリがnullの場合は未分類フラグがtrueになること
 */
class AddExpenseUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var addExpenseUseCase: AddExpenseUseCase

    @Before
    fun setUp() {
        fakeExpenseRepository = FakeExpenseRepository()
        addExpenseUseCase = AddExpenseUseCase(fakeExpenseRepository)
    }

    @Test
    fun `正常な支出を追加できること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 1000
        val categoryId = "cat1"
        val memo = "ランチ"

        // When
        val result = addExpenseUseCase(date, amount, categoryId, null, memo)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeExpenseRepository.expenses.size)
        val savedExpense = fakeExpenseRepository.expenses.first()
        assertEquals(date, savedExpense.date)
        assertEquals(amount, savedExpense.amount)
        assertEquals(categoryId, savedExpense.categoryId)
        assertEquals(memo, savedExpense.memo)
        assertFalse(savedExpense.isUncategorized)
    }

    @Test
    fun `カテゴリがnullの場合は未分類フラグがtrueになること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 1000

        // When
        val result = addExpenseUseCase(date, amount, null, null, null)

        // Then
        assertTrue(result.isSuccess)
        val savedExpense = fakeExpenseRepository.expenses.first()
        assertNull(savedExpense.categoryId)
        assertTrue(savedExpense.isUncategorized)
    }

    @Test
    fun `金額が0の場合はエラーになること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 0

        // When
        val result = addExpenseUseCase(date, amount, null, null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `金額が負の値の場合はエラーになること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = -500

        // When
        val result = addExpenseUseCase(date, amount, null, null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `日付が空の場合はエラーになること`() = runTest {
        // Given
        val date = ""
        val amount = 1000

        // When
        val result = addExpenseUseCase(date, amount, null, null, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("日付を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `サブカテゴリを指定して支出を追加できること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 1500
        val categoryId = "cat1"
        val subCategoryId = "subcat1"

        // When
        val result = addExpenseUseCase(date, amount, categoryId, subCategoryId, null)

        // Then
        assertTrue(result.isSuccess)
        val savedExpense = fakeExpenseRepository.expenses.first()
        assertEquals(categoryId, savedExpense.categoryId)
        assertEquals(subCategoryId, savedExpense.subCategoryId)
    }
}

/**
 * テスト用のFakeExpenseRepository
 */
class FakeExpenseRepository : ExpenseRepository {
    val expenses = mutableListOf<Expense>()

    override suspend fun saveExpense(expense: Expense): Result<Unit> {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            expenses[index] = expense
        } else {
            expenses.add(expense)
        }
        return Result.success(Unit)
    }

    override suspend fun getExpenseById(id: String): Result<Expense?> {
        return Result.success(expenses.find { it.id == id })
    }

    override suspend fun deleteExpense(expense: Expense): Result<Unit> {
        expenses.remove(expense)
        return Result.success(Unit)
    }

    override suspend fun getExpensesByMonth(month: String): Result<List<Expense>> {
        return Result.success(expenses.filter { it.date.startsWith(month) })
    }

    override suspend fun getCategorizedExpensesByMonth(month: String): Result<List<Expense>> {
        return Result.success(
            expenses.filter { it.date.startsWith(month) && it.categoryId != null }
        )
    }

    override fun observeUncategorizedExpensesByMonth(month: String): Flow<List<Expense>> {
        return flowOf(expenses.filter { it.date.startsWith(month) && it.isUncategorized })
    }

    override suspend fun getTotalExpenseByMonth(month: String): Result<Int> {
        val total = expenses
            .filter { it.date.startsWith(month) && it.categoryId != null }
            .sumOf { it.amount }
        return Result.success(total)
    }

    override fun observeExpensesByMonth(month: String): Flow<List<Expense>> {
        return flowOf(expenses.filter { it.date.startsWith(month) })
    }

    override fun observeTotalExpenseByMonth(month: String): Flow<Int> {
        val total = expenses
            .filter { it.date.startsWith(month) && it.categoryId != null }
            .sumOf { it.amount }
        return flowOf(total)
    }

    override suspend fun getAllExpenses(): Result<List<Expense>> {
        return Result.success(expenses)
    }
}
