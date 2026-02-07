package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.presentation.viewmodel.MonthlyHistoryViewModel
import com.tinygc.okodukai.presentation.viewmodel.MonthlyHistoryItem
import java.text.NumberFormat
import java.util.*

@Composable
fun MonthlyHistoryScreen(
    paddingValues: PaddingValues,
    viewModel: MonthlyHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "月次履歴",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onBack) {
                    Text("戻る")
                }
            }
        }

        // Info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊 過去12ヶ月の予算・支出の推移",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "月ごとの予算設定と実際の支出、および臨時収入を確認できます",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Histories list
        if (uiState.histories.isNotEmpty()) {
            items(uiState.histories) { item ->
                MonthlyHistoryCard(
                    item = item,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun MonthlyHistoryCard(
    item: MonthlyHistoryItem,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month header
            Text(
                text = formatMonthForHistory(item.month),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Budget row
            if (item.budget != null) {
                HistoryRowWithBar(
                    label = "予算",
                    amount = item.budget,
                    currencyFormatter = currencyFormatter,
                    color = MaterialTheme.colorScheme.primary,
                    percentage = null
                )
            } else {
                Text(
                    text = "予算未設定",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expense row
            HistoryRowWithBar(
                label = "支出",
                amount = item.totalExpense,
                currencyFormatter = currencyFormatter,
                color = MaterialTheme.colorScheme.error,
                percentage = item.budget?.let { budget ->
                    if (budget > 0) (item.totalExpense.toFloat() / budget) else 0f
                }
            )

            // Remaining budget row (only if budget is set)
            if (item.remainingBudget != null) {
                val remainingColor = if (item.remainingBudget < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "残額",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currencyFormatter.format(item.remainingBudget),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = remainingColor
                    )
                }
            }

            // Income row (only if > 0)
            if (item.totalIncome > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "臨時収入",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currencyFormatter.format(item.totalIncome),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRowWithBar(
    label: String,
    amount: Int,
    currencyFormatter: NumberFormat,
    color: androidx.compose.ui.graphics.Color,
    percentage: Float?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currencyFormatter.format(amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (percentage != null) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Progress bar (only if percentage is provided)
        if (percentage != null) {
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// Helper function
private fun formatMonthForHistory(month: String): String {
    val (year, monthNum) = month.split("-")
    return "${year}年${monthNum.toInt()}月"
}
