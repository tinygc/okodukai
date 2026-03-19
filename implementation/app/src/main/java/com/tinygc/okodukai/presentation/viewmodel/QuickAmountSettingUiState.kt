package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.util.QuickAmountConfig

data class QuickAmountSettingUiState(
    val amountInputs: List<String> = QuickAmountConfig.defaults.map { it.toString() },
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedMessage: String? = null
)
