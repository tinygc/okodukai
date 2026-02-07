package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 月別臨時収入合計取得ユースケース
 */
class GetTotalIncomeByMonthUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 指定月の臨時収入合計を取得（一回限り）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 臨時収入合計金額
     */
    suspend operator fun invoke(month: String): Result<Int> {
        return incomeRepository.getTotalIncomeByMonth(month)
    }
    
    /**
     * 指定月の臨時収入合計を監視（リアルタイム更新）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 臨時収入合計金額のFlow
     */
    fun observe(month: String): Flow<Int> {
        return incomeRepository.observeTotalIncomeByMonth(month)
    }
}
