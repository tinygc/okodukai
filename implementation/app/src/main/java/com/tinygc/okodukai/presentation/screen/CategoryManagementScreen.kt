package com.tinygc.okodukai.presentation.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.model.Category
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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

    var displayParents by remember { mutableStateOf(uiState.parents) }
    var isParentDragging by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIdx = from.index - 1
        val toIdx = to.index - 1
        if (fromIdx < 0 || toIdx < 0 || fromIdx >= displayParents.size || toIdx >= displayParents.size) return@rememberReorderableLazyListState
        displayParents = displayParents.toMutableList().apply {
            add(toIdx, removeAt(fromIdx))
        }
    }

    LaunchedEffect(uiState.parents) {
        if (!isParentDragging) {
            displayParents = uiState.parents
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoryManagementEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "カテゴリ管理",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    TextButton(onClick = onBack) {
                        Text(
                            text = "戻る",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { showAddParentDialog = true }) {
                        Text(
                            text = "カテゴリ追加",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                    Button(onClick = { viewModel.resetToDefaults() }) {
                        Text(
                            text = "リセット",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                }
            }
        }

        itemsIndexed(
            items = displayParents,
            key = { _, parent -> parent.id }
        ) { index, parent ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = parent.id
            ) { isDragging ->
                val scope = this
                SlideInItem(delayMillis = index * 50) {
                    val subs = uiState.subCategoriesByParentId[parent.id].orEmpty()
                    CategoryGroupItem(
                        scope = scope,
                        parent = parent,
                        subCategories = subs,
                        isDragging = isDragging,
                        onAddSub = { showAddSubDialogForParentId = parent.id },
                        onEdit = { editCategory = it },
                        onDelete = { viewModel.deleteCategory(it) },
                        onReorderSub = { ids -> viewModel.reorderSubCategories(parent.id, ids) },
                        onParentDragStarted = { isParentDragging = true },
                        onParentDragStopped = {
                            isParentDragging = false
                            viewModel.reorderParentCategories(displayParents.map { it.id })
                        }
                    )
                }
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
    scope: ReorderableCollectionItemScope,
    parent: Category,
    subCategories: List<Category>,
    isDragging: Boolean,
    onAddSub: () -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onReorderSub: (List<String>) -> Unit,
    onParentDragStarted: () -> Unit,
    onParentDragStopped: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = if (isDragging) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = parent.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Icon(
                        Icons.Filled.DragHandle,
                        contentDescription = "並び替え",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = with(scope) {
                            Modifier
                                .padding(12.dp)
                                .draggableHandle(
                                    onDragStarted = { onParentDragStarted() },
                                    onDragStopped = { onParentDragStopped() }
                                )
                        }
                    )
                    IconButton(onClick = { onEdit(parent) }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "編集",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(parent) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAddSub) {
                Text(
                    text = "サブカテゴリ追加",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ReorderableColumn(
                list = subCategories,
                onSettle = { fromIndex, toIndex ->
                    val reordered = subCategories.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                    onReorderSub(reordered.map { it.id })
                },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) { _, sub, subDragging ->
                ReorderableItem {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "- ${sub.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = if (subDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (subDragging) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Row {
                            Icon(
                                Icons.Filled.DragHandle,
                                contentDescription = "並び替え",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .draggableHandle()
                            )
                            IconButton(onClick = { onEdit(sub) }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "編集",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDelete(sub) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "削除",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideInItem(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = delayMillis)) +
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(durationMillis = 260, delayMillis = delayMillis)
            )
    ) {
        content()
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
