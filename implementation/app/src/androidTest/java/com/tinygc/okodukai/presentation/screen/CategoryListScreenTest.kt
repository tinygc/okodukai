package com.tinygc.okodukai.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.tinygc.okodukai.presentation.viewmodel.CategoryTotalUiModel
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryUiState
import org.junit.Rule
import org.junit.Test

class CategoryListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateMessageIsDisplayed() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            categoryTotals = emptyList()
        )

        composeRule.setContent {
            MaterialTheme {
                CategoryListContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("データがありません").assertIsDisplayed()
    }

    @Test
    fun errorMessageIsDisplayedWhenLoadingFails() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            errorMessage = "dummy"
        )

        composeRule.setContent {
            MaterialTheme {
                CategoryListContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("データ取得に失敗しました").assertIsDisplayed()
    }

    @Test
    fun categoryNamesAreDisplayed() {
        val uiState = MonthlySummaryUiState(
            month = "2026-02",
            categoryTotals = listOf(
                CategoryTotalUiModel("c1", "食費", 3000),
                CategoryTotalUiModel("c2", "交通費", 2000)
            )
        )

        composeRule.setContent {
            MaterialTheme {
                CategoryListContent(
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    uiState = uiState,
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("食費").assertIsDisplayed()
        composeRule.onNodeWithText("交通費").assertIsDisplayed()
    }
}
