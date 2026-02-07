package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.usecase.category.AddCategoryUseCase
import com.tinygc.okodukai.domain.usecase.category.DeleteCategoryUseCase
import com.tinygc.okodukai.domain.usecase.category.GetAllCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.category.UpdateCategoryUseCase
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
class CategoryManagementViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<CategoryManagementEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        observeCategories()
    }

    fun addParentCategory(name: String) {
        saveCategory(name = name, parentId = null)
    }

    fun addSubCategory(parentId: String, name: String) {
        saveCategory(name = name, parentId = parentId)
    }

    fun updateCategory(categoryId: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = updateCategoryUseCase(categoryId = categoryId, name = name)
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("カテゴリを保存しました"))
            } else {
                sendEvent(CategoryManagementEvent.ShowToast("保存に失敗しました"))
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            val result = deleteCategoryUseCase(category)
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("カテゴリを削除しました"))
            } else {
                sendEvent(CategoryManagementEvent.ShowToast("保存に失敗しました"))
            }
        }
    }

    private fun saveCategory(name: String, parentId: String?) {
        if (name.isBlank()) {
            sendEvent(CategoryManagementEvent.ShowToast(
                if (parentId == null) "カテゴリ名を入力してください" else "サブカテゴリ名を入力してください"
            ))
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = addCategoryUseCase(name = name, parentId = parentId)
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("カテゴリを保存しました"))
            } else {
                val message = result.exceptionOrNull()?.message ?: "保存に失敗しました"
                sendEvent(CategoryManagementEvent.ShowToast(message))
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getAllCategoriesUseCase.observe().collect { categories ->
                val parents = categories.filter { it.parentId == null }
                val subByParent = categories
                    .filter { it.parentId != null }
                    .groupBy { it.parentId!! }
                _uiState.update {
                    it.copy(
                        parents = parents,
                        subCategoriesByParentId = subByParent
                    )
                }
            }
        }
    }

    private fun sendEvent(event: CategoryManagementEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
