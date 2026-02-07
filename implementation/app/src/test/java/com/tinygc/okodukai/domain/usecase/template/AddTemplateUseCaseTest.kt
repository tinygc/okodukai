package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import com.tinygc.okodukai.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AddTemplateUseCase のテスト
 * 
 * 検証項目（FR-9: テンプレ管理）：
 * - テンプレが正確に保存されること
 * - 名前が必須であること
 * - カテゴリIDが必須であること
 * - 固定金額が正の整数であること
 */
class AddTemplateUseCaseTest {

    private lateinit var fakeTemplateRepository: FakeTemplateRepository
    private lateinit var addTemplateUseCase: AddTemplateUseCase

    @Before
    fun setUp() {
        fakeTemplateRepository = FakeTemplateRepository()
        addTemplateUseCase = AddTemplateUseCase(fakeTemplateRepository)
    }

    @Test
    fun `正常なテンプレを追加できること`() = runTest {
        // Given
        val name = "ランチ"
        val categoryId = "cat1"
        val amount = 1000

        // When
        val result = addTemplateUseCase(name, categoryId, null, amount)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, fakeTemplateRepository.templates.size)
        val savedTemplate = fakeTemplateRepository.templates.first()
        assertEquals(name, savedTemplate.name)
        assertEquals(categoryId, savedTemplate.categoryId)
        assertEquals(amount, savedTemplate.amount)
    }

    @Test
    fun `サブカテゴリを指定してテンプレを追加できること`() = runTest {
        // Given
        val name = "コンビニランチ"
        val categoryId = "cat1"
        val subCategoryId = "subcat1"
        val amount = 600

        // When
        val result = addTemplateUseCase(name, categoryId, subCategoryId, amount)

        // Then
        assertTrue(result.isSuccess)
        val savedTemplate = fakeTemplateRepository.templates.first()
        assertEquals(subCategoryId, savedTemplate.subCategoryId)
    }

    @Test
    fun `名前が空の場合はエラーになること`() = runTest {
        // Given
        val name = ""
        val categoryId = "cat1"
        val amount = 1000

        // When
        val result = addTemplateUseCase(name, categoryId, null, amount)

        // Then
        assertTrue(result.isFailure)
        assertEquals("テンプレート名を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `固定金額が0の場合はエラーになること`() = runTest {
        // Given
        val name = "ランチ"
        val categoryId = "cat1"
        val amount = 0

        // When
        val result = addTemplateUseCase(name, categoryId, null, amount)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }

    @Test
    fun `固定金額が負の値の場合はエラーになること`() = runTest {
        // Given
        val name = "ランチ"
        val categoryId = "cat1"
        val amount = -500

        // When
        val result = addTemplateUseCase(name, categoryId, null, amount)

        // Then
        assertTrue(result.isFailure)
        assertEquals("金額は正の整数を入力してください", result.exceptionOrNull()?.message)
    }
}

/**
 * テスト用のFakeTemplateRepository
 */
class FakeTemplateRepository : TemplateRepository {
    val templates = mutableListOf<Template>()

    override suspend fun saveTemplate(template: Template): Result<Unit> {
        // 制約チェック: テンプレートの10件制限
        val currentCount = templates.size
        val existing = templates.find { it.id == template.id }
        if (existing == null && currentCount >= 10) {
            return Result.failure(IllegalStateException("テンプレートは最大10件です"))
        }
        
        val index = templates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            templates[index] = template
        } else {
            templates.add(template)
        }
        return Result.success(Unit)
    }

    override suspend fun getTemplateById(id: String): Result<Template?> {
        return Result.success(templates.find { it.id == id })
    }

    override suspend fun getTemplateCount(): Result<Int> {
        return Result.success(templates.size)
    }

    override suspend fun deleteTemplate(template: Template): Result<Unit> {
        templates.remove(template)
        return Result.success(Unit)
    }

    override suspend fun getAllTemplates(): Result<List<Template>> {
        return Result.success(templates)
    }

    override fun observeAllTemplates(): Flow<List<Template>> {
        return flowOf(templates)
    }

    override suspend fun getTemplatesByCategoryId(categoryId: String): Result<List<Template>> {
        return Result.success(templates.filter { it.categoryId == categoryId })
    }
}
