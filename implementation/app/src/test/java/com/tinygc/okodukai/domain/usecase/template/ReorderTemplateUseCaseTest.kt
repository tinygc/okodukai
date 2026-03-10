package com.tinygc.okodukai.domain.usecase.template

import com.tinygc.okodukai.domain.model.Template
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ReorderTemplateUseCase のテスト
 * 
 * 検証項目（FR-10: テンプレ並び替え）：
 * - テンプレートを並び替えられること
 * - 並び替え後の sortOrder がリスト順に設定されること
 * - 空のリストを渡した場合は成功すること
 */
class ReorderTemplateUseCaseTest {

    private lateinit var fakeTemplateRepository: FakeTemplateRepository
    private lateinit var reorderTemplateUseCase: ReorderTemplateUseCase

    @Before
    fun setUp() {
        fakeTemplateRepository = FakeTemplateRepository()
        reorderTemplateUseCase = ReorderTemplateUseCase(fakeTemplateRepository)
    }

    @Test
    fun `テンプレートを並び替えられること`() = runTest {
        // Given - 3 つのテンプレートを作成
        val template1 = Template("t1", "昼食", "cat1", null, 1000, sortOrder = 0, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        val template2 = Template("t2", "おやつ", "cat2", null, 500, sortOrder = 1, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        val template3 = Template("t3", "コーヒー", "cat3", null, 300, sortOrder = 2, "2024-01-01T00:00:00", "2024-01-01T00:00:00")

        fakeTemplateRepository.templates.addAll(listOf(template1, template2, template3))

        // When - 順序を逆にして並び替え
        val newOrder = listOf("t3", "t2", "t1")
        val result = reorderTemplateUseCase(newOrder)

        // Then
        assertTrue(result.isSuccess)
        val t3 = fakeTemplateRepository.templates.find { it.id == "t3" }!!
        val t2 = fakeTemplateRepository.templates.find { it.id == "t2" }!!
        val t1 = fakeTemplateRepository.templates.find { it.id == "t1" }!!
        
        assertEquals(0, t3.sortOrder)
        assertEquals(1, t2.sortOrder)
        assertEquals(2, t1.sortOrder)
    }

    @Test
    fun `sortOrder が昇順に設定されること`() = runTest {
        // Given
        val templates = (0..4).map { i ->
            Template("t$i", "テンプレ$i", "cat$i", null, 1000 + i, sortOrder = i, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        }
        fakeTemplateRepository.templates.addAll(templates)

        // When - ランダムな順序で並び替え
        val newOrder = listOf("t4", "t1", "t3", "t0", "t2")
        val result = reorderTemplateUseCase(newOrder)

        // Then
        assertTrue(result.isSuccess)
        newOrder.forEachIndexed { expectedOrder, templateId ->
            val template = fakeTemplateRepository.templates.find { it.id == templateId }!!
            assertEquals("Template $templateId の sortOrder が $expectedOrder でない", expectedOrder, template.sortOrder)
        }
    }

    @Test
    fun `空のリストを並び替えても成功すること`() = runTest {
        // Given
        val template1 = Template("t1", "テンプレ1", "cat1", null, 1000, sortOrder = 0, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        fakeTemplateRepository.templates.add(template1)

        // When
        val result = reorderTemplateUseCase(emptyList())

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `存在しないテンプレートIDを含む場合、スキップされること`() = runTest {
        // Given
        val template1 = Template("t1", "昼食", "cat1", null, 1000, sortOrder = 0, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        val template2 = Template("t2", "おやつ", "cat2", null, 500, sortOrder = 1, "2024-01-01T00:00:00", "2024-01-01T00:00:00")
        fakeTemplateRepository.templates.addAll(listOf(template1, template2))

        // When - 存在しないID "t99" を含む
        val newOrder = listOf("t2", "t99", "t1")
        val result = reorderTemplateUseCase(newOrder)

        // Then
        assertTrue(result.isSuccess)
        val t1 = fakeTemplateRepository.templates.find { it.id == "t1" }!!
        val t2 = fakeTemplateRepository.templates.find { it.id == "t2" }!!
        
        // "t2" がインデックス 0 に、"t99" はスキップされて "t1" がインデックス 2 になる
        assertEquals(0, t2.sortOrder)
        assertEquals(2, t1.sortOrder)
    }
}
