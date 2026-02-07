package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.usecase.expense.AddExpenseUseCase
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * テンプレートから支出を作成するユースケース
 * 
 * テンプレートを選択することで、カテゴリと金額が自動入力された支出を作成する
 */
class CreateExpenseFromTemplateUseCase @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase
) {
    /**
     * テンプレートから支出を作成する
     * 
     * @param templateCategoryId テンプレートのカテゴリID
     * @param templateSubCategoryId テンプレートのサブカテゴリID
     * @param templateAmount テンプレートの金額
     * @param date 支出日（省略時は当日）
     * @param memo メモ（任意）
     * @return 作成結果
     */
    suspend operator fun invoke(
        templateCategoryId: String,
        templateSubCategoryId: String?,
        templateAmount: Int,
        date: String = DateTimeUtil.getCurrentDate(),
        memo: String? = null
    ): Result<Unit> {
        return addExpenseUseCase(
            date = date,
            amount = templateAmount,
            categoryId = templateCategoryId,
            subCategoryId = templateSubCategoryId,
            memo = memo
        )
    }
}
