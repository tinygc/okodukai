package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 月別臨時収入取得ユースケース
 */
class GetIncomesByMonthUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 指定月の臨時収入を取得（一回限り）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 臨時収入リスト
     */
    suspend operator fun invoke(month: String): Result<List<Income>> {
        return incomeRepository.getIncomesByMonth(month)
    }
    
    /**
     * 指定月の臨時収入を監視（リアルタイム更新）
     * 
     * @param month 対象月（YYYY-MM）
     * @return 臨時収入リストのFlow
     */
    fun observe(month: String): Flow<List<Income>> {
        return incomeRepository.observeIncomesByMonth(month)
    }
}
