package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.usecase.category.AddCategoryUseCase
import com.tinygc.okodukai.domain.usecase.category.DeleteCategoryUseCase
import com.tinygc.okodukai.domain.usecase.category.GetOrderedParentCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.category.GetOrderedSubCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.category.UpdateCategoryOrderUseCase
import com.tinygc.okodukai.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val initializeDefaultDataUseCase: com.tinygc.okodukai.domain.usecase.setup.InitializeDefaultDataUseCase,
    private val getOrderedParentCategoriesUseCase: GetOrderedParentCategoriesUseCase,
    private val getOrderedSubCategoriesUseCase: GetOrderedSubCategoriesUseCase,
    private val updateCategoryOrderUseCase: UpdateCategoryOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<CategoryManagementEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private val subCategoryObserveJobs = mutableMapOf<String, Job>()

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

    fun reorderParentCategories(categoryIdsInOrder: List<String>) {
        viewModelScope.launch {
            val result = updateCategoryOrderUseCase(categoryIdsInOrder = categoryIdsInOrder, parentId = null)
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("並び順を保存しました"))
            } else {
                sendEvent(CategoryManagementEvent.ShowToast("並び順の保存に失敗しました"))
            }
        }
    }

    fun reorderSubCategories(parentId: String, categoryIdsInOrder: List<String>) {
        viewModelScope.launch {
            val result = updateCategoryOrderUseCase(categoryIdsInOrder = categoryIdsInOrder, parentId = parentId)
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("並び順を保存しました"))
            } else {
                sendEvent(CategoryManagementEvent.ShowToast("並び順の保存に失敗しました"))
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }
            val result = initializeDefaultDataUseCase.reset()
            _uiState.update { it.copy(isResetting = false) }
            if (result.isSuccess) {
                sendEvent(CategoryManagementEvent.ShowToast("初期値にリセットしました"))
            } else {
                sendEvent(CategoryManagementEvent.ShowToast("リセットに失敗しました"))
            }
        }
    }

    private fun saveCategory(name: String, parentId: String?) {
        if (name.isBlank()) {
            sendEvent(
                CategoryManagementEvent.ShowToast(
                    if (parentId == null) "カテゴリ名を入力してください" else "サブカテゴリ名を入力してください"
                )
            )
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
            getOrderedParentCategoriesUseCase.observe().collect { parents ->
                val parentIds = parents.map { it.id }.toSet()

                // 親一覧から外れた監視を停止
                val removedParentIds = subCategoryObserveJobs.keys - parentIds
                removedParentIds.forEach { removedId ->
                    subCategoryObserveJobs.remove(removedId)?.cancel()
                }

                // 新しい親カテゴリのサブカテゴリ監視を開始
                parents.forEach { parent ->
                    if (!subCategoryObserveJobs.containsKey(parent.id)) {
                        val job = viewModelScope.launch {
                            getOrderedSubCategoriesUseCase.observe(parent.id).collect { subs ->
                                _uiState.update { state ->
                                    state.copy(
                                        subCategoriesByParentId = state.subCategoriesByParentId.toMutableMap().apply {
                                            put(parent.id, subs)
                                        }
                                    )
                                }
                            }
                        }
                        subCategoryObserveJobs[parent.id] = job
                    }
                }

                _uiState.update {
                    it.copy(
                        parents = parents,
                        subCategoriesByParentId = it.subCategoriesByParentId
                            .filterKeys { key -> key in parentIds }
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
