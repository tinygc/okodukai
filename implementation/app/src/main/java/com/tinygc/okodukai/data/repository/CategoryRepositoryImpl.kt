package com.tinygc.okodukai.data.repository

import androidx.room.withTransaction
import com.tinygc.okodukai.data.local.dao.CategoryDao
import com.tinygc.okodukai.data.local.dao.CategoryOrderDao
import com.tinygc.okodukai.data.local.database.OkodukaiDatabase
import com.tinygc.okodukai.data.local.entity.CategoryOrderEntity
import com.tinygc.okodukai.data.local.mapper.toDomain
import com.tinygc.okodukai.data.local.mapper.toEntity
import java.time.ZonedDateTime
import java.util.UUID
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * カテゴリリポジトリ実装
 */
class CategoryRepositoryImpl @Inject constructor(
    private val database: OkodukaiDatabase,
    private val categoryDao: CategoryDao,
    private val categoryOrderDao: CategoryOrderDao
) : CategoryRepository {

    companion object {
        const val MAX_PARENT_CATEGORY_COUNT = 10
        const val MAX_SUB_CATEGORY_COUNT = 10
    }

    override suspend fun saveCategory(category: Category): Result<Unit> = runCatching {
        database.withTransaction {
            val existing = categoryDao.getById(category.id)

            // 制約チェック: 親カテゴリの10件制限
            if (category.parentId == null) {
                val currentCount = categoryDao.getParentCategoryCount()
                if (existing == null && currentCount >= MAX_PARENT_CATEGORY_COUNT) {
                    throw IllegalStateException("カテゴリは最大${MAX_PARENT_CATEGORY_COUNT}件です")
                }
            } else {
                // サブカテゴリの10件制限
                val currentCount = categoryDao.getSubCategoryCount(category.parentId)
                if (existing == null && currentCount >= MAX_SUB_CATEGORY_COUNT) {
                    throw IllegalStateException("サブカテゴリは最大${MAX_SUB_CATEGORY_COUNT}件です")
                }
            }

            if (existing == null) {
                categoryDao.insert(category.toEntity())

                // 新規作成時は末尾に並び順を割り当てる
                val now = ZonedDateTime.now().toString()
                val maxOrder = if (category.parentId == null) {
                    categoryOrderDao.getMaxParentDisplayOrder()
                } else {
                    categoryOrderDao.getMaxSubDisplayOrder(category.parentId)
                }
                categoryOrderDao.insert(
                    CategoryOrderEntity(
                        id = UUID.randomUUID().toString(),
                        categoryId = category.id,
                        parentId = category.parentId,
                        displayOrder = (maxOrder ?: -1) + 1,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                categoryDao.update(category.toEntity())
            }
        }
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

    override suspend fun updateCategoryOrder(categoryIdsInOrder: List<String>, parentId: String?): Result<Unit> = runCatching {
        database.withTransaction {
            val scopedCategoryIds = if (parentId == null) {
                categoryDao.getParentCategories().map { it.id }
            } else {
                categoryDao.getSubCategories(parentId).map { it.id }
            }

            if (categoryIdsInOrder.toSet() != scopedCategoryIds.toSet()) {
                throw IllegalArgumentException("並び順の対象カテゴリが不正です")
            }
            if (categoryIdsInOrder.size != scopedCategoryIds.size || categoryIdsInOrder.distinct().size != categoryIdsInOrder.size) {
                throw IllegalArgumentException("並び順の入力が不正です")
            }

            val now = ZonedDateTime.now().toString()
            categoryIdsInOrder.forEachIndexed { index, categoryId ->
                val existingOrder = categoryOrderDao.getByCategoryId(categoryId)
                if (existingOrder != null) {
                    categoryOrderDao.update(
                        existingOrder.copy(
                            parentId = parentId,
                            displayOrder = index,
                            updatedAt = now
                        )
                    )
                } else {
                    categoryOrderDao.insert(
                        CategoryOrderEntity(
                            id = UUID.randomUUID().toString(),
                            categoryId = categoryId,
                            parentId = parentId,
                            displayOrder = index,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
            }
        }
    }

    override suspend fun getParentCategoriesOrdered(): Result<List<Category>> = runCatching {
        sortCategories(categoryDao.getParentCategories(), categoryOrderDao.getParentOrders())
    }

    override fun observeParentCategoriesOrdered(): Flow<List<Category>> {
        return combine(
            categoryDao.getParentCategoriesFlow(),
            categoryOrderDao.getParentOrdersFlow()
        ) { categories, orders ->
            sortCategories(categories, orders)
        }
    }

    override suspend fun getSubCategoriesOrdered(parentId: String): Result<List<Category>> = runCatching {
        sortCategories(categoryDao.getSubCategories(parentId), categoryOrderDao.getSubOrders(parentId))
    }

    override fun observeSubCategoriesOrdered(parentId: String): Flow<List<Category>> {
        return combine(
            categoryDao.getSubCategoriesFlow(parentId),
            categoryOrderDao.getSubOrdersFlow(parentId)
        ) { categories, orders ->
            sortCategories(categories, orders)
        }
    }

    private fun sortCategories(
        categories: List<com.tinygc.okodukai.data.local.entity.CategoryEntity>,
        orders: List<CategoryOrderEntity>
    ): List<Category> {
        if (orders.isEmpty()) {
            return categories.sortedBy { it.createdAt }.map { it.toDomain() }
        }

        val orderMap = orders.associateBy { it.categoryId }
        return categories.sortedWith(
            compareBy(
                { orderMap[it.id] == null },
                { orderMap[it.id]?.displayOrder ?: Int.MAX_VALUE },
                { it.createdAt }
            )
        ).map { it.toDomain() }
    }
}
