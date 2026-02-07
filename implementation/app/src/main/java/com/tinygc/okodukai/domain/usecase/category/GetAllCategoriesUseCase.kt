package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 全カテゴリ取得ユースケース
 */
class GetAllCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 全カテゴリを取得（一回限り）
     */
    suspend operator fun invoke(): Result<List<Category>> {
        return categoryRepository.getAllCategories()
    }

    /**
     * 全カテゴリを監視（リアルタイム更新）
     */
    fun observe(): Flow<List<Category>> {
        return categoryRepository.observeAllCategories()
    }
}
