package com.tinygc.okodukai.presentation.viewmodel

data class RemoveAdsUiState(
    val isPurchased: Boolean = false,
    val price: String? = null,
    val isLoading: Boolean = false,
    val showPurchaseSuccessDialog: Boolean = false,
    val showRestoreSuccessMessage: Boolean = false,
    val errorMessage: String? = null
)
