package com.tinygc.okodukai.presentation.screen

import com.tinygc.okodukai.domain.model.Category

data class CategoryManagementUiState(
    val parents: List<Category> = emptyList(),
    val subCategoriesByParentId: Map<String, List<Category>> = emptyMap(),
    val isSaving: Boolean = false
)

sealed class CategoryManagementEvent {
    data class ShowToast(val message: String) : CategoryManagementEvent()
}
