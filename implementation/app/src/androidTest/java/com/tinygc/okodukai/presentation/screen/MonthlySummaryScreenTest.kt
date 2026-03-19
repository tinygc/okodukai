package com.tinygc.okodukai.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.tinygc.okodukai.presentation.viewmodel.CategoryTotalUiModel
import com.tinygc.okodukai.presentation.viewmodel.ExpenseItem
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class MonthlySummaryScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categoryTop3AndOtherAreDisplayed() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            budget = 50000,
            totalExpense = 6500,
            remainingBudget = 43500,
            topCategories = listOf(
                CategoryTotalUiModel("c1", "食費", 3000),
                CategoryTotalUiModel("c2", "交通費", 2000),
                CategoryTotalUiModel("c3", "日用品", 1000)
            ),
            otherTotal = 500,
            expenseItems = listOf(
                ExpenseItem(
                    id = "e1",
                    date = "2026-02-01",
                    amount = 1200,
                    categoryId = "c1",
                    subCategoryId = null,
                    categoryName = "食費",
                    subCategoryName = null,
                    memo = null,
                    isUncategorized = false,
                    createdAt = "",
                    updatedAt = ""
                )
            )
        )

        composeRule.setContent {
            MaterialTheme {
                MonthlySummaryContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onBackToCurrentMonth = {},
                    showBackToCurrentMonth = false,
                    onUpdateExpense = { _, _, _, _ -> },
                    onDeleteExpense = {},
                    onShowAllCategories = {},
                    onShowAllExpenses = {}
                )
            }
        }

        assertTextDisplayed("カテゴリ別支出")
        assertTextDisplayed("食費")
        assertTextDisplayed("交通費")
        assertTextDisplayed("日用品")
        assertTextDisplayed("その他")
        assertTextDisplayed("一覧を見る ＞")
    }

    @Test
    fun emptyMonthMessagesAreDisplayed() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            budget = 0,
            totalExpense = 0,
            remainingBudget = 0,
            topCategories = emptyList(),
            otherTotal = 0,
            expenseItems = emptyList(),
            isEmptyMonth = true
        )

        composeRule.setContent {
            MaterialTheme {
                MonthlySummaryContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onBackToCurrentMonth = {},
                    showBackToCurrentMonth = false,
                    onUpdateExpense = { _, _, _, _ -> },
                    onDeleteExpense = {},
                    onShowAllCategories = {},
                    onShowAllExpenses = {}
                )
            }
        }

        assertTextDisplayed("データがありません")
        assertTextDisplayed("今月の支出はありません")
    }

    @Test
    fun editIconClickThenSaveCallsOnUpdateExpense() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            budget = 50000,
            totalExpense = 1200,
            remainingBudget = 48800,
            topCategories = listOf(
                CategoryTotalUiModel("c1", "食費", 1200)
            ),
            otherTotal = 0,
            expenseItems = listOf(
                ExpenseItem(
                    id = "e1",
                    date = "2026-02-01",
                    amount = 1200,
                    categoryId = "c1",
                    subCategoryId = null,
                    categoryName = "食費",
                    subCategoryName = null,
                    memo = null,
                    isUncategorized = false,
                    createdAt = "",
                    updatedAt = ""
                )
            )
        )

        var editedExpenseId: String? = null

        composeRule.setContent {
            MaterialTheme {
                MonthlySummaryContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onBackToCurrentMonth = {},
                    showBackToCurrentMonth = false,
                    onUpdateExpense = { expense: ExpenseItem, _: String, _: Int, _: String? -> editedExpenseId = expense.id },
                    onDeleteExpense = {},
                    onShowAllCategories = {},
                    onShowAllExpenses = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("編集").performClick()
        composeRule.onNodeWithText("保存").performClick()
        composeRule.runOnIdle {
            assertEquals("e1", editedExpenseId)
        }
    }

    @Test
    fun showAllExpensesButtonClickCallsCallback() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            budget = 50000,
            totalExpense = 1200,
            remainingBudget = 48800,
            topCategories = listOf(
                CategoryTotalUiModel("c1", "食費", 1200)
            ),
            otherTotal = 0,
            expenseItems = listOf(
                ExpenseItem(
                    id = "e1",
                    date = "2026-02-01",
                    amount = 1200,
                    categoryId = "c1",
                    subCategoryId = null,
                    categoryName = "食費",
                    subCategoryName = null,
                    memo = null,
                    isUncategorized = false,
                    createdAt = "",
                    updatedAt = ""
                )
            )
        )

        var showAllExpensesClicked = false

        composeRule.setContent {
            MaterialTheme {
                MonthlySummaryContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onBackToCurrentMonth = {},
                    showBackToCurrentMonth = false,
                    onUpdateExpense = { _, _, _, _ -> },
                    onDeleteExpense = {},
                    onShowAllCategories = {},
                    onShowAllExpenses = { showAllExpensesClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("全件表示 ＞").performClick()
        composeRule.runOnIdle {
            assertEquals(true, showAllExpensesClicked)
        }
    }

    @Test
    fun showAllCategoriesButtonClickCallsCallback() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            budget = 50000,
            totalExpense = 1200,
            remainingBudget = 48800,
            topCategories = listOf(
                CategoryTotalUiModel("c1", "食費", 1200)
            ),
            otherTotal = 0,
            expenseItems = listOf(
                ExpenseItem(
                    id = "e1",
                    date = "2026-02-01",
                    amount = 1200,
                    categoryId = "c1",
                    subCategoryId = null,
                    categoryName = "食費",
                    subCategoryName = null,
                    memo = null,
                    isUncategorized = false,
                    createdAt = "",
                    updatedAt = ""
                )
            )
        )

        var showAllCategoriesClicked = false

        composeRule.setContent {
            MaterialTheme {
                MonthlySummaryContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onBackToCurrentMonth = {},
                    showBackToCurrentMonth = false,
                    onUpdateExpense = { _, _, _, _ -> },
                    onDeleteExpense = {},
                    onShowAllCategories = { showAllCategoriesClicked = true },
                    onShowAllExpenses = {}
                )
            }
        }

        composeRule.onNodeWithText("一覧を見る ＞").performClick()
        composeRule.runOnIdle {
            assertEquals(true, showAllCategoriesClicked)
        }
    }

    private fun assertTextDisplayed(text: String) {
        composeRule.onAllNodesWithText(text)[0].assertIsDisplayed()
    }
}
