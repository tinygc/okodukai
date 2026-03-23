package com.tinygc.okodukai.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.presentation.viewmodel.DefaultCategorySettingViewModel

private inline fun LazyListScope.itemContent(crossinline content: @Composable () -> Unit) {
    item { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultCategorySettingScreen(
    paddingValues: PaddingValues,
    viewModel: DefaultCategorySettingViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.onSavedMessageShown()
        }
    }

    DefaultCategorySettingContent(
        paddingValues = paddingValues,
        uiState = uiState,
        onBack = onBack,
        onCategorySelected = viewModel::onCategorySelected,
        onSave = viewModel::onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DefaultCategorySettingContent(
    paddingValues: PaddingValues,
    uiState: com.tinygc.okodukai.presentation.viewmodel.DefaultCategorySettingUiState,
    onBack: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSave: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("defaultCategorySettingList"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemContent {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "デフォルトカテゴリ設定",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onBack) {
                    Text(
                        text = "戻る",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                    )
                }
            }
        }

        itemContent {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "支出入力を開いたときに自動で選択されるカテゴリを設定できます。",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        itemContent {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "デフォルトカテゴリ",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.SemiBold
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        val selectedCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "なし（未設定）",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("カテゴリ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("なし（未設定）") },
                                onClick = {
                                    onCategorySelected(null)
                                    categoryExpanded = false
                                }
                            )
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onSave,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uiState.isSaving) "保存中..." else "設定を保存",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}
