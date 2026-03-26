package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private inline fun LazyListScope.itemContent(crossinline content: @Composable () -> Unit) {
    item { content() }
}

@Composable
fun ManagementHubScreen(
    paddingValues: PaddingValues,
    onNavigateToCategory: () -> Unit,
    onNavigateToTemplate: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToDefaultCategory: () -> Unit = {},
    onNavigateToSavingGoal: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToQuickAmountSetting: () -> Unit = {},
    onNavigateToRemoveAds: () -> Unit = {},
    isAdRemovalPurchased: Boolean = false,
    onShowInitialSetupGuide: () -> Unit = {}
) {
    ManagementHubContent(
        paddingValues = paddingValues,
        onNavigateToCategory = onNavigateToCategory,
        onNavigateToTemplate = onNavigateToTemplate,
        onNavigateToBudget = onNavigateToBudget,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToDefaultCategory = onNavigateToDefaultCategory,
        onNavigateToSavingGoal = onNavigateToSavingGoal,
        onNavigateToBackup = onNavigateToBackup,
        onNavigateToQuickAmountSetting = onNavigateToQuickAmountSetting,
        onNavigateToRemoveAds = onNavigateToRemoveAds,
        isAdRemovalPurchased = isAdRemovalPurchased,
        onShowInitialSetupGuide = onShowInitialSetupGuide
    )
}

@Composable
internal fun ManagementHubContent(
    paddingValues: PaddingValues,
    onNavigateToCategory: () -> Unit,
    onNavigateToTemplate: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToDefaultCategory: () -> Unit = {},
    onNavigateToSavingGoal: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToQuickAmountSetting: () -> Unit = {},
    onNavigateToRemoveAds: () -> Unit = {},
    isAdRemovalPurchased: Boolean = false,
    onShowInitialSetupGuide: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("managementHubList")
    ) {
        itemContent {
            Text(
                text = "管理",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold
            )
        }
        itemContent {
            Spacer(modifier = Modifier.height(12.dp))
        }
        itemContent {
            ManagementHubSectionTitle(title = "家計管理")
        }
        itemContent {
            ManagementHubItem(label = "予算設定", onClick = onNavigateToBudget)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "月別履歴", onClick = onNavigateToHistory)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "臨時収入管理", onClick = onNavigateToIncome)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "貯金目標管理", onClick = onNavigateToSavingGoal)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubSectionTitle(title = "入力設定")
        }
        itemContent {
            ManagementHubItem(label = "カテゴリ管理", onClick = onNavigateToCategory)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "テンプレ管理", onClick = onNavigateToTemplate)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "デフォルトカテゴリ設定", onClick = onNavigateToDefaultCategory)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "クイック入力金額設定", onClick = onNavigateToQuickAmountSetting)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubSectionTitle(title = "データ管理")
        }
        itemContent {
            ManagementHubItem(label = "バックアップ", onClick = onNavigateToBackup)
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubSectionTitle(title = "その他")
        }
        itemContent {
            ManagementHubItem(
                label = if (isAdRemovalPurchased) "広告非表示（購入済み）" else "広告を非表示にする",
                onClick = if (isAdRemovalPurchased) ({}) else onNavigateToRemoveAds
            )
        }
        itemContent {
            Spacer(modifier = Modifier.height(8.dp))
        }
        itemContent {
            ManagementHubItem(label = "初期設定ガイドを再表示", onClick = onShowInitialSetupGuide)
        }
    }
}

@Composable
private fun ManagementHubSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ManagementHubItem(
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
