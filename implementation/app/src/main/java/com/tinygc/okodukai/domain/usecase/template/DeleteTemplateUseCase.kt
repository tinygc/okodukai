package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import javax.inject.Inject

/**
 * テンプレート削除ユースケース
 */
class DeleteTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * テンプレートを削除する
     * 
     * @param template 削除対象のテンプレート
     * @return 削除結果
     */
    suspend operator fun invoke(template: Template): Result<Unit> {
        return templateRepository.deleteTemplate(template)
    }
}
