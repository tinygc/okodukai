package com.tinygc.okodukai.domain.repository

interface BackupRepository {
    fun setDriveAccount(accountName: String)
    fun clearDriveAccount()
    suspend fun exportToDriveAppData(): Result<String>
    suspend fun importFromDriveAppData(): Result<String>
}
