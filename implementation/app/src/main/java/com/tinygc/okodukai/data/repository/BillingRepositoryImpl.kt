package com.tinygc.okodukai.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.tinygc.okodukai.domain.repository.BillingRepository
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository {

    companion object {
        private const val TAG = "BillingRepository"
        const val PRODUCT_ID = "remove_ads"
    }

    private val _purchaseState = MutableStateFlow(false)
    private val _price = MutableStateFlow<String?>(null)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _purchaseState.value = true
                    acknowledgePurchaseIfNeeded(purchase)
                }
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    override val isAdRemovalPurchased: Flow<Boolean> = _purchaseState

    override fun queryPrice(): Flow<String?> = _price

    override suspend fun purchaseAdRemoval(activityProvider: Any): Boolean {
        if (activityProvider !is Activity) return false
        ensureConnected()
        val productDetails = queryProductDetails() ?: return false
        productDetails.oneTimePurchaseOfferDetails ?: return false
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()
        val result = billingClient.launchBillingFlow(activityProvider, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override suspend fun restorePurchase(): Boolean {
        ensureConnected()
        return queryExistingPurchases()
    }

    override suspend fun initialize() {
        ensureConnected()
        queryExistingPurchases()
        loadPrice()
    }

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        val connected = suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (continuation.isActive) {
                        val success = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                        if (!success) {
                            Log.w(TAG, "Billing connection failed: ${billingResult.debugMessage}")
                        }
                        continuation.resume(success)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) {
                        Log.w(TAG, "Billing service disconnected during setup")
                        continuation.resume(false)
                    }
                }
            })
        }
        if (!connected) {
            throw IllegalStateException("Failed to connect to Google Play Billing")
        }
    }

    private suspend fun queryExistingPurchases(): Boolean {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val purchases = suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
        val purchased = purchases.any { purchase ->
            purchase.products.contains(PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        _purchaseState.value = purchased
        if (purchased) {
            purchases
                .filter { it.products.contains(PRODUCT_ID) }
                .forEach { acknowledgePurchaseIfNeeded(it) }
        }
        return purchased
    }

    private suspend fun loadPrice() {
        val productDetails = queryProductDetails()
        _price.value = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    private suspend fun queryProductDetails(): ProductDetails? {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(productDetailsList.firstOrNull())
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "Failed to acknowledge purchase: ${result.debugMessage}")
                }
            }
        }
    }
}
