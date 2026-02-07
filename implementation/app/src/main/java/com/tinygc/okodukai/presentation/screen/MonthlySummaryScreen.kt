package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryViewModel
import com.tinygc.okodukai.presentation.viewmodel.ExpenseItem
import java.text.NumberFormat
import java.util.*

@Composable
fun MonthlySummaryScreen(
    paddingValues: PaddingValues,
    viewModel: MonthlySummaryViewModel = hiltViewModel(),
    onEditExpense: (ExpenseItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month selector
        item {
            MonthSelector(
                currentMonth = uiState.month,
                onPreviousMonth = { viewModel.onMonthChange(getPreviousMonth(uiState.month)) },
                onNextMonth = { viewModel.onMonthChange(getNextMonth(uiState.month)) }
            )
        }

        // Budget summary cards
        item {
            BudgetSummaryCard(
                budget = uiState.budget ?: 0,
                totalExpense = uiState.totalExpense ?: 0,
                remainingBudget = uiState.remainingBudget ?: 0,
                totalIncome = uiState.totalIncome ?: 0,
                currencyFormatter = currencyFormatter
            )
        }

        // Category totals
        if (uiState.categoryTotals.isNotEmpty()) {
            item {
                Text(
                    text = "カテゴリ別支出",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(uiState.categoryTotals) { categoryTotal ->
                CategoryTotalItem(
                    categoryName = categoryTotal.categoryName ?: "未分類",
                    amount = categoryTotal.totalAmount,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // Expense list
        if (uiState.expenseItems.isNotEmpty()) {
            item {
                Text(
                    text = "支出一覧",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(uiState.expenseItems) { expense ->
                ExpenseListItem(
                    expense = expense,
                    currencyFormatter = currencyFormatter,
                    onEdit = { onEditExpense(expense) },
                    onDelete = {
                        expenseToDelete = expense
                        showDeleteDialog = true
                    }
                )
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Empty state
        if (!uiState.isLoading && uiState.expenseItems.isEmpty() && uiState.errorMessage == null) {
            item {
                Text(
                    text = "この月の支出はまだ登録されていません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
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

    // Delete confirmation dialog
    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("支出を削除") },
            text = { Text("この支出を削除してもよろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteExpense(expenseToDelete!!.id)
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun MonthSelector(
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "前の月")
        }
        
        Text(
            text = formatMonthDisplay(currentMonth),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "次の月")
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    budget: Int,
    totalExpense: Int,
    remainingBudget: Int,
    totalIncome: Int,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (remainingBudget < 0) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BudgetRow("予算", budget, currencyFormatter, MaterialTheme.colorScheme.primary)
            BudgetRow("支出", totalExpense, currencyFormatter, MaterialTheme.colorScheme.error)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            BudgetRow(
                "残高", 
                remainingBudget, 
                currencyFormatter, 
                if (remainingBudget < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                isBold = true
            )
            if (totalIncome > 0) {
                BudgetRow("臨時収入", totalIncome, currencyFormatter, MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun BudgetRow(
    label: String,
    amount: Int,
    currencyFormatter: NumberFormat,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = currencyFormatter.format(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun CategoryTotalItem(
    categoryName: String,
    amount: Int,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = currencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: ExpenseItem,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.categoryName ?: "未分類",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.subCategoryName?.let { subName ->
                    Text(
                        text = subName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = currencyFormatter.format(expense.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                expense.memo?.let { memo ->
                    if (memo.isNotBlank()) {
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatDateDisplay(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "編集",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getPreviousMonth(currentMonth: String): String {
    val (year, month) = currentMonth.split("-").map { it.toInt() }
    return if (month == 1) {
        String.format("%04d-%02d", year - 1, 12)
    } else {
        String.format("%04d-%02d", year, month - 1)
    }
}

private fun getNextMonth(currentMonth: String): String {
    val (year, month) = currentMonth.split("-").map { it.toInt() }
    return if (month == 12) {
        String.format("%04d-%02d", year + 1, 1)
    } else {
        String.format("%04d-%02d", year, month + 1)
    }
}

private fun formatMonthDisplay(month: String): String {
    val (year, monthNum) = month.split("-")
    return "${year}年${monthNum.toInt()}月"
}

private fun formatDateDisplay(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size == 3) {
            "${parts[1].toInt()}月${parts[2].toInt()}日"
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
}
