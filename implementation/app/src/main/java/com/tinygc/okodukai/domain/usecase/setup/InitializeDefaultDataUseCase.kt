package com.tinygc.okodukai.domain.usecase.setup

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.repository.TemplateRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * 初期カテゴリ/テンプレートの投入ユースケース
 */
class InitializeDefaultDataUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val templateRepository: TemplateRepository
) {
    suspend fun seedIfEmpty(): Result<Unit> = runCatching {
        val parentCount = categoryRepository.getParentCategoryCount().getOrThrow()
        if (parentCount == 0) {
            val seededIds = seedCategories()
            seedTemplates(seededIds)
        }
    }

    private suspend fun seedCategories(): SeededCategoryIds {
        val now = DateTimeUtil.getCurrentDateTime()
        val parentIds = mutableMapOf<String, String>()
        val subIds = mutableMapOf<String, String>()

        defaultCategories.forEach { default ->
            val parentId = DateTimeUtil.generateId()
            parentIds[default.name] = parentId

            categoryRepository.saveCategory(
                Category(
                    id = parentId,
                    name = default.name,
                    parentId = null,
                    createdAt = now,
                    updatedAt = now
                )
            ).getOrThrow()

            default.subCategories.forEach { subName ->
                val subId = DateTimeUtil.generateId()
                subIds["${default.name}:${subName}"] = subId

                categoryRepository.saveCategory(
                    Category(
                        id = subId,
                        name = subName,
                        parentId = parentId,
                        createdAt = now,
                        updatedAt = now
                    )
                ).getOrThrow()
            }
        }

        return SeededCategoryIds(parentIds, subIds)
    }

    private suspend fun seedTemplates(ids: SeededCategoryIds) {
        val templateCount = templateRepository.getTemplateCount().getOrThrow()
        if (templateCount > 0) {
            return
        }

        val now = DateTimeUtil.getCurrentDateTime()
        defaultTemplates.forEach { template ->
            val categoryId = ids.parentIds[template.parentName] ?: return@forEach
            val subCategoryId = template.subName?.let { name ->
                ids.subIds["${template.parentName}:${name}"]
            }

            templateRepository.saveTemplate(
                Template(
                    id = DateTimeUtil.generateId(),
                    name = template.name,
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    amount = template.amount,
                    createdAt = now,
                    updatedAt = now
                )
            ).getOrThrow()
        }
    }

    private data class DefaultCategory(
        val name: String,
        val subCategories: List<String>
    )

    private data class DefaultTemplate(
        val name: String,
        val parentName: String,
        val subName: String?,
        val amount: Int
    )

    private data class SeededCategoryIds(
        val parentIds: Map<String, String>,
        val subIds: Map<String, String>
    )

    private val defaultCategories = listOf(
        DefaultCategory("食費", listOf("外食", "コンビニ", "自炊")),
        DefaultCategory("交通", listOf("電車", "バス", "タクシー")),
        DefaultCategory("娯楽", listOf("映画", "ゲーム", "旅行")),
        DefaultCategory("日用品", listOf("洗剤", "ティッシュ", "文具")),
        DefaultCategory("医療", listOf("病院", "薬", "サプリ")),
        DefaultCategory("交際", listOf("飲み会", "プレゼント", "交際費")),
        DefaultCategory("光熱", listOf("電気", "ガス", "水道"))
    )

    private val defaultTemplates = listOf(
        DefaultTemplate("コンビニ 300円", "食費", "コンビニ", 300),
        DefaultTemplate("ランチ 1000円", "食費", "外食", 1000),
        DefaultTemplate("電車 200円", "交通", "電車", 200)
    )

    /**
     * 全データを削除して初期データで再シード
     */
    suspend fun reset(): Result<Unit> = runCatching {
        // 全カテゴリとテンプレートを削除
        val allCategories = categoryRepository.getAllCategories().getOrThrow()
        allCategories.forEach { category ->
            categoryRepository.deleteCategory(category).getOrThrow()
        }
        
        val allTemplates = templateRepository.getAllTemplates().getOrThrow()
        allTemplates.forEach { template ->
            templateRepository.deleteTemplate(template).getOrThrow()
        }
        
        // 初期データを再シード
        val seededIds = seedCategories()
        seedTemplates(seededIds)
    }
}
