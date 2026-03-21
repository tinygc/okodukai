package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.util.DateTimeUtil
import com.tinygc.okodukai.presentation.viewmodel.CategoryTotalUiModel
import com.tinygc.okodukai.presentation.viewmodel.ExpenseItem
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryUiState
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryViewModel
import com.tinygc.okodukai.presentation.viewmodel.SavingGoalProgressUiModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MonthlySummaryScreen(
    paddingValues: PaddingValues,
    onNavigateToExpenseList: (String) -> Unit = {},
    onNavigateToCategoryList: (String) -> Unit = {},
    viewModel: MonthlySummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth = DateTimeUtil.getCurrentMonth()

    MonthlySummaryContent(
        paddingValues = paddingValues,
        uiState = uiState,
        onPreviousMonth = { viewModel.onMonthChange(getPreviousMonth(uiState.month)) },
        onNextMonth = { viewModel.onMonthChange(getNextMonth(uiState.month)) },
        onBackToCurrentMonth = { viewModel.onMonthChange(currentMonth) },
        showBackToCurrentMonth = uiState.month != currentMonth,
        onUpdateExpense = { expenseItem, date, amount, memo ->
            viewModel.onUpdateExpense(expenseItem, date, amount, memo)
        },
        onDeleteExpense = { viewModel.onDeleteExpense(it.id) },
        onShowAllCategories = { onNavigateToCategoryList(uiState.month) },
        onShowAllExpenses = { onNavigateToExpenseList(uiState.month) }
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
    onUpdateExpense: (ExpenseItem, String, Int, String?) -> Unit,
    onDeleteExpense: (ExpenseItem) -> Unit,
    onShowAllCategories: () -> Unit,
    onShowAllExpenses: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseItem?>(null) }
    var editDateInput by remember { mutableStateOf("") }
    var editAmountInput by remember { mutableStateOf("") }
    var editMemoInput by remember { mutableStateOf("") }

    val latestExpenses = uiState.expenseItems.sortedByDescending { it.date }.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MonthSelector(
                currentMonth = uiState.month,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onBackToCurrentMonth = onBackToCurrentMonth,
                showBackToCurrentMonth = showBackToCurrentMonth
            )
        }

        item {
            BudgetSummaryCard(
                budget = uiState.budget ?: 0,
                totalExpense = uiState.totalExpense ?: 0,
                remainingBudget = uiState.remainingBudget ?: 0,
                totalIncome = uiState.totalIncome ?: 0,
                currencyFormatter = currencyFormatter
            )
        }

        item {
            SavingsProgressCard(
                carryOverBalance = uiState.carryOverBalance,
                savingsAvailable = uiState.savingsAvailable,
                goalAchievementMode = uiState.goalAchievementMode,
                savingGoals = uiState.savingGoals,
                totalSavingTarget = uiState.totalSavingTarget,
                totalSavingRemaining = uiState.totalSavingRemaining,
                isSavingGoalAchieved = uiState.isSavingGoalAchieved,
                currencyFormatter = currencyFormatter
            )
        }

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
            items(uiState.topCategories) { categoryTotal: CategoryTotalUiModel ->
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

        item {
            Text(
                text = "支出一覧",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

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
                    onEdit = {
                        expenseToEdit = expense
                        editDateInput = expense.date
                        editAmountInput = expense.amount.toString()
                        editMemoInput = expense.memo.orEmpty()
                        showEditDialog = true
                    },
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

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("支出を削除") },
            text = { Text("この支出を削除しますか？") },
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

    if (showEditDialog && expenseToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("支出を編集") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editDateInput,
                        onValueChange = { editDateInput = it },
                        label = { Text("日付 (YYYY-MM-DD)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editAmountInput,
                        onValueChange = { editAmountInput = it.filter(Char::isDigit) },
                        label = { Text("金額") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editMemoInput,
                        onValueChange = { editMemoInput = it },
                        label = { Text("メモ（任意）") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editAmountInput.toIntOrNull()
                        if (editDateInput.isBlank() || amount == null || amount <= 0) {
                            return@TextButton
                        }
                        onUpdateExpense(
                            expenseToEdit!!,
                            editDateInput,
                            amount,
                            editMemoInput.ifBlank { null }
                        )
                        showEditDialog = false
                        expenseToEdit = null
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
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
                text = "今月の残り予算",
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
private fun SavingsProgressCard(
    carryOverBalance: Int,
    savingsAvailable: Int,
    goalAchievementMode: GoalAchievementMode,
    savingGoals: List<SavingGoalProgressUiModel>,
    totalSavingTarget: Int,
    totalSavingRemaining: Int,
    isSavingGoalAchieved: Boolean,
    currencyFormatter: NumberFormat
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "貯金目標",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                SavingsModeBadge(mode = goalAchievementMode)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SavingsSummaryMetricCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = "繰越金",
                    amountText = currencyFormatter.format(carryOverBalance)
                )
            }

            if (savingGoals.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "貯金目標が未登録です",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                return@Column
            }

            when (goalAchievementMode) {
                GoalAchievementMode.INDIVIDUAL -> {
                    savingGoals.forEach { goal ->
                        SavingGoalProgressCard(goal = goal, currencyFormatter = currencyFormatter)
                    }
                }

                GoalAchievementMode.TOTAL -> {
                    TotalSavingProgressCard(
                        totalSavingTarget = totalSavingTarget,
                        totalSavingRemaining = totalSavingRemaining,
                        isSavingGoalAchieved = isSavingGoalAchieved,
                        currencyFormatter = currencyFormatter
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsModeBadge(mode: GoalAchievementMode) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = if (mode == GoalAchievementMode.INDIVIDUAL) "個別達成中" else "合計達成中",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SavingsSummaryMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    amountText: String,
    emphasize: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amountText,
                style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SavingGoalProgressCard(
    goal: SavingGoalProgressUiModel,
    currencyFormatter: NumberFormat
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = if (goal.isAchieved) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (goal.isAchieved) "達成" else "あと ${currencyFormatter.format(goal.remainingAmount)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Text(
                text = "目標金額: ${currencyFormatter.format(goal.targetAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TotalSavingProgressCard(
    totalSavingTarget: Int,
    totalSavingRemaining: Int,
    isSavingGoalAchieved: Boolean,
    currencyFormatter: NumberFormat
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "合計目標",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormatter.format(totalSavingTarget),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            Text(
                text = "達成状況",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isSavingGoalAchieved) "達成" else "あと ${currencyFormatter.format(totalSavingRemaining)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "編集")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }
        }
    }
}

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
