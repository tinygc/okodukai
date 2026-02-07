package com.tinygc.okodukai.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

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
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = if (uiState.currentTab == ExpenseEntryTab.Normal) 0 else 1) {
            Tab(
                selected = uiState.currentTab == ExpenseEntryTab.Normal,
                onClick = { viewModel.onTabSelected(ExpenseEntryTab.Normal) },
                text = { Text("通常") }
            )
            Tab(
                selected = uiState.currentTab == ExpenseEntryTab.Template,
                onClick = { viewModel.onTabSelected(ExpenseEntryTab.Template) },
                text = { Text("テンプレ") }
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

    OutlinedTextField(
        value = uiState.amountInput,
        onValueChange = viewModel::onAmountChange,
        label = { Text("金額") },
        placeholder = { Text("例: 1200") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    OutlinedTextField(
        value = uiState.dateInput,
        onValueChange = viewModel::onDateChange,
        label = { Text("日付 (YYYY-MM-DD)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
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
            label = { Text("カテゴリ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 12.dp)
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
                label = { Text("サブカテゴリ") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subCategoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 16.dp)
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
        Text("保存")
    }
}

@Composable
private fun TemplateEntryContent(
    uiState: ExpenseEntryUiState,
    viewModel: ExpenseEntryViewModel
) {
    if (uiState.templates.isEmpty()) {
        Text(
            text = "テンプレがありません",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    LazyColumn {
        items(uiState.templates) { template ->
            Button(
                onClick = {
                    viewModel.onTemplateSelected(
                        templateCategoryId = template.categoryId,
                        templateSubCategoryId = template.subCategoryId,
                        templateAmount = template.amount
                    )
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(text = "${template.name} ${template.amount}円")
            }
        }
    }
}
