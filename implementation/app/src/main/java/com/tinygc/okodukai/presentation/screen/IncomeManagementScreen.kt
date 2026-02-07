package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.presentation.viewmodel.IncomeManagementViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun IncomeManagementScreen(
    paddingValues: PaddingValues,
    viewModel: IncomeManagementViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var incomeToDelete by remember { mutableStateOf<Income?>(null) }

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
                    text = "臨時収入管理",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onBack) {
                    Text("戻る")
                }
            }
        }

        // Month selector
        item {
            MonthSelectorCardForIncome(
                currentMonth = uiState.month,
                onPreviousMonth = { viewModel.onMonthChange(getPreviousMonth(uiState.month)) },
                onNextMonth = { viewModel.onMonthChange(getNextMonth(uiState.month)) }
            )
        }

        // Total income display
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "今月の臨時収入合計",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = currencyFormatter.format(uiState.totalIncome),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Add button
        item {
            Button(
                onClick = viewModel::showAddDialog,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && !uiState.isLoading
            ) {
                Icon(Icons.Default.Add, contentDescription = "追加")
                Spacer(modifier = Modifier.width(8.dp))
                Text("臨時収入を追加")
            }
        }

        // Incomes list
        if (uiState.incomes.isNotEmpty()) {
            item {
                Text(
                    text = "臨時収入一覧",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(uiState.incomes) { income ->
                IncomeListItem(
                    income = income,
                    currencyFormatter = currencyFormatter,
                    onDelete = {
                        incomeToDelete = income
                        showDeleteDialog = true
                    }
                )
            }
        } else if (!uiState.isLoading) {
            item {
                Text(
                    text = "この月の臨時収入はまだ登録されていません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
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

        // Success message
        uiState.successMessage?.let { message ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
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

    // Add income dialog
    if (uiState.showAddDialog) {
        AddIncomeDialog(
            amountInput = uiState.amountInput,
            memoInput = uiState.memoInput,
            dateInput = uiState.dateInput,
            isSaving = uiState.isSaving,
            onAmountChange = viewModel::onAmountChange,
            onMemoChange = viewModel::onMemoChange,
            onDateChange = viewModel::onDateChange,
            onAdd = viewModel::addIncome,
            onDismiss = viewModel::hideAddDialog
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && incomeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("臨時収入を削除") },
            text = { Text("この臨時収入を削除してもよろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteIncome(incomeToDelete!!)
                        showDeleteDialog = false
                        incomeToDelete = null
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
private fun MonthSelectorCardForIncome(
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "前の月")
            }

            Text(
                text = formatMonthDisplay(currentMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "次の月")
            }
        }
    }
}

@Composable
private fun IncomeListItem(
    income: Income,
    currencyFormatter: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = currencyFormatter.format(income.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                income.memo?.let { memo ->
                    if (memo.isNotBlank()) {
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatDateDisplay(income.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun AddIncomeDialog(
    amountInput: String,
    memoInput: String,
    dateInput: String,
    isSaving: Boolean,
    onAmountChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("臨時収入を追加") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = onDateChange,
                    label = { Text("日付 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    label = { Text("金額") },
                    placeholder = { Text("例: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    singleLine = true
                )

                OutlinedTextField(
                    value = memoInput,
                    onValueChange = onMemoChange,
                    label = { Text("メモ（任意）") },
                    placeholder = { Text("例: ボーナス") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAdd,
                enabled = amountInput.isNotEmpty() && dateInput.isNotEmpty() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "追加中..." else "追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("キャンセル")
            }
        }
    )
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
