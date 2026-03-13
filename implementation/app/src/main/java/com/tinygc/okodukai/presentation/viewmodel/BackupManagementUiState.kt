package com.tinygc.okodukai.presentation.viewmodel

data class BackupManagementUiState(
    val accountName: String? = null,
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
