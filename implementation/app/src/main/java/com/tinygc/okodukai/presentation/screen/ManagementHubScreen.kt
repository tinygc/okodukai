package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManagementHubScreen(
    paddingValues: PaddingValues,
    onNavigateToCategory: () -> Unit,
    onNavigateToTemplate: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToDefaultCategory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "管理",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        ManagementHubItem(label = "カテゴリ管理", onClick = onNavigateToCategory)
        Spacer(modifier = Modifier.height(8.dp))
        ManagementHubItem(label = "テンプレ管理", onClick = onNavigateToTemplate)
        Spacer(modifier = Modifier.height(8.dp))
        ManagementHubItem(label = "予算設定", onClick = onNavigateToBudget)
        Spacer(modifier = Modifier.height(8.dp))
        ManagementHubItem(label = "臨時収入管理", onClick = onNavigateToIncome)
        Spacer(modifier = Modifier.height(8.dp))
        ManagementHubItem(label = "月次履歴", onClick = onNavigateToHistory)
        Spacer(modifier = Modifier.height(8.dp))
        ManagementHubItem(label = "デフォルトカテゴリ設定", onClick = onNavigateToDefaultCategory)
    }
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
