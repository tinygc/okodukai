package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.usecase.category.GetParentCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.category.GetSubCategoriesUseCase
import com.tinygc.okodukai.domain.usecase.expense.AddExpenseUseCase
import com.tinygc.okodukai.domain.usecase.template.CreateExpenseFromTemplateUseCase
import com.tinygc.okodukai.domain.usecase.template.GetAllTemplatesUseCase
import com.tinygc.okodukai.domain.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getParentCategoriesUseCase: GetParentCategoriesUseCase,
    private val getSubCategoriesUseCase: GetSubCategoriesUseCase,
    private val getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val createExpenseFromTemplateUseCase: CreateExpenseFromTemplateUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExpenseEntryUiState(
            dateInput = DateTimeUtil.getCurrentDate()
        )
    )
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<ExpenseEntryUiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var subCategoryJob: Job? = null

    init {
        observeCategories()
        observeTemplates()
        applyDefaultCategory()
    }

    fun onTabSelected(tab: ExpenseEntryTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amountInput = value) }
    }

    fun onQuickAmountAdd(amount: Int) {
        val currentAmount = _uiState.value.amountInput.toIntOrNull() ?: 0
        val newAmount = currentAmount + amount
        _uiState.update { it.copy(amountInput = newAmount.toString()) }
    }

    fun onResetAmount() {
        _uiState.update { it.copy(amountInput = "") }
    }

    fun onDateChange(value: String) {
        _uiState.update { it.copy(dateInput = value) }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                selectedSubCategoryId = null,
                subCategories = emptyList()
            )
        }
        if (categoryId != null) {
            observeSubCategories(categoryId)
        }
    }

    fun onSubCategorySelected(subCategoryId: String?) {
        _uiState.update { it.copy(selectedSubCategoryId = subCategoryId) }
    }

    fun onSaveExpense() {
        val state = _uiState.value

        if (state.amountInput.isBlank()) {
            sendEvent(ExpenseEntryUiEvent.ShowToast("金額を入力してください"))
            return
        }

        val amount = state.amountInput.toIntOrNull()
        if (amount == null || amount <= 0) {
            sendEvent(ExpenseEntryUiEvent.ShowToast("0より大きい金額を入力してください"))
            return
        }

        if (state.currentTab == ExpenseEntryTab.Normal && state.selectedCategoryId == null) {
            sendEvent(ExpenseEntryUiEvent.ShowToast("カテゴリを選択してください"))
            return
        }

        if (state.dateInput.isBlank()) {
            sendEvent(ExpenseEntryUiEvent.ShowToast("日付を入力してください"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = addExpenseUseCase(
                date = state.dateInput,
                amount = amount,
                categoryId = state.selectedCategoryId,
                subCategoryId = state.selectedSubCategoryId
            )
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(ExpenseEntryUiEvent.ShowToast("支出を記録しました"))
                clearInput()
            } else {
                sendEvent(ExpenseEntryUiEvent.ShowToast("保存に失敗しました"))
            }
        }
    }

    fun onTemplateSelected(templateCategoryId: String, templateSubCategoryId: String?, templateAmount: Int) {
        viewModelScope.launch {
            val result = createExpenseFromTemplateUseCase(
                templateCategoryId = templateCategoryId,
                templateSubCategoryId = templateSubCategoryId,
                templateAmount = templateAmount
            )
            if (result.isSuccess) {
                sendEvent(ExpenseEntryUiEvent.ShowToast("記録しました"))
            } else {
                sendEvent(ExpenseEntryUiEvent.ShowToast("保存に失敗しました"))
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getParentCategoriesUseCase.observe().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun applyDefaultCategory() {
        viewModelScope.launch {
            userPreferencesDataStore.defaultCategoryId.collect { defaultId ->
                // まだカテゴリが未選択のときだけデフォルトを適用する
                if (_uiState.value.selectedCategoryId == null && defaultId != null) {
                    onCategorySelected(defaultId)
                }
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

    private fun observeSubCategories(parentId: String) {
        subCategoryJob?.cancel()
        subCategoryJob = viewModelScope.launch {
            getSubCategoriesUseCase.observe(parentId).collect { subCategories ->
                _uiState.update { it.copy(subCategories = subCategories) }
            }
        }
    }

    private fun clearInput() {
        _uiState.update {
            it.copy(
                amountInput = ""
            )
        }
    }

    private fun sendEvent(event: ExpenseEntryUiEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
