package com.tinygc.okodukai.domain.repository

import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val isAdRemovalPurchased: Flow<Boolean>
    suspend fun purchaseAdRemoval(activityProvider: Any): Boolean
    suspend fun restorePurchase(): Boolean
    fun queryPrice(): Flow<String?>
    suspend fun initialize()
}
