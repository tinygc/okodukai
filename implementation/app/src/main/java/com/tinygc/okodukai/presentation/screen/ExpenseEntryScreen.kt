package com.tinygc.okodukai.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    paddingValues: PaddingValues,
    viewModel: ExpenseEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExpenseEntryUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                ExpenseEntryUiEvent.Saved -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "支出入力",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = if (uiState.currentTab == ExpenseEntryTab.Normal) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            val normalSelected = uiState.currentTab == ExpenseEntryTab.Normal
            Tab(
                selected = normalSelected,
                onClick = { viewModel.onTabSelected(ExpenseEntryTab.Normal) },
                modifier = Modifier.height(44.dp),
                text = {
                    Text(
                        text = "通常",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = if (normalSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
            val templateSelected = uiState.currentTab == ExpenseEntryTab.Template
            Tab(
                selected = templateSelected,
                onClick = { viewModel.onTabSelected(ExpenseEntryTab.Template) },
                modifier = Modifier.height(44.dp),
                text = {
                    Text(
                        text = "テンプレ",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                        fontWeight = if (templateSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.currentTab) {
            ExpenseEntryTab.Normal -> {
                NormalEntryContent(uiState = uiState, viewModel = viewModel)
            }
            ExpenseEntryTab.Template -> {
                TemplateEntryContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalEntryContent(
    uiState: ExpenseEntryUiState,
    viewModel: ExpenseEntryViewModel
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var subCategoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // DatePickerの初期値を現在の日付から設定
    val today = LocalDate.now()
    val initialMillis = today.atStartOfDay(ZoneId.of("Asia/Tokyo")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 日付フィールド (タップでDatePickerを開く) - 一番上に配置
    OutlinedTextField(
        value = uiState.dateInput,
        onValueChange = {},
        readOnly = true,
        label = { Text("日付", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "カレンダーを開く",
                modifier = Modifier.clickable { showDatePicker = true },
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { showDatePicker = true },
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp
        )
    )

    // DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("Asia/Tokyo"))
                                .toLocalDate()
                            viewModel.onDateChange(selectedDate.format(dateFormatter))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("キャンセル")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // クイック金額入力ボタン
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "クイック入力",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 第1行: 1, 10, 50
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAmountButton(
                    amount = 1,
                    onClick = { viewModel.onQuickAmountAdd(1) },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountButton(
                    amount = 10,
                    onClick = { viewModel.onQuickAmountAdd(10) },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountButton(
                    amount = 50,
                    onClick = { viewModel.onQuickAmountAdd(50) },
                    modifier = Modifier.weight(1f)
                )
            }

            // 第2行: 100, 300, 500
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAmountButton(
                    amount = 100,
                    onClick = { viewModel.onQuickAmountAdd(100) },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountButton(
                    amount = 300,
                    onClick = { viewModel.onQuickAmountAdd(300) },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountButton(
                    amount = 500,
                    onClick = { viewModel.onQuickAmountAdd(500) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 第3行: 1000, 3000, リセット
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAmountButton(
                    amount = 1000,
                    onClick = { viewModel.onQuickAmountAdd(1000) },
                    modifier = Modifier.weight(1f)
                )
                QuickAmountButton(
                    amount = 3000,
                    onClick = { viewModel.onQuickAmountAdd(3000) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { viewModel.onResetAmount() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("リセット", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 金額表示・手入力フィールド
    OutlinedTextField(
        value = uiState.amountInput,
        onValueChange = viewModel::onAmountChange,
        label = { Text("金額", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
        placeholder = { Text("例: 1200", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        suffix = { Text("円", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)) }
    )

    ExposedDropdownMenuBox(
        expanded = categoryExpanded,
        onExpandedChange = { categoryExpanded = !categoryExpanded }
    ) {
        val selectedCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
        TextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("カテゴリ", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { categoryExpanded = false }
        ) {
            uiState.categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        viewModel.onCategorySelected(category.id)
                        categoryExpanded = false
                    }
                )
            }
        }
    }

    if (uiState.subCategories.isNotEmpty()) {
        ExposedDropdownMenuBox(
            expanded = subCategoryExpanded,
            onExpandedChange = { subCategoryExpanded = !subCategoryExpanded }
        ) {
            val selectedSubCategory = uiState.subCategories.find { it.id == uiState.selectedSubCategoryId }
            TextField(
                value = selectedSubCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("サブカテゴリ", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subCategoryExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            ExposedDropdownMenu(
                expanded = subCategoryExpanded,
                onDismissRequest = { subCategoryExpanded = false }
            ) {
                uiState.subCategories.forEach { subCategory ->
                    DropdownMenuItem(
                        text = { Text(subCategory.name) },
                        onClick = {
                            viewModel.onSubCategorySelected(subCategory.id)
                            subCategoryExpanded = false
                        }
                    )
                }
            }
        }
    }

    Button(
        onClick = { viewModel.onSaveExpense() },
        enabled = !uiState.isSaving
    ) {
        Text("保存", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEntryContent(
    uiState: ExpenseEntryUiState,
    viewModel: ExpenseEntryViewModel
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // DatePickerの初期値を現在の日付から設定
    val today = LocalDate.now()
    val initialMillis = today.atStartOfDay(ZoneId.of("Asia/Tokyo")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 日付フィールド (タップでDatePickerを開く) - テンプレタブにも表示
        OutlinedTextField(
            value = uiState.dateInput,
            onValueChange = {},
            readOnly = true,
            label = { Text("日付", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "カレンダーを開く",
                    modifier = Modifier.clickable { showDatePicker = true },
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )
        )

        // DatePickerDialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("Asia/Tokyo"))
                                    .toLocalDate()
                                viewModel.onDateChange(selectedDate.format(dateFormatter))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("キャンセル")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (uiState.templates.isEmpty()) {
            Text(
                text = "テンプレがありません",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.templates) { template ->
            FilledTonalButton(
                onClick = {
                    viewModel.onTemplateSelected(
                        templateCategoryId = template.categoryId,
                        templateSubCategoryId = template.subCategoryId,
                        templateAmount = template.amount
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "${template.name} ${template.amount}円",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                )
            }
        }
            }
        }
    }
}

@Composable
private fun QuickAmountButton(
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "+${amount}",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Bold
        )
    }
}
