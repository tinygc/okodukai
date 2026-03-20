package com.tinygc.okodukai.presentation.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManagementHubScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun managementSectionsAndLabelsAreDisplayedForFr12() {
        composeRule.setContent {
            MaterialTheme {
                ManagementHubScreen(
                    paddingValues = PaddingValues(0.dp),
                    onNavigateToCategory = {},
                    onNavigateToTemplate = {}
                )
            }
        }

        composeRule.onNodeWithText("家計管理").assertIsDisplayed()
        composeRule.onNodeWithText("入力設定").assertIsDisplayed()
        composeRule.onNodeWithText("データ管理").assertIsDisplayed()

        composeRule.onNodeWithText("予算設定").assertIsDisplayed()
        composeRule.onNodeWithText("月別履歴").assertIsDisplayed()
        composeRule.onNodeWithText("臨時収入管理").assertIsDisplayed()
        composeRule.onNodeWithText("貯金目標管理").assertIsDisplayed()

        composeRule.onNodeWithText("カテゴリ管理").assertIsDisplayed()
        composeRule.onNodeWithText("テンプレ管理").assertIsDisplayed()
        composeRule.onNodeWithText("デフォルトカテゴリ設定").assertIsDisplayed()
        composeRule.onNodeWithText("クイック入力金額設定").assertIsDisplayed()

        composeRule.onNodeWithText("バックアップ").assertIsDisplayed()
    }

    @Test
    fun managementSectionOrderIsFixedForFr12() {
        composeRule.setContent {
            MaterialTheme {
                ManagementHubScreen(
                    paddingValues = PaddingValues(0.dp),
                    onNavigateToCategory = {},
                    onNavigateToTemplate = {}
                )
            }
        }

        val householdTop = composeRule.onNodeWithText("家計管理").getUnclippedBoundsInRoot().top
        val inputTop = composeRule.onNodeWithText("入力設定").getUnclippedBoundsInRoot().top
        val dataTop = composeRule.onNodeWithText("データ管理").getUnclippedBoundsInRoot().top

        assertTrue(householdTop < inputTop)
        assertTrue(inputTop < dataTop)
    }

    @Test
    fun oldLabelGetsujiRirekiIsNotDisplayed() {
        composeRule.setContent {
            MaterialTheme {
                ManagementHubScreen(
                    paddingValues = PaddingValues(0.dp),
                    onNavigateToCategory = {},
                    onNavigateToTemplate = {}
                )
            }
        }

        composeRule.onAllNodesWithText("月次履歴").assertCountEquals(0)
    }

    @Test
    fun managementMenuClickRoutesAreWired() {
        var budgetClicked = false
        var historyClicked = false
        var backupClicked = false

        composeRule.setContent {
            MaterialTheme {
                ManagementHubScreen(
                    paddingValues = PaddingValues(0.dp),
                    onNavigateToCategory = {},
                    onNavigateToTemplate = {},
                    onNavigateToBudget = { budgetClicked = true },
                    onNavigateToHistory = { historyClicked = true },
                    onNavigateToBackup = { backupClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("予算設定").performClick()
        composeRule.onNodeWithText("月別履歴").performClick()
        composeRule.onNodeWithText("バックアップ").performClick()

        composeRule.runOnIdle {
            assertTrue(budgetClicked)
            assertTrue(historyClicked)
            assertTrue(backupClicked)
        }
    }
}