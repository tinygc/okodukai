package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * テンプレート追加ユースケース
 * 
 * ビジネスルール：
 * - テンプレートは最大10件まで
 * - カテゴリIDは必須
 * - 制約チェックはRepositoryで実施
 */
class AddTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * テンプレートを追加する
     * 
     * @param name テンプレート名
     * @param categoryId カテゴリID（必須）
     * @param subCategoryId サブカテゴリID（任意）
     * @param amount 固定金額
     * @return 追加結果
     */
    suspend operator fun invoke(
        name: String,
        categoryId: String,
        subCategoryId: String? = null,
        amount: Int
    ): Result<Unit> {
        // バリデーション
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("テンプレート名を入力してください"))
        }
        
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("金額は正の整数を入力してください"))
        }
        
        val currentDateTime = DateTimeUtil.getCurrentDateTime()
        val template = Template(
            id = DateTimeUtil.generateId(),
            name = name.trim(),
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            amount = amount,
            createdAt = currentDateTime,
            updatedAt = currentDateTime
        )
        
        return templateRepository.saveTemplate(template)
    }
}
