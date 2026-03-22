package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.data.local.preference.UserPreferencesDataStore
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val hideInitialSetupAnnouncement: StateFlow<Boolean?> = userPreferencesDataStore.hideInitialSetupAnnouncement
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val templateManagementVisited: StateFlow<Boolean> = userPreferencesDataStore.templateManagementVisited
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val shouldShowInitialSetupDialog: StateFlow<Boolean> = combine(
        hideInitialSetupAnnouncement,
        templateManagementVisited,
        budgetRepository.observeBudgetByMonth(DateTimeUtil.getCurrentMonth())
    ) { hideFlag, templateVisited, budget ->
        // ローディング中は表示しない
        if (hideFlag == null) {
            return@combine false
        }

        // ユーザーが「今後表示しない」をチェックしていれば非表示
        if (hideFlag == true) {
            return@combine false
        }

        // 未設定条件：予算未設定 OR テンプレ未訪問
        val budgetExists = budget != null
        (!budgetExists) || (!templateVisited)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun hideInitialSetupAnnouncement() {
        viewModelScope.launch {
            userPreferencesDataStore.setHideInitialSetupAnnouncement(true)
        }
    }

    fun markTemplateManagementVisited() {
        viewModelScope.launch {
            userPreferencesDataStore.setTemplateManagementVisited(true)
        }
    }
}
