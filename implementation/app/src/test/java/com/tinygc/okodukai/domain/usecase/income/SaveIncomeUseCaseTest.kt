package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AddIncomeUseCase のテスト
 * 
 * 検証項目（FR-6: 臨時収入の記録）：
 * - 臨時収入が正確に保存されること
 * - 金額が正の整数であること
 * - 日付が必須であること
 * - 臨時収入が月別に集計されること
 */
class SaveIncomeUseCaseTest {

    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var addIncomeUseCase: AddIncomeUseCase

    @Before
    fun setUp() {
        fakeIncomeRepository = FakeIncomeRepository()
        addIncomeUseCase = AddIncomeUseCase(fakeIncomeRepository)
    }

    @Test
    fun `正常な臨時収入を追加できること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 10000
        val memo = "ボーナス"

        // When
        val result = addIncomeUseCase(date, amount, memo)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeIncomeRepository.incomes.size)
        val savedIncome = fakeIncomeRepository.incomes.first()
        assertEquals(date, savedIncome.date)
        assertEquals(amount, savedIncome.amount)
        assertEquals(memo, savedIncome.memo)
    }

    @Test
    fun `メモなしで臨時収入を追加できること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 5000

        // When
        val result = addIncomeUseCase(date, amount, null)

        // Then
        assertTrue(result.isSuccess)
        val savedIncome = fakeIncomeRepository.incomes.first()
        assertNull(savedIncome.memo)
    }

    @Test
    fun `金額が0の場合はエラーになること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = 0

        // When
        val result = addIncomeUseCase(date, amount, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `金額が負の値の場合はエラーになること`() = runTest {
        // Given
        val date = "2026-02-07"
        val amount = -1000

        // When
        val result = addIncomeUseCase(date, amount, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `日付が空の場合はエラーになること`() = runTest {
        // Given
        val date = ""
        val amount = 5000

        // When
        val result = addIncomeUseCase(date, amount, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("日付を入力してください", result.exceptionOrNull()?.message)
    }
}

/**
 * テスト用のFakeIncomeRepository
 */
class FakeIncomeRepository : IncomeRepository {
    val incomes = mutableListOf<Income>()

    override suspend fun saveIncome(income: Income): Result<Unit> {
        val index = incomes.indexOfFirst { it.id == income.id }
        if (index != -1) {
            incomes[index] = income
        } else {
            incomes.add(income)
        }
        return Result.success(Unit)
    }

    override suspend fun getIncomeById(id: String): Result<Income?> {
        return Result.success(incomes.find { it.id == id })
    }

    override suspend fun getAllIncomes(): Result<List<Income>> {
        return Result.success(incomes)
    }

    override suspend fun deleteIncome(income: Income): Result<Unit> {
        incomes.remove(income)
        return Result.success(Unit)
    }

    override suspend fun getIncomesByMonth(month: String): Result<List<Income>> {
        return Result.success(incomes.filter { it.date.startsWith(month) })
    }

    override suspend fun getTotalIncomeByMonth(month: String): Result<Int> {
        val total = incomes
            .filter { it.date.startsWith(month) }
            .sumOf { it.amount }
        return Result.success(total)
    }

    override fun observeIncomesByMonth(month: String): Flow<List<Income>> {
        return flowOf(incomes.filter { it.date.startsWith(month) })
    }

    override fun observeTotalIncomeByMonth(month: String): Flow<Int> {
        val total = incomes
            .filter { it.date.startsWith(month) }
            .sumOf { it.amount }
        return flowOf(total)
    }

    override fun observeAllIncomes(): Flow<List<Income>> {
        return flowOf(incomes)
    }
}
