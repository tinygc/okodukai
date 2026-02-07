package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.TemplateDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * テンプレートリポジトリ実装
 */
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao
) : TemplateRepository {

    companion object {
        const val MAX_TEMPLATE_COUNT = 10
    }

    override suspend fun saveTemplate(template: Template): Result<Unit> = runCatching {
        // 制約チェック: テンプレートの10件制限
        val currentCount = templateDao.getCount()
        val existing = templateDao.getById(template.id)
        if (existing == null && currentCount >= MAX_TEMPLATE_COUNT) {
            throw IllegalStateException("テンプレートは最大${MAX_TEMPLATE_COUNT}件です")
        }
        templateDao.insert(template.toEntity())
    }

    override suspend fun deleteTemplate(template: Template): Result<Unit> = runCatching {
        templateDao.delete(template.toEntity())
    }

    override suspend fun getTemplateById(id: String): Result<Template?> = runCatching {
        templateDao.getById(id)?.toDomain()
    }

    override suspend fun getAllTemplates(): Result<List<Template>> = runCatching {
        templateDao.getAll().map { it.toDomain() }
    }

    override fun observeAllTemplates(): Flow<List<Template>> {
        return templateDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTemplateCount(): Result<Int> = runCatching {
        templateDao.getCount()
    }

    override suspend fun getTemplatesByCategoryId(categoryId: String): Result<List<Template>> = runCatching {
        templateDao.getByCategoryId(categoryId).map { it.toDomain() }
    }
}
