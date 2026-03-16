package com.tinygc.okodukai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.okodukai.BuildConfig
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

        // Repo が認証設定エラーとしてタグ付けしたメッセージを処理
        val authConfigMessage = causes
            .mapNotNull { it.message }
            .firstOrNull { it.contains("Google認証設定エラー", ignoreCase = true) }
        if (authConfigMessage != null) {
            return if (BuildConfig.DEBUG) authConfigMessage
            else "Google認証設定エラーです（このビルドを署名しているSHA-1をOAuthクライアントに追加してください）"
        }

        val importSpecificMessage = causes
            .mapNotNull { it.message }
            .firstOrNull {
                it == "バックアップファイルが見つかりません" ||
                    it == "バックアップ形式が不正です" ||
                    it == "カテゴリデータが不正です" ||
                    it == "支出データが不正です" ||
                    it == "backupPolicy に必須キーが不足しています" ||
                    it.startsWith("backupPolicy の値が不正です") ||
                    it == "settings 以外の EXCLUDED は未対応です"
            }
        if (importSpecificMessage != null) {
            return importSpecificMessage
        }

        val importStepMessage = causes
            .mapNotNull { it.message }
            .firstOrNull { it.startsWith("インポート失敗:") }
        if (importStepMessage != null) {
            return importStepMessage
        }

        if (causeMessages.contains("insufficient", ignoreCase = true) ||
            causeMessages.contains("permission", ignoreCase = true)
        ) {
            return "Google Drive権限が不足しています。サインアウト後に再サインインしてください"
        }

        if (causeMessages.contains("timeout", ignoreCase = true) ||
            causeMessages.contains("network", ignoreCase = true)
        ) {
            return "通信エラーが発生しました。ネットワーク状態を確認して再試行してください"
        }

        return defaultMessage
    }
}
