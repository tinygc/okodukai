package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryViewModel
import com.tinygc.okodukai.presentation.viewmodel.ExpenseItem
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryUiState
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlySummaryScreen(
    paddingValues: PaddingValues,
    viewModel: MonthlySummaryViewModel = hiltViewModel(),
    onEditExpense: (ExpenseItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }
    MonthlySummaryContent(
        paddingValues = paddingValues,
        uiState = uiState,
        onPreviousMonth = { viewModel.onMonthChange(getPreviousMonth(uiState.month)) },
        onNextMonth = { viewModel.onMonthChange(getNextMonth(uiState.month)) },
        onBackToCurrentMonth = { viewModel.onMonthChange(currentMonth) },
        showBackToCurrentMonth = uiState.month != currentMonth,
        onEditExpense = onEditExpense,
        onDeleteExpense = { viewModel.onDeleteExpense(it.id) },
        onShowAllCategories = {},
        onShowAllExpenses = {}
    )
}

@Composable
internal fun MonthlySummaryContent(
    paddingValues: PaddingValues,
    uiState: MonthlySummaryUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBackToCurrentMonth: () -> Unit,
    showBackToCurrentMonth: Boolean,
    onEditExpense: (ExpenseItem) -> Unit,
    onDeleteExpense: (ExpenseItem) -> Unit,
    onShowAllCategories: () -> Unit,
    onShowAllExpenses: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month selector
        item {
            MonthSelector(
                currentMonth = uiState.month,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onBackToCurrentMonth = onBackToCurrentMonth,
                showBackToCurrentMonth = showBackToCurrentMonth
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
        item {
            Text(
                text = "カテゴリ別支出",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }
        if (uiState.topCategories.isEmpty() && uiState.otherTotal == 0) {
            item {
                Text(
                    text = "データがありません",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(uiState.topCategories) { categoryTotal ->
                CategoryTotalItem(
                    categoryName = categoryTotal.categoryName ?: "未分類",
                    amount = categoryTotal.totalAmount,
                    currencyFormatter = currencyFormatter
                )
            }
            if (uiState.otherTotal > 0) {
                item {
                    CategoryTotalItem(
                        categoryName = "その他",
                        amount = uiState.otherTotal,
                        currencyFormatter = currencyFormatter
                    )
                }
            }
            item {
                Text(
                    text = "一覧を見る ＞",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clickable { onShowAllCategories() },
                    textAlign = TextAlign.End
                )
            }
        }

        // Expense list
        item {
            Text(
                text = "支出一覧",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }
        val latestExpenses = uiState.expenseItems.sortedByDescending { it.date }.take(5)
        if (latestExpenses.isEmpty()) {
            item {
                Text(
                    text = "今月の支出はありません",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(latestExpenses) { expense ->
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
            item {
                Text(
                    text = "全件表示 ＞",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clickable { onShowAllExpenses() },
                    textAlign = TextAlign.End
                )
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
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

    // Delete confirmation dialog
    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("支出を削除") },
            text = { Text("この支出を削除してもよろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteExpense(expenseToDelete!!)
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
    onNextMonth: () -> Unit,
    onBackToCurrentMonth: () -> Unit,
    showBackToCurrentMonth: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "前の月",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = formatMonthDisplay(currentMonth),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "次の月",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (showBackToCurrentMonth) {
                Text(
                    text = "今月に戻る",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onBackToCurrentMonth() },
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "今月",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
    val progress = if (budget > 0) {
        (totalExpense.toFloat() / budget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (remainingBudget < 0) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "残額",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = currencyFormatter.format(remainingBudget),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BudgetSubRow("予算", budget, currencyFormatter)
                BudgetSubRow("支出", totalExpense, currencyFormatter)
            }

            ProgressBar(progress = progress)

            if (totalIncome > 0) {
                BudgetSubRow("臨時収入", totalIncome, currencyFormatter)
            }
        }
    }
}

@Composable
private fun BudgetSubRow(
    label: String,
    amount: Int,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = currencyFormatter.format(amount),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(MaterialTheme.colorScheme.onSecondaryContainer)
        )
    }
}

@Composable
private fun CategoryTotalItem(
    categoryName: String,
    amount: Int,
    currencyFormatter: NumberFormat
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
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
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormatter.format(amount),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = expense.categoryName ?: "未分類",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
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
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                expense.memo?.let { memo ->
                    if (memo.isNotBlank()) {
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = formatDateDisplay(expense.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "編集",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "削除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getPreviousMonth(currentMonth: String): String {
    return try {
        val parts = currentMonth.split("-")
        if (parts.size == 2) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            if (month == 1) {
                String.format("%04d-%02d", year - 1, 12)
            } else {
                String.format("%04d-%02d", year, month - 1)
            }
        } else {
            currentMonth
        }
    } catch (e: Exception) {
        currentMonth
    }
}

private fun getNextMonth(currentMonth: String): String {
    return try {
        val parts = currentMonth.split("-")
        if (parts.size == 2) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            if (month == 12) {
                String.format("%04d-%02d", year + 1, 1)
            } else {
                String.format("%04d-%02d", year, month + 1)
            }
        } else {
            currentMonth
        }
    } catch (e: Exception) {
        currentMonth
    }
}

private fun formatMonthDisplay(month: String): String {
    return try {
        val parts = month.split("-")
        if (parts.size == 2) {
            val year = parts[0]
            val monthNum = parts[1].toInt()
            "${year}年${monthNum}月"
        } else {
            month
        }
    } catch (e: Exception) {
        month
    }
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
