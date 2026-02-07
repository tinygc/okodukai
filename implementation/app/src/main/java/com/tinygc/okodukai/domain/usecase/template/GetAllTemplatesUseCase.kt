package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 全テンプレート取得ユースケース
 */
class GetAllTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * 全テンプレートを取得（一回限り）
     * 
     * @return テンプレートリスト
     */
    suspend operator fun invoke(): Result<List<Template>> {
        return templateRepository.getAllTemplates()
    }
    
    /**
     * 全テンプレートを監視（リアルタイム更新）
     * 
     * @return テンプレートリストのFlow
     */
    fun observe(): Flow<List<Template>> {
        return templateRepository.observeAllTemplates()
    }
}
