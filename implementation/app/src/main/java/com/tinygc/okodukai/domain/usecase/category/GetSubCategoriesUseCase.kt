package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * サブカテゴリ取得ユースケース
 */
class GetSubCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * サブカテゴリを取得（一回限り）
     * 
     * @param parentId 親カテゴリID
     * @return サブカテゴリリスト
     */
    suspend operator fun invoke(parentId: String): Result<List<Category>> {
        return categoryRepository.getSubCategories(parentId)
    }
    
    /**
     * サブカテゴリを監視（リアルタイム更新）
     * 
     * @param parentId 親カテゴリID
     * @return サブカテゴリリストのFlow
     */
    fun observe(parentId: String): Flow<List<Category>> {
        return categoryRepository.observeSubCategories(parentId)
    }
}
