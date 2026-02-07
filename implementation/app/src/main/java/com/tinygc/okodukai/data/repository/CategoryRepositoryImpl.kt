package com.tinygc.okodukai.data.repository

import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * カテゴリリポジトリ実装
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    companion object {
        const val MAX_PARENT_CATEGORY_COUNT = 10
        const val MAX_SUB_CATEGORY_COUNT = 10
    }

    override suspend fun saveCategory(category: Category): Result<Unit> = runCatching {
        // 制約チェック: 親カテゴリの10件制限
        if (category.parentId == null) {
            val currentCount = categoryDao.getParentCategoryCount()
            val existing = categoryDao.getById(category.id)
            if (existing == null && currentCount >= MAX_PARENT_CATEGORY_COUNT) {
                throw IllegalStateException("カテゴリは最大${MAX_PARENT_CATEGORY_COUNT}件です")
            }
        } else {
            // サブカテゴリの10件制限
            val currentCount = categoryDao.getSubCategoryCount(category.parentId)
            val existing = categoryDao.getById(category.id)
            if (existing == null && currentCount >= MAX_SUB_CATEGORY_COUNT) {
                throw IllegalStateException("サブカテゴリは最大${MAX_SUB_CATEGORY_COUNT}件です")
            }
        }
        categoryDao.insert(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> = runCatching {
        categoryDao.delete(category.toEntity())
    }

    override suspend fun getCategoryById(id: String): Result<Category?> = runCatching {
        categoryDao.getById(id)?.toDomain()
    }

    override suspend fun getParentCategories(): Result<List<Category>> = runCatching {
        categoryDao.getParentCategories().map { it.toDomain() }
    }

    override fun observeParentCategories(): Flow<List<Category>> {
        return categoryDao.getParentCategoriesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getParentCategoryCount(): Result<Int> = runCatching {
        categoryDao.getParentCategoryCount()
    }

    override suspend fun getSubCategories(parentId: String): Result<List<Category>> = runCatching {
        categoryDao.getSubCategories(parentId).map { it.toDomain() }
    }

    override fun observeSubCategories(parentId: String): Flow<List<Category>> {
        return categoryDao.getSubCategoriesFlow(parentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSubCategoryCount(parentId: String): Result<Int> = runCatching {
        categoryDao.getSubCategoryCount(parentId)
    }

    override suspend fun getAllCategories(): Result<List<Category>> = runCatching {
        categoryDao.getAll().map { it.toDomain() }
    }

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }
}
