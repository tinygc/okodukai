package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 並び順を考慮したサブカテゴリ取得ユースケース
 */
class GetOrderedSubCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 並び順を考慮してサブカテゴリを取得
     * 並び順が設定されていない場合は作成日時順で返す
     * 
     * @param parentId 親カテゴリID
     * @return サブカテゴリリスト（並び順済み）
     */
    suspend operator fun invoke(parentId: String): Result<List<Category>> {
        return categoryRepository.getSubCategoriesOrdered(parentId)
    }
    
    /**
     * 並び順を考慮してサブカテゴリを監視
     * 
     * @param parentId 親カテゴリID
     * @return サブカテゴリリストのFlow（並び順済み）
     */
    fun observe(parentId: String): Flow<List<Category>> {
        return categoryRepository.observeSubCategoriesOrdered(parentId)
    }
}
