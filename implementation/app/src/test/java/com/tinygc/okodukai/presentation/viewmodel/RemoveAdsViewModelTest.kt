package com.tinygc.okodukai.presentation.viewmodel

import com.tinygc.okodukai.domain.MainDispatcherRule
import com.tinygc.okodukai.domain.repository.BillingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeBillingRepository : BillingRepository {
    val purchaseState = MutableStateFlow(false)
    val priceState = MutableStateFlow<String?>(null)
    var purchaseResult = true
    var restoreResult = false
    var shouldThrow = false

    override val isAdRemovalPurchased: Flow<Boolean> = purchaseState
    override fun queryPrice(): Flow<String?> = priceState

    override suspend fun purchaseAdRemoval(activityProvider: Any): Boolean {
        if (shouldThrow) throw RuntimeException("Test error")
        if (purchaseResult) {
            purchaseState.value = true
        }
        return purchaseResult
    }

    override suspend fun restorePurchase(): Boolean {
        if (shouldThrow) throw RuntimeException("Test error")
        if (restoreResult) {
            purchaseState.value = true
        }
        return restoreResult
    }

    override suspend fun initialize() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class RemoveAdsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        repository: FakeBillingRepository = FakeBillingRepository()
    ): Pair<RemoveAdsViewModel, FakeBillingRepository> {
        return RemoveAdsViewModel(repository) to repository
    }

    @Test
    fun `FR-16 初期状態で購入済みがfalseであること`() = runTest {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.first()

        assertFalse(state.isPurchased)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.showPurchaseSuccessDialog)
    }

    @Test
    fun `FR-16 購入済みの場合isPurchasedがtrueになること`() = runTest {
        val repo = FakeBillingRepository()
        repo.purchaseState.value = true
        val viewModel = RemoveAdsViewModel(repo)
        val state = viewModel.uiState.first()

        assertTrue(state.isPurchased)
    }

    @Test
    fun `FR-16 価格が取得されること`() = runTest {
        val repo = FakeBillingRepository()
        repo.priceState.value = "¥500"
        val viewModel = RemoveAdsViewModel(repo)
        val state = viewModel.uiState.first()

        assertEquals("¥500", state.price)
    }

    @Test
    fun `FR-16 成功ダイアログを閉じられること`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.dismissPurchaseSuccessDialog()
        val state = viewModel.uiState.first()

        assertFalse(state.showPurchaseSuccessDialog)
    }

    @Test
    fun `FR-16 エラーをクリアできること`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.clearError()
        val state = viewModel.uiState.first()

        assertNull(state.errorMessage)
    }

    @Test
    fun `FR-16 復元失敗時にエラーメッセージが表示されること`() = runTest {
        val repo = FakeBillingRepository()
        repo.restoreResult = false
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.restorePurchase()
        val state = viewModel.uiState.first()

        assertEquals("購入情報の取得に失敗しました", state.errorMessage)
    }

    @Test
    fun `FR-16 復元成功時に購入状態がtrueになること`() = runTest {
        val repo = FakeBillingRepository()
        repo.restoreResult = true
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.restorePurchase()
        val state = viewModel.uiState.first()

        assertTrue(state.isPurchased)
    }

    @Test
    fun `FR-16 復元成功時にshowRestoreSuccessMessageがtrueになること`() = runTest {
        val repo = FakeBillingRepository()
        repo.restoreResult = true
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.restorePurchase()
        val state = viewModel.uiState.first()

        assertTrue(state.showRestoreSuccessMessage)
    }

    @Test
    fun `FR-16 復元成功メッセージを閉じられること`() = runTest {
        val repo = FakeBillingRepository()
        repo.restoreResult = true
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.restorePurchase()
        viewModel.dismissRestoreSuccessMessage()
        val state = viewModel.uiState.first()

        assertFalse(state.showRestoreSuccessMessage)
    }

    @Test
    fun `FR-16 購入失敗時にエラーメッセージが表示されること`() = runTest {
        val repo = FakeBillingRepository()
        repo.purchaseResult = false
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.purchaseAdRemoval(Object())
        val state = viewModel.uiState.first()

        assertEquals("購入処理に失敗しました", state.errorMessage)
    }

    @Test
    fun `FR-16 購入時に例外が発生したらエラーメッセージ表示`() = runTest {
        val repo = FakeBillingRepository()
        repo.shouldThrow = true
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.purchaseAdRemoval(Object())
        val state = viewModel.uiState.first()

        assertEquals("Google Playに接続できません", state.errorMessage)
    }

    @Test
    fun `FR-16 復元時に例外が発生したらエラーメッセージ表示`() = runTest {
        val repo = FakeBillingRepository()
        repo.shouldThrow = true
        val viewModel = RemoveAdsViewModel(repo)

        viewModel.restorePurchase()
        val state = viewModel.uiState.first()

        assertEquals("Google Playに接続できません", state.errorMessage)
    }
}
