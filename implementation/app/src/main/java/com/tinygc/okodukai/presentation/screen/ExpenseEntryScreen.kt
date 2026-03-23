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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.tinygc.okodukai.presentation.component.DatePickerField

private inline fun LazyListScope.itemContent(crossinline content: @Composable () -> Unit) {
    item { content() }
}

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

    ExpenseEntryContent(
        paddingValues = paddingValues,
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onDateChange = viewModel::onDateChange,
        onQuickAmountAdd = viewModel::onQuickAmountAdd,
        onResetAmount = viewModel::onResetAmount,
        onAmountChange = viewModel::onAmountChange,
        onCategorySelected = viewModel::onCategorySelected,
        onSubCategorySelected = viewModel::onSubCategorySelected,
        onSaveExpense = viewModel::onSaveExpense,
        onTemplateSelected = viewModel::onTemplateSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseEntryContent(
    paddingValues: PaddingValues,
    uiState: ExpenseEntryUiState,
    onTabSelected: (ExpenseEntryTab) -> Unit,
    onDateChange: (String) -> Unit,
    onQuickAmountAdd: (Int) -> Unit,
    onResetAmount: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSubCategorySelected: (String) -> Unit,
    onSaveExpense: () -> Unit,
    onTemplateSelected: (String, String?, Int) -> Unit
) {

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
                onClick = { onTabSelected(ExpenseEntryTab.Normal) },
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
                onClick = { onTabSelected(ExpenseEntryTab.Template) },
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
                NormalEntryContent(
                    uiState = uiState,
                    onDateChange = onDateChange,
                    onQuickAmountAdd = onQuickAmountAdd,
                    onResetAmount = onResetAmount,
                    onAmountChange = onAmountChange,
                    onCategorySelected = onCategorySelected,
                    onSubCategorySelected = onSubCategorySelected,
                    onSaveExpense = onSaveExpense,
                    modifier = Modifier.weight(1f)
                )
            }
            ExpenseEntryTab.Template -> {
                TemplateEntryContent(
                    uiState = uiState,
                    onDateChange = onDateChange,
                    onTemplateSelected = onTemplateSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalEntryContent(
    uiState: ExpenseEntryUiState,
    onDateChange: (String) -> Unit,
    onQuickAmountAdd: (Int) -> Unit,
    onResetAmount: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSubCategorySelected: (String) -> Unit,
    onSaveExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var subCategoryExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expenseEntryNormalList"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemContent {
            DatePickerField(
                dateValue = uiState.dateInput,
                onDateSelected = onDateChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
        itemContent {
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

                    uiState.quickInputAmounts.take(6).chunked(3).forEach { rowAmounts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowAmounts.forEach { amount ->
                                QuickAmountButton(
                                    amount = amount,
                                    onClick = { onQuickAmountAdd(amount) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            repeat(3 - rowAmounts.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val lastRowAmounts = uiState.quickInputAmounts.drop(6).take(2)
                        lastRowAmounts.forEach { amount ->
                            QuickAmountButton(
                                amount = amount,
                                onClick = { onQuickAmountAdd(amount) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        repeat((2 - lastRowAmounts.size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        OutlinedButton(
                            onClick = onResetAmount,
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
        }
        itemContent {
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = onAmountChange,
                label = { Text("金額", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
                placeholder = { Text("例: 1200", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                suffix = { Text("円", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)) }
            )
        }
        itemContent {
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
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                onCategorySelected(category.id)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }
        if (uiState.subCategories.isNotEmpty()) {
            itemContent {
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
                    )
                    ExposedDropdownMenu(
                        expanded = subCategoryExpanded,
                        onDismissRequest = { subCategoryExpanded = false }
                    ) {
                        uiState.subCategories.forEach { subCategory ->
                            DropdownMenuItem(
                                text = { Text(subCategory.name) },
                                onClick = {
                                    onSubCategorySelected(subCategory.id)
                                    subCategoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        itemContent {
            Button(
                onClick = onSaveExpense,
                enabled = !uiState.isSaving
            ) {
                Text("保存", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
            }
        }
    }
}

@Composable
private fun TemplateEntryContent(
    uiState: ExpenseEntryUiState,
    onDateChange: (String) -> Unit,
    onTemplateSelected: (String, String?, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expenseEntryTemplateList"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemContent {
            DatePickerField(
                dateValue = uiState.dateInput,
                onDateSelected = onDateChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.templates.isEmpty()) {
            itemContent {
                Text(
                    text = "テンプレがありません",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.templates) { template ->
                FilledTonalButton(
                    onClick = {
                        onTemplateSelected(
                            template.categoryId,
                            template.subCategoryId,
                            template.amount
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
