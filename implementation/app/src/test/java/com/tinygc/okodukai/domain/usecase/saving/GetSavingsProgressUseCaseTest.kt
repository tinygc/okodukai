package com.tinygc.okodukai.domain.usecase.saving

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.repository.SavingGoalRepository
import com.tinygc.okodukai.domain.usecase.budget.FakeBudgetRepository
import com.tinygc.okodukai.domain.usecase.expense.FakeExpenseRepository
import com.tinygc.okodukai.domain.usecase.income.FakeIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSavingsProgressUseCaseTest {

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var fakeSavingGoalRepository: FakeSavingGoalRepository
    private lateinit var useCase: GetSavingsProgressUseCase

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeIncomeRepository = FakeIncomeRepository()
        fakeSavingGoalRepository = FakeSavingGoalRepository()
        useCase = GetSavingsProgressUseCase(
            budgetRepository = fakeBudgetRepository,
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            savingGoalRepository = fakeSavingGoalRepository
        )
    }

    @Test
    fun `予算超過を翌月へ負債として繰越できること`() = runTest {
        fakeBudgetRepository.addBudget(Budget("b1", "2026-01", 10000, "", ""))
        fakeBudgetRepository.addBudget(Budget("b2", "2026-02", 10000, "", ""))

        fakeExpenseRepository.expenses.add(
            com.tinygc.okodukai.domain.model.Expense("e1", "2026-01-10", 12000, "cat", null, null, false, "", "")
        )
        fakeExpenseRepository.expenses.add(
            com.tinygc.okodukai.domain.model.Expense("e2", "2026-02-10", 5000, "cat", null, null, false, "", "")
        )

        val goal = SavingGoal("g1", "イヤホン", 5000, true, 0, "", "")
        fakeSavingGoalRepository.saveSavingGoal(goal)

        val result = useCase("2026-02", GoalAchievementMode.INDIVIDUAL)

        assertTrue(result.isSuccess)
        val progress = result.getOrThrow()
        assertEquals(3000, progress.carryOverBalance)
        assertEquals(3000, progress.availableAmount)
        assertEquals(2000, progress.goals.first().remainingAmount)
    }

    @Test
    fun `臨時収入を含めて合計達成判定できること`() = runTest {
        fakeBudgetRepository.addBudget(Budget("b1", "2026-02", 10000, "", ""))
        fakeExpenseRepository.expenses.add(
            com.tinygc.okodukai.domain.model.Expense("e1", "2026-02-03", 8000, "cat", null, null, false, "", "")
        )
        fakeIncomeRepository.incomes.add(
            Income("i1", "2026-02-15", 5000, null, "", "")
        )

        fakeSavingGoalRepository.saveSavingGoal(SavingGoal("g1", "キーボード", 3000, true, 0, "", ""))
        fakeSavingGoalRepository.saveSavingGoal(SavingGoal("g2", "マウス", 2000, true, 1, "", ""))

        val result = useCase("2026-02", GoalAchievementMode.TOTAL)

        assertTrue(result.isSuccess)
        val progress = result.getOrThrow()
        assertEquals(7000, progress.availableAmount)
        assertEquals(5000, progress.totalTargetAmount)
        assertEquals(0, progress.totalRemainingAmount)
        assertTrue(progress.isTotalAchieved)
    }
}

class FakeSavingGoalRepository : SavingGoalRepository {
    val goals = mutableListOf<SavingGoal>()

    override suspend fun saveSavingGoal(savingGoal: SavingGoal): Result<Unit> {
        val index = goals.indexOfFirst { it.id == savingGoal.id }
        if (index >= 0) {
            goals[index] = savingGoal
        } else {
            goals.add(savingGoal)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteSavingGoal(savingGoal: SavingGoal): Result<Unit> {
        goals.removeIf { it.id == savingGoal.id }
        return Result.success(Unit)
    }

    override suspend fun getSavingGoalById(id: String): Result<SavingGoal?> {
        return Result.success(goals.find { it.id == id })
    }

    override suspend fun getAllSavingGoals(): Result<List<SavingGoal>> {
        return Result.success(goals.sortedBy { it.displayOrder })
    }

    override fun observeAllSavingGoals(): Flow<List<SavingGoal>> {
        return flowOf(goals.sortedBy { it.displayOrder })
    }
}
