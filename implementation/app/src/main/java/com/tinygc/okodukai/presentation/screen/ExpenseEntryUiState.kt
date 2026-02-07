package com.tinygc.okodukai.presentation.screen

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Template

enum class ExpenseEntryTab {
    Normal,
    Template
}

data class ExpenseEntryUiState(
    val currentTab: ExpenseEntryTab = ExpenseEntryTab.Normal,
    val amountInput: String = "",
    val dateInput: String = "",
    val categories: List<Category> = emptyList(),
    val subCategories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedSubCategoryId: String? = null,
    val templates: List<Template> = emptyList(),
    val isSaving: Boolean = false
)

sealed class ExpenseEntryUiEvent {
    data class ShowToast(val message: String) : ExpenseEntryUiEvent()
    data object Saved : ExpenseEntryUiEvent()
}
