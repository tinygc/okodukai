package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.usecase.category.GetParentCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultCategorySettingViewModel @Inject constructor(
    private val getParentCategoriesUseCase: GetParentCategoriesUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DefaultCategorySettingUiState())
    val uiState: StateFlow<DefaultCategorySettingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getParentCategoriesUseCase.observe(),
                userPreferencesDataStore.defaultCategoryId
            ) { categories, defaultId ->
                Pair(categories, defaultId)
            }.collect { (categories, defaultId) ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        selectedCategoryId = defaultId
                    )
                }
            }
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onSave() {
        val categoryId = _uiState.value.selectedCategoryId
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userPreferencesDataStore.setDefaultCategoryId(categoryId)
            _uiState.update { it.copy(isSaving = false, savedMessage = "保存しました") }
        }
    }

    fun onSavedMessageShown() {
        _uiState.update { it.copy(savedMessage = null) }
    }
}
