package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 親カテゴリ取得ユースケース
 */
class GetParentCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 親カテゴリを取得（一回限り）
     * 
     * @return 親カテゴリリスト
     */
    suspend operator fun invoke(): Result<List<Category>> {
        return categoryRepository.getParentCategories()
    }
    
    /**
     * 親カテゴリを監視（リアルタイム更新）
     * 
     * @return 親カテゴリリストのFlow
     */
    fun observe(): Flow<List<Category>> {
        return categoryRepository.observeParentCategories()
    }
}
