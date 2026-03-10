package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * テンプレート並び替えユースケース
 * 
 * ビジネスルール：
 * - テンプレートの並び順をリスト順に更新
 * - 各テンプレートの sortOrder を 0 から始まる昇順に設定
 * - 通常は Repository レベルでトランザクション内で実行される（v1実装）
 * - 将来は `updateTemplateOrder()` 等の一括メソッドに委譲予定
 */
class ReorderTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * テンプレートを並び替える
     * 
     * @param templateIds 並び替え後のテンプレートIDリスト（0から始まる表示順序）
     * @return 並び替え結果
     */
    suspend operator fun invoke(templateIds: List<String>): Result<Unit> {
        if (templateIds.isEmpty()) {
            return Result.success(Unit)
        }

        return runCatching {
            // すべてのテンプレートを取得して、現在の状態を保持しながら sortOrder を更新
            val result = templateRepository.getAllTemplates()
            if (!result.isSuccess) {
                throw result.exceptionOrNull() ?: Exception("テンプレート取得に失敗しました")
            }

            val allTemplates = result.getOrThrow()
            val templateMap = allTemplates.associateBy { it.id }

            // 新しい sortOrder で各テンプレートを更新
            templateIds.forEachIndexed { index, templateId ->
                val template = templateMap[templateId]
                if (template != null) {
                    val updatedTemplate = template.copy(
                        sortOrder = index,
                        updatedAt = DateTimeUtil.getCurrentDateTime()
                    )
                    val updateResult = templateRepository.saveTemplate(updatedTemplate)
                    if (!updateResult.isSuccess) {
                        throw updateResult.exceptionOrNull() ?: Exception("テンプレート保存に失敗しました")
                    }
                }
            }
        }
    }
}
