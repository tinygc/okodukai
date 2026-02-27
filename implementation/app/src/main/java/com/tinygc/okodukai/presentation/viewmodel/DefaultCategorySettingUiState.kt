package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.model.Category

data class DefaultCategorySettingUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isSaving: Boolean = false,
    val savedMessage: String? = null
)
