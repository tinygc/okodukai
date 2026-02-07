package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.Template
import kotlinx.coroutines.flow.Flow

/**
 * テンプレートリポジトリインターフェース
 */
interface TemplateRepository {

    /**
     * テンプレートを保存（追加または更新）
     */
    suspend fun saveTemplate(template: Template): Result<Unit>

    /**
     * テンプレートを削除
     */
    suspend fun deleteTemplate(template: Template): Result<Unit>

    /**
     * IDでテンプレートを取得
     */
    suspend fun getTemplateById(id: String): Result<Template?>

    /**
     * 全テンプレートを取得
     */
    suspend fun getAllTemplates(): Result<List<Template>>

    /**
     * 全テンプレートを監視
     */
    fun observeAllTemplates(): Flow<List<Template>>

    /**
     * テンプレート件数を取得
     */
    suspend fun getTemplateCount(): Result<Int>

    /**
     * 指定カテゴリのテンプレートを取得
     */
    suspend fun getTemplatesByCategoryId(categoryId: String): Result<List<Template>>
}
