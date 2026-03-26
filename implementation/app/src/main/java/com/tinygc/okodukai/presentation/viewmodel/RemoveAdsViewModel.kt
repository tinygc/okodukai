package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoveAdsViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(RemoveAdsUiState())

    val uiState: StateFlow<RemoveAdsUiState> = combine(
        billingRepository.isAdRemovalPurchased,
        billingRepository.queryPrice(),
        _localState
    ) { isPurchased, price, local ->
        local.copy(
            isPurchased = isPurchased,
            price = price
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemoveAdsUiState()
    )

    fun purchaseAdRemoval(activity: Any) {
        viewModelScope.launch {
            _localState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val launched = billingRepository.purchaseAdRemoval(activity)
                if (!launched) {
                    _localState.update { it.copy(isLoading = false, errorMessage = "購入処理に失敗しました") }
                } else {
                    _localState.update { it.copy(isLoading = false, showPurchaseSuccessDialog = true) }
                }
            } catch (_: Exception) {
                _localState.update { it.copy(isLoading = false, errorMessage = "Google Playに接続できません") }
            }
        }
    }

    fun restorePurchase() {
        viewModelScope.launch {
            _localState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val restored = billingRepository.restorePurchase()
                if (restored) {
                    _localState.update { it.copy(isLoading = false, showRestoreSuccessMessage = true) }
                } else {
                    _localState.update { it.copy(isLoading = false, errorMessage = "購入情報の取得に失敗しました") }
                }
            } catch (_: Exception) {
                _localState.update { it.copy(isLoading = false, errorMessage = "Google Playに接続できません") }
            }
        }
    }

    fun dismissPurchaseSuccessDialog() {
        _localState.update { it.copy(showPurchaseSuccessDialog = false) }
    }

    fun dismissRestoreSuccessMessage() {
        _localState.update { it.copy(showRestoreSuccessMessage = false) }
    }

    fun clearError() {
        _localState.update { it.copy(errorMessage = null) }
    }
}
