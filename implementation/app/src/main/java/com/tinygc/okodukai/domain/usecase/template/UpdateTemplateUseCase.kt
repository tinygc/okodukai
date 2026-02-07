package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * テンプレート更新ユースケース
 */
class UpdateTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * テンプレートを更新する
     * 
     * @param templateId 更新対象のテンプレートID
     * @param name テンプレート名
     * @param categoryId カテゴリID（必須）
     * @param subCategoryId サブカテゴリID（任意）
     * @param amount 固定金額
     * @return 更新結果
     */
    suspend operator fun invoke(
        templateId: String,
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
        
        // 既存のテンプレートを取得
        val existingTemplateResult = templateRepository.getTemplateById(templateId)
        if (existingTemplateResult.isFailure) {
            return Result.failure(existingTemplateResult.exceptionOrNull()!!)
        }
        
        val existingTemplate = existingTemplateResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("指定されたテンプレートが見つかりません"))
        
        val updatedTemplate = Template(
            id = templateId,
            name = name.trim(),
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            amount = amount,
            createdAt = existingTemplate.createdAt,
            updatedAt = DateTimeUtil.getCurrentDateTime()
        )
        
        return templateRepository.saveTemplate(updatedTemplate)
    }
}
