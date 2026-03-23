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

internal fun calculateShouldShowInitialSetupDialog(
    hideFlag: Boolean?,
    templateVisited: Boolean,
    budgetExists: Boolean
): Boolean {
    if (hideFlag == null) {
        return false
    }
    if (hideFlag) {
        return false
    }
    return !budgetExists || !templateVisited
}

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
        calculateShouldShowInitialSetupDialog(
            hideFlag = hideFlag,
            templateVisited = templateVisited,
            budgetExists = budget != null
        )
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

    fun showInitialSetupAnnouncementAgain() {
        viewModelScope.launch {
            userPreferencesDataStore.setHideInitialSetupAnnouncement(false)
        }
    }

    fun markTemplateManagementVisited() {
        viewModelScope.launch {
            userPreferencesDataStore.setTemplateManagementVisited(true)
        }
    }
}
