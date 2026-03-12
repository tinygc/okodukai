package com.tinygc.okodukai.domain.usecase.budget

import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 予算取得ユースケース
 */
class GetBudgetByMonthUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    /**
    * 指定月の予算を取得（一回限り）
    *
    * 仕様変更により、実際には毎月固定予算として最新設定値を返す
     * 
     * @param month 対象月（YYYY-MM）
     * @return 予算（存在しない場合はnull）
     */
    suspend operator fun invoke(month: String): Result<Budget?> {
        return runCatching {
            // 仕様変更により、予算は「毎月固定額」を1件保持する運用。
            // 互換性のため既存データは全件取得し、最新更新の予算を返す。
            val budgets = budgetRepository.getAllBudgets().getOrThrow()
            budgets.maxByOrNull { it.updatedAt }
        }
    }
    
    /**
    * 指定月の予算を監視（リアルタイム更新）
    *
    * 仕様変更により、実際には毎月固定予算として最新設定値を返す
     * 
     * @param month 対象月（YYYY-MM）
     * @return 予算のFlow
     */
    fun observe(month: String): Flow<Budget?> {
        return budgetRepository.observeAllBudgets().map { budgets ->
            budgets.maxByOrNull { it.updatedAt }
        }
    }
}
