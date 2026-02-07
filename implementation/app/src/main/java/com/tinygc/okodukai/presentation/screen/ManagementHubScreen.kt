package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManagementHubScreen(
    paddingValues: PaddingValues,
    onNavigateToCategory: () -> Unit,
    onNavigateToTemplate: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "管理",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onNavigateToCategory) {
            Text("カテゴリ管理")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToTemplate) {
            Text("テンプレ管理")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToBudget) {
            Text("予算設定")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToIncome) {
            Text("臨時収入管理")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToHistory) {
            Text("月次履歴")
        }
    }
}
