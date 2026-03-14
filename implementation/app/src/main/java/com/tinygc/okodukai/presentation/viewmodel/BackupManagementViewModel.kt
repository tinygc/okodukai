package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthException
import com.tinygc.okodukai.domain.usecase.backup.ClearDriveAccountUseCase
import com.tinygc.okodukai.domain.usecase.backup.ExportBackupToDriveUseCase
import com.tinygc.okodukai.domain.usecase.backup.ImportBackupFromDriveUseCase
import com.tinygc.okodukai.domain.usecase.backup.SetDriveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupManagementViewModel @Inject constructor(
    private val setDriveAccountUseCase: SetDriveAccountUseCase,
    private val clearDriveAccountUseCase: ClearDriveAccountUseCase,
    private val exportBackupToDriveUseCase: ExportBackupToDriveUseCase,
    private val importBackupFromDriveUseCase: ImportBackupFromDriveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupManagementUiState())
    val uiState: StateFlow<BackupManagementUiState> = _uiState.asStateFlow()

    fun onAccountSelected(accountName: String) {
        setDriveAccountUseCase(accountName)
        _uiState.value = _uiState.value.copy(
            accountName = accountName,
            errorMessage = null,
            successMessage = "Googleアカウントを接続しました"
        )
    }

    fun onSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            successMessage = null
        )
    }

    fun onAccountCleared() {
        clearDriveAccountUseCase()
        _uiState.value = _uiState.value.copy(
            accountName = null,
            errorMessage = null,
            successMessage = "Googleアカウントの接続を解除しました"
        )
    }

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null)
            val result = exportBackupToDriveUseCase()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isWorking = false,
                    errorMessage = null,
                    successMessage = result.getOrNull()
                )
            } else {
                _uiState.value.copy(
                    isWorking = false,
                    successMessage = null,
                    errorMessage = toBackupErrorMessage(result.exceptionOrNull(), "バックアップに失敗しました")
                )
            }
        }
    }

    fun importBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null)
            val result = importBackupFromDriveUseCase()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isWorking = false,
                    errorMessage = null,
                    successMessage = result.getOrNull()
                )
            } else {
                _uiState.value.copy(
                    isWorking = false,
                    successMessage = null,
                    errorMessage = toBackupErrorMessage(result.exceptionOrNull(), "復元に失敗しました")
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    private fun toBackupErrorMessage(error: Throwable?, defaultMessage: String): String {
        if (error == null) return defaultMessage

        val causes = generateSequence(error) { it.cause }.toList()
        val causeMessages = causes
            .mapNotNull { it.message }
            .joinToString(" | ")

        val detailedGoogleAuthMessage = causes
            .mapNotNull { it.message }
            .firstOrNull {
                it.contains("Google認証設定エラー", ignoreCase = true) ||
                    it.contains("SHA-1=", ignoreCase = true) ||
                    it.contains("package=", ignoreCase = true)
            }
        if (detailedGoogleAuthMessage != null) {
            return detailedGoogleAuthMessage
        }

        val hasKeyError = error is GoogleAuthException || causeMessages.contains("key", ignoreCase = true)
        if (hasKeyError) {
            return "Google認証設定エラーです（このビルドを署名しているSHA-1をOAuthクライアントに追加してください）"
        }

        if (causeMessages.contains("insufficient", ignoreCase = true) ||
            causeMessages.contains("permission", ignoreCase = true)
        ) {
            return "Google Drive権限が不足しています。サインアウト後に再サインインしてください"
        }

        return error.message ?: defaultMessage
    }
}
