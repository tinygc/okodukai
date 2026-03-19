package com.tinygc.okodukai.domain.usecase.backup

import com.tinygc.okodukai.domain.repository.BackupRepository
import javax.inject.Inject

class SetDriveAccountUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    operator fun invoke(accountName: String) {
        backupRepository.setDriveAccount(accountName)
    }
}
