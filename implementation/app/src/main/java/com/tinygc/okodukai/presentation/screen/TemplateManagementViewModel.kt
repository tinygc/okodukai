package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.usecase.category.GetAllCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.template.AddTemplateUseCase
import com.tinygc.okodukai.domain.usecase.template.DeleteTemplateUseCase
import com.tinygc.okodukai.domain.usecase.template.GetAllTemplatesUseCase
import com.tinygc.okodukai.domain.usecase.template.ReorderTemplateUseCase
import com.tinygc.okodukai.domain.usecase.template.UpdateTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateManagementViewModel @Inject constructor(
    private val getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val addTemplateUseCase: AddTemplateUseCase,
    private val updateTemplateUseCase: UpdateTemplateUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val reorderTemplateUseCase: ReorderTemplateUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateManagementUiState())
    val uiState: StateFlow<TemplateManagementUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<TemplateManagementEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        observeTemplates()
        observeCategories()
    }

    fun addTemplate(name: String, categoryId: String, subCategoryId: String?, amount: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = addTemplateUseCase(
                name = name,
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                amount = amount
            )
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(TemplateManagementEvent.ShowToast("テンプレを保存しました"))
            } else {
                val message = result.exceptionOrNull()?.message ?: "保存に失敗しました"
                sendEvent(TemplateManagementEvent.ShowToast(message))
            }
        }
    }

    fun updateTemplate(template: Template, name: String, categoryId: String, subCategoryId: String?, amount: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = updateTemplateUseCase(
                templateId = template.id,
                name = name,
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                amount = amount
            )
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(TemplateManagementEvent.ShowToast("テンプレを保存しました"))
            } else {
                val message = result.exceptionOrNull()?.message ?: "保存に失敗しました"
                sendEvent(TemplateManagementEvent.ShowToast(message))
            }
        }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch {
            val result = deleteTemplateUseCase(template)
            if (result.isSuccess) {
                sendEvent(TemplateManagementEvent.ShowToast("テンプレを削除しました"))
            } else {
                sendEvent(TemplateManagementEvent.ShowToast("保存に失敗しました"))
            }
        }
    }

    fun reorderTemplates(templateIds: List<String>) {
        viewModelScope.launch {
            val result = reorderTemplateUseCase(templateIds)
            if (!result.isSuccess) {
                sendEvent(TemplateManagementEvent.ShowToast("並び替えに失敗しました"))
            }
        }
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            getAllTemplatesUseCase.observe().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getAllCategoriesUseCase.observe().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun sendEvent(event: TemplateManagementEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
