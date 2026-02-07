package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AddCategoryUseCase のテスト
 * 
 * 検証項目（FR-3: カテゴリ・サブカテゴリの管理）：
 * - カテゴリ名が必須であること
 * - カテゴリが正確に保存されること
 * - サブカテゴリを作成する際は親カテゴリIDが必須であること
 * - カテゴリとサブカテゴリはそれぞれ最大10件まで
 */
class AddCategoryUseCaseTest {

    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var addCategoryUseCase: AddCategoryUseCase

    @Before
    fun setUp() {
        fakeCategoryRepository = FakeCategoryRepository()
        addCategoryUseCase = AddCategoryUseCase(fakeCategoryRepository)
    }

    @Test
    fun `正常な親カテゴリを追加できること`() = runTest {
        // Given
        val name = "食費"

        // When
        val result = addCategoryUseCase(name, null)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeCategoryRepository.categories.size)
        val savedCategory = fakeCategoryRepository.categories.first()
        assertEquals(name, savedCategory.name)
        assertNull(savedCategory.parentId)
    }

    @Test
    fun `正常なサブカテゴリを追加できること`() = runTest {
        // Given
        val parentCategory = Category("cat1", "食費", null, "", "")
        fakeCategoryRepository.addCategory(parentCategory)
        val subCategoryName = "ランチ"

        // When
        val result = addCategoryUseCase(subCategoryName, "cat1")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, fakeCategoryRepository.categories.size)
        val savedSubCategory = fakeCategoryRepository.categories.last()
        assertEquals(subCategoryName, savedSubCategory.name)
        assertEquals("cat1", savedSubCategory.parentId)
    }

    @Test
    fun `カテゴリ名が空の場合はエラーになること`() = runTest {
        // Given
        val name = ""

        // When
        val result = addCategoryUseCase(name, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("カテゴリ名を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `親カテゴリが10件を超える場合はエラーになること`() = runTest {
        // Given: 10個の親カテゴリを追加
        repeat(10) { i ->
            fakeCategoryRepository.addCategory(
                Category("cat$i", "カテゴリ$i", parentId = null, "", "")
            )
        }

        // When: 11個目を追加
        val result = addCategoryUseCase("カテゴリ11", null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("カテゴリは最大10件です", result.exceptionOrNull()?.message)
    }

    @Test
    fun `サブカテゴリが10件を超える場合はエラーになること`() = runTest {
        // Given: 親カテゴリと10個のサブカテゴリを追加
        val parentCategory = Category("cat1", "食費", parentId = null, "", "")
        fakeCategoryRepository.addCategory(parentCategory)
        repeat(10) { i ->
            fakeCategoryRepository.addCategory(
                Category("subcat$i", "サブカテゴリ$i", parentId = "cat1", "", "")
            )
        }

        // When: 11個目のサブカテゴリを追加
        val result = addCategoryUseCase("サブカテゴリ11", "cat1")

        // Then
        assertTrue(result.isFailure)
        assertEquals("サブカテゴリは最大10件です", result.exceptionOrNull()?.message)
    }

    @Test
    fun `存在しない親カテゴリIDを指定した場合はエラーになること`() = runTest {
        // Given
        val nonExistentParentId = "invalid_id"

        // When
        val result = addCategoryUseCase("サブカテゴリ", nonExistentParentId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("親カテゴリが見つかりません", result.exceptionOrNull()?.message)
    }
}

/**
 * テスト用のFakeCategoryRepository
 */
class FakeCategoryRepository : CategoryRepository {
    val categories = mutableListOf<Category>()

    fun addCategory(category: Category) {
        categories.add(category)
    }

    override suspend fun saveCategory(category: Category): Result<Unit> {
        // 制約チェック: 親カテゴリの10件制限
        if (category.parentId == null) {
            val currentCount = categories.count { it.parentId == null }
            val existing = categories.find { it.id == category.id }
            if (existing == null && currentCount >= 10) {
                return Result.failure(IllegalStateException("カテゴリは最大10件です"))
            }
        } else {
            // サブカテゴリの10件制限
            val currentCount = categories.count { it.parentId == category.parentId }
            val existing = categories.find { it.id == category.id }
            if (existing == null && currentCount >= 10) {
                return Result.failure(IllegalStateException("サブカテゴリは最大10件です"))
            }
            // 親カテゴリの存在チェック
            if (categories.none { it.id == category.parentId }) {
                return Result.failure(IllegalArgumentException("親カテゴリが見つかりません"))
            }
        }
        
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categories[index] = category
        } else {
            categories.add(category)
        }
        return Result.success(Unit)
    }

    override suspend fun getParentCategoryCount(): Result<Int> {
        return Result.success(categories.count { it.parentId == null })
    }

    override fun observeParentCategories(): Flow<List<Category>> {
        return flowOf(categories.filter { it.parentId == null })
    }

    override suspend fun getSubCategoryCount(parentId: String): Result<Int> {
        return Result.success(categories.count { it.parentId == parentId })
    }

    override fun observeSubCategories(parentId: String): Flow<List<Category>> {
        return flowOf(categories.filter { it.parentId == parentId })
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        categories.remove(category)
        return Result.success(Unit)
    }

    override suspend fun getAllCategories(): Result<List<Category>> {
        return Result.success(categories)
    }

    override suspend fun getParentCategories(): Result<List<Category>> {
        return Result.success(categories.filter { it.parentId == null })
    }

    override suspend fun getSubCategories(parentId: String): Result<List<Category>> {
        return Result.success(categories.filter { it.parentId == parentId })
    }

    override suspend fun getCategoryById(categoryId: String): Result<Category?> {
        return Result.success(categories.find { it.id == categoryId })
    }

    override fun observeAllCategories(): Flow<List<Category>> {
        return flowOf(categories)
    }
}
