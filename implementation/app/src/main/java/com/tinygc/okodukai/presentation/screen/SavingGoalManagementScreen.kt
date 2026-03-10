package com.tinygc.okodukai.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.model.SavingGoal

@Composable
fun SavingGoalManagementScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    viewModel: SavingGoalManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editGoal by remember { mutableStateOf<SavingGoal?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SavingGoalManagementEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "貯金目標管理",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onBack) {
                Text(text = "戻る")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "達成モード", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.mode == GoalAchievementMode.INDIVIDUAL,
                        onClick = { viewModel.setMode(GoalAchievementMode.INDIVIDUAL) }
                    )
                    Text(text = "個別達成")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.mode == GoalAchievementMode.TOTAL,
                        onClick = { viewModel.setMode(GoalAchievementMode.TOTAL) }
                    )
                    Text(text = "合計達成")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { showAddDialog = true }) {
            Text("目標追加")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.goals, key = { it.id }) { goal ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(goal.name, fontWeight = FontWeight.Medium)
                            Text("目標金額: ${goal.targetAmount}円")
                            Text(if (goal.isActive) "有効" else "無効")
                        }
                        Row {
                            IconButton(onClick = { editGoal = goal }) {
                                Icon(Icons.Filled.Edit, contentDescription = "編集")
                            }
                            IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "削除")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SavingGoalDialog(
            title = "貯金目標追加",
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, isActive ->
                viewModel.addGoal(name, amount, isActive)
                showAddDialog = false
            }
        )
    }

    editGoal?.let { goal ->
        SavingGoalDialog(
            title = "貯金目標編集",
            initialName = goal.name,
            initialAmount = goal.targetAmount.toString(),
            initialActive = goal.isActive,
            onDismiss = { editGoal = null },
            onSave = { name, amount, isActive ->
                viewModel.updateGoal(goal, name, amount, isActive)
                editGoal = null
            }
        )
    }
}

@Composable
private fun SavingGoalDialog(
    title: String,
    initialName: String = "",
    initialAmount: String = "",
    initialActive: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (String, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var amount by remember { mutableStateOf(initialAmount) }
    var isActive by remember { mutableStateOf(initialActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("目標名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("目標金額") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = isActive, onClick = { isActive = true })
                    Text("有効")
                    RadioButton(selected = !isActive, onClick = { isActive = false })
                    Text("無効")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountInt = amount.toIntOrNull() ?: 0
                    onSave(name, amountInt, isActive)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}
