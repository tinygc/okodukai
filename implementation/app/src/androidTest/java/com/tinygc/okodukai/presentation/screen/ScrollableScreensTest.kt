package com.tinygc.okodukai.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.presentation.viewmodel.BackupManagementUiState
import com.tinygc.okodukai.presentation.viewmodel.DefaultCategorySettingUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollableScreensTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expenseEntryNormalTabCanScrollToSaveOnSmallHeight() {
        composeRule.setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(240.dp)
                ) {
                    ExpenseEntryContent(
                        paddingValues = PaddingValues(0.dp),
                        uiState = ExpenseEntryUiState(
                            currentTab = ExpenseEntryTab.Normal,
                            amountInput = "1200",
                            dateInput = "2026-03-23",
                            categories = sampleCategories(),
                            subCategories = sampleSubCategories(),
                            selectedCategoryId = "parent-1",
                            selectedSubCategoryId = "child-1"
                        ),
                        onTabSelected = {},
                        onDateChange = {},
                        onQuickAmountAdd = {},
                        onResetAmount = {},
                        onAmountChange = {},
                        onCategorySelected = {},
                        onSubCategorySelected = {},
                        onSaveExpense = {},
                        onTemplateSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.onNodeWithText("保存").assertIsNotDisplayed()

        composeRule.onNodeWithTag("expenseEntryNormalList")
            .performScrollToNode(hasText("保存"))

        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }

    @Test
    fun expenseEntryTemplateTabCanScrollToLastTemplateOnSmallHeight() {
        val lastTemplateLabel = "テンプレ20 2000円"

        composeRule.setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(240.dp)
                ) {
                    ExpenseEntryContent(
                        paddingValues = PaddingValues(0.dp),
                        uiState = ExpenseEntryUiState(
                            currentTab = ExpenseEntryTab.Template,
                            dateInput = "2026-03-23",
                            templates = sampleTemplates()
                        ),
                        onTabSelected = {},
                        onDateChange = {},
                        onQuickAmountAdd = {},
                        onResetAmount = {},
                        onAmountChange = {},
                        onCategorySelected = {},
                        onSubCategorySelected = {},
                        onSaveExpense = {},
                        onTemplateSelected = { _, _, _ -> }
                    )
                }
            }
        }

        composeRule.onNodeWithText(lastTemplateLabel).assertIsNotDisplayed()

        composeRule.onNodeWithTag("expenseEntryTemplateList")
            .performScrollToNode(hasText(lastTemplateLabel))

        composeRule.onNodeWithText(lastTemplateLabel).assertIsDisplayed()
    }

    @Test
    fun defaultCategorySettingCanScrollToSaveOnSmallHeight() {
        composeRule.setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(240.dp)
                ) {
                    DefaultCategorySettingContent(
                        paddingValues = PaddingValues(0.dp),
                        uiState = DefaultCategorySettingUiState(
                            categories = sampleCategories(),
                            selectedCategoryId = "parent-1"
                        ),
                        onBack = {},
                        onCategorySelected = {},
                        onSave = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("設定を保存").assertIsNotDisplayed()

        composeRule.onNodeWithTag("defaultCategorySettingList")
            .performScrollToNode(hasText("設定を保存"))

        composeRule.onNodeWithText("設定を保存").assertIsDisplayed()
    }

    @Test
    fun backupManagementCanScrollToImportOnSmallHeight() {
        composeRule.setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(240.dp)
                ) {
                    BackupManagementContent(
                        paddingValues = PaddingValues(0.dp),
                        uiState = BackupManagementUiState(accountName = "user@example.com"),
                        onSignIn = {},
                        onSignOut = {},
                        onExport = {},
                        onImport = {},
                        onBack = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("Import（全置換）").assertIsNotDisplayed()

        composeRule.onNodeWithTag("backupManagementList")
            .performScrollToNode(hasText("Import（全置換）"))

        composeRule.onNodeWithText("Import（全置換）").assertIsDisplayed()
    }

    private fun sampleCategories(): List<Category> {
        return List(10) { index ->
            Category(
                id = "parent-${index + 1}",
                name = "カテゴリ${index + 1}",
                createdAt = "2026-03-23T00:00:00",
                updatedAt = "2026-03-23T00:00:00"
            )
        }
    }

    private fun sampleSubCategories(): List<Category> {
        return List(10) { index ->
            Category(
                id = "child-${index + 1}",
                name = "サブカテゴリ${index + 1}",
                parentId = "parent-1",
                createdAt = "2026-03-23T00:00:00",
                updatedAt = "2026-03-23T00:00:00"
            )
        }
    }

    private fun sampleTemplates(): List<Template> {
        return List(20) { index ->
            Template(
                id = "template-${index + 1}",
                name = "テンプレ${index + 1}",
                categoryId = "parent-1",
                amount = (index + 1) * 100,
                createdAt = "2026-03-23T00:00:00",
                updatedAt = "2026-03-23T00:00:00"
            )
        }
    }
}