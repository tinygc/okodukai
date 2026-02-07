package com.tinygc.okodukai.domain.usecase.income

import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.repository.IncomeRepository
import javax.inject.Inject

/**
 * 臨時収入削除ユースケース
 */
class DeleteIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    /**
     * 臨時収入を削除する
     * 
     * @param income 削除対象の臨時収入
     * @return 削除結果
     */
    suspend operator fun invoke(income: Income): Result<Unit> {
        return incomeRepository.deleteIncome(income)
    }
}
