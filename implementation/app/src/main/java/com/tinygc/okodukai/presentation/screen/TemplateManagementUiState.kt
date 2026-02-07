package com.tinygc.okodukai.presentation.screen

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Template

data class TemplateManagementUiState(
    val templates: List<Template> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false
)

sealed class TemplateManagementEvent {
    data class ShowToast(val message: String) : TemplateManagementEvent()
}
