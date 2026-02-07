package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.Income
import kotlinx.coroutines.flow.Flow

/**
 * 臨時収入リポジトリインターフェース
 */
interface IncomeRepository {

    /**
     * 臨時収入を保存（追加または更新）
     */
    suspend fun saveIncome(income: Income): Result<Unit>

    /**
     * 臨時収入を削除
     */
    suspend fun deleteIncome(income: Income): Result<Unit>

    /**
     * IDで臨時収入を取得
     */
    suspend fun getIncomeById(id: String): Result<Income?>

    /**
     * 指定月の臨時収入を全て取得
     */
    suspend fun getIncomesByMonth(month: String): Result<List<Income>>

    /**
     * 指定月の臨時収入を監視
     */
    fun observeIncomesByMonth(month: String): Flow<List<Income>>

    /**
     * 指定月の臨時収入合計金額を取得
     */
    suspend fun getTotalIncomeByMonth(month: String): Result<Int>

    /**
     * 指定月の臨時収入合計金額を監視
     */
    fun observeTotalIncomeByMonth(month: String): Flow<Int>

    /**
     * 全臨時収入を取得
     */
    suspend fun getAllIncomes(): Result<List<Income>>

    /**
     * 全臨時収入を監視
     */
    fun observeAllIncomes(): Flow<List<Income>>
}
