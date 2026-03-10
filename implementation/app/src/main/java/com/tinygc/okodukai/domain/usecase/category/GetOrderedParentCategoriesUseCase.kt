package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 並び順を考慮した親カテゴリ取得ユースケース
 */
class GetOrderedParentCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 並び順を考慮して親カテゴリを取得
     * 並び順が設定されていない場合は作成日時順で返す
     * 
     * @return 親カテゴリリスト（並び順済み）
     */
    suspend operator fun invoke(): Result<List<Category>> {
        return categoryRepository.getParentCategoriesOrdered()
    }
    
    /**
     * 並び順を考慮して親カテゴリを監視
     * 
     * @return 親カテゴリリストのFlow（並び順済み）
     */
    fun observe(): Flow<List<Category>> {
        return categoryRepository.observeParentCategoriesOrdered()
    }
}
