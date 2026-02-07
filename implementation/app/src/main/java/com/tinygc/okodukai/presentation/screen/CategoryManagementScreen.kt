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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.model.Category

@Composable
fun CategoryManagementScreen(
    paddingValues: PaddingValues,
    viewModel: CategoryManagementViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddParentDialog by remember { mutableStateOf(false) }
    var showAddSubDialogForParentId by remember { mutableStateOf<String?>(null) }
    var editCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoryManagementEvent.ShowToast -> {
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
            Text(text = "カテゴリ管理", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) {
                Text("戻る")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { showAddParentDialog = true }) {
            Text("カテゴリ追加")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.parents) { parent ->
                val subs = uiState.subCategoriesByParentId[parent.id].orEmpty()
                CategoryGroupItem(
                    parent = parent,
                    subCategories = subs,
                    onAddSub = { showAddSubDialogForParentId = parent.id },
                    onEdit = { editCategory = it },
                    onDelete = { viewModel.deleteCategory(it) }
                )
            }
        }
    }

    if (showAddParentDialog) {
        CategoryNameDialog(
            title = "カテゴリ追加",
            onDismiss = { showAddParentDialog = false },
            onSave = { name ->
                viewModel.addParentCategory(name)
                showAddParentDialog = false
            }
        )
    }

    showAddSubDialogForParentId?.let { parentId ->
        CategoryNameDialog(
            title = "サブカテゴリ追加",
            onDismiss = { showAddSubDialogForParentId = null },
            onSave = { name ->
                viewModel.addSubCategory(parentId, name)
                showAddSubDialogForParentId = null
            }
        )
    }

    editCategory?.let { category ->
        CategoryNameDialog(
            title = "カテゴリ編集",
            initialValue = category.name,
            onDismiss = { editCategory = null },
            onSave = { name ->
                viewModel.updateCategory(category.id, name)
                editCategory = null
            }
        )
    }
}

@Composable
private fun CategoryGroupItem(
    parent: Category,
    subCategories: List<Category>,
    onAddSub: () -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = parent.name, style = MaterialTheme.typography.titleMedium)
            Row {
                IconButton(onClick = { onEdit(parent) }) {
                    androidx.compose.material3.Icon(Icons.Filled.Edit, contentDescription = "編集")
                }
                IconButton(onClick = { onDelete(parent) }) {
                    androidx.compose.material3.Icon(Icons.Filled.Delete, contentDescription = "削除")
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onAddSub) {
            Text("サブカテゴリ追加")
        }
        Spacer(modifier = Modifier.height(6.dp))
        subCategories.forEach { sub ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "- ${sub.name}")
                Row {
                    IconButton(onClick = { onEdit(sub) }) {
                        androidx.compose.material3.Icon(Icons.Filled.Edit, contentDescription = "編集")
                    }
                    IconButton(onClick = { onDelete(sub) }) {
                        androidx.compose.material3.Icon(Icons.Filled.Delete, contentDescription = "削除")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名前") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
