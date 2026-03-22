package com.tinygc.okodukai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.presentation.component.DatePickerField
import com.tinygc.okodukai.presentation.viewmodel.ExpenseItem
import com.tinygc.okodukai.presentation.viewmodel.MonthlySummaryViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ExpenseListScreen(
    paddingValues: PaddingValues,
    month: String,
    onBack: () -> Unit,
    viewModel: MonthlySummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseItem?>(null) }
    var editDateInput by remember { mutableStateOf("") }
    var editAmountInput by remember { mutableStateOf("") }
    var editMemoInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "戻る"
                )
            }
            Text(
                text = "支出一覧",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Expense list
        if (uiState.expenseItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "今月の支出はありません",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    uiState.expenseItems.sortedByDescending { it.date },
                    key = { it.id }
                ) { expense ->
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

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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

    if (showEditDialog && expenseToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("支出を編集") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        dateValue = editDateInput,
                        onDateSelected = { editDateInput = it }
                    )
                    OutlinedTextField(
                        value = editAmountInput,
                        onValueChange = { editAmountInput = it.filter { ch -> ch.isDigit() } },
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
                        viewModel.onUpdateExpense(
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
private fun ExpenseListItem(
    expense: ExpenseItem,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expense.categoryName ?: "未分類",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!expense.memo.isNullOrEmpty()) {
                    Text(
                        text = expense.memo,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥${expense.amount.toString().padStart(1, ' ')}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "編集",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "削除",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
