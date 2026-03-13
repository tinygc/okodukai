package com.tinygc.okodukai.domain.usecase.backup

import com.tinygc.okodukai.domain.repository.BackupRepository
import javax.inject.Inject

class ImportBackupFromDriveUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<String> {
        return backupRepository.importFromDriveAppData()
    }
}
