package com.tinygc.okodukai.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.domain.model.GoalAchievementMode
import com.tinygc.okodukai.domain.model.SavingGoal
import com.tinygc.okodukai.domain.usecase.saving.DeleteSavingGoalUseCase
import com.tinygc.okodukai.domain.usecase.saving.GetAllSavingGoalsUseCase
import com.tinygc.okodukai.domain.usecase.saving.GetGoalAchievementModeUseCase
import com.tinygc.okodukai.domain.usecase.saving.SaveSavingGoalUseCase
import com.tinygc.okodukai.domain.usecase.saving.SetGoalAchievementModeUseCase
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
class SavingGoalManagementViewModel @Inject constructor(
    private val getAllSavingGoalsUseCase: GetAllSavingGoalsUseCase,
    private val saveSavingGoalUseCase: SaveSavingGoalUseCase,
    private val deleteSavingGoalUseCase: DeleteSavingGoalUseCase,
    private val getGoalAchievementModeUseCase: GetGoalAchievementModeUseCase,
    private val setGoalAchievementModeUseCase: SetGoalAchievementModeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingGoalManagementUiState())
    val uiState: StateFlow<SavingGoalManagementUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<SavingGoalManagementEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        observeGoals()
        observeMode()
    }

    fun addGoal(name: String, targetAmount: Int, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = saveSavingGoalUseCase.create(name, targetAmount, isActive)
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(SavingGoalManagementEvent.ShowToast("貯金目標を保存しました"))
            } else {
                sendEvent(
                    SavingGoalManagementEvent.ShowToast(
                        result.exceptionOrNull()?.message ?: "保存に失敗しました"
                    )
                )
            }
        }
    }

    fun updateGoal(goal: SavingGoal, name: String, targetAmount: Int, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = saveSavingGoalUseCase.update(goal, name, targetAmount, isActive)
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                sendEvent(SavingGoalManagementEvent.ShowToast("貯金目標を保存しました"))
            } else {
                sendEvent(
                    SavingGoalManagementEvent.ShowToast(
                        result.exceptionOrNull()?.message ?: "保存に失敗しました"
                    )
                )
            }
        }
    }

    fun deleteGoal(goal: SavingGoal) {
        viewModelScope.launch {
            val result = deleteSavingGoalUseCase(goal)
            if (result.isSuccess) {
                sendEvent(SavingGoalManagementEvent.ShowToast("貯金目標を削除しました"))
            } else {
                sendEvent(SavingGoalManagementEvent.ShowToast("削除に失敗しました"))
            }
        }
    }

    fun setMode(mode: GoalAchievementMode) {
        viewModelScope.launch {
            setGoalAchievementModeUseCase(mode)
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            getAllSavingGoalsUseCase.observe().collect { goals ->
                _uiState.update { it.copy(goals = goals) }
            }
        }
    }

    private fun observeMode() {
        viewModelScope.launch {
            getGoalAchievementModeUseCase.observe().collect { mode ->
                _uiState.update { it.copy(mode = mode) }
            }
        }
    }

    private fun sendEvent(event: SavingGoalManagementEvent) {
        viewModelScope.launch {
            eventChannel.send(event)
        }
    }
}
