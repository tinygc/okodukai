package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.util.QuickAmountConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QuickAmountSettingViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAmountSettingUiState())
    val uiState: StateFlow<QuickAmountSettingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.quickInputAmounts.collect { amounts ->
                _uiState.update { it.copy(amountInputs = amounts.map(Int::toString)) }
            }
        }
    }

    fun onAmountChange(index: Int, value: String) {
        if (index !in 0 until QuickAmountConfig.SLOT_COUNT) {
            return
        }
        val next = _uiState.value.amountInputs.toMutableList()
        next[index] = value
        _uiState.update { it.copy(amountInputs = next, errorMessage = null) }
    }

    fun onResetToDefault() {
        _uiState.update {
            it.copy(
                amountInputs = QuickAmountConfig.defaults.map(Int::toString),
                errorMessage = null
            )
        }
    }

    fun onSave() {
        val inputs = _uiState.value.amountInputs
        when (QuickAmountConfig.validateInputStrings(inputs)) {
            QuickAmountConfig.ValidationError.EMPTY -> {
                _uiState.update { it.copy(errorMessage = "すべての金額を入力してください") }
                return
            }
            QuickAmountConfig.ValidationError.INVALID -> {
                _uiState.update { it.copy(errorMessage = "1以上99999以下の整数を入力してください") }
                return
            }
            null -> Unit
        }

        val values = QuickAmountConfig.parseInputStrings(inputs) ?: run {
            _uiState.update { it.copy(errorMessage = "1以上99999以下の整数を入力してください") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                userPreferencesDataStore.setQuickInputAmounts(values)
            }.onSuccess {
                _uiState.update { it.copy(savedMessage = "保存しました") }
            }.onFailure {
                _uiState.update { it.copy(errorMessage = "保存に失敗しました") }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun onSavedMessageShown() {
        _uiState.update { it.copy(savedMessage = null) }
    }
}
