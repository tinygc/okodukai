package com.tinygc.okodukai.domain.usecase.budget

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 月別予算取得ユースケース
 */
class GetBudgetByMonthUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    /**
     * 指定月の予算を取得（一回限り）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 予算（存在しない場合はnull）
     */
    suspend operator fun invoke(month: String): Result<Budget?> {
        return budgetRepository.getBudgetByMonth(month)
    }
    
    /**
     * 指定月の予算を監視（リアルタイム更新）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 予算のFlow
     */
    fun observe(month: String): Flow<Budget?> {
        return budgetRepository.observeBudgetByMonth(month)
    }
}
