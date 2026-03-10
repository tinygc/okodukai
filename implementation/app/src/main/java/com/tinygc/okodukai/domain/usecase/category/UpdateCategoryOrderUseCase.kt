package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * カテゴリ並び順更新ユースケース
 */
class UpdateCategoryOrderUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * カテゴリの並び順を更新
     * 
     * @param categoryIdsInOrder 並び順に並べ替え済みのカテゴリIDリスト
     * @param parentId 親カテゴリID（NULLの場合は親カテゴリ間の並び順を更新）
     * @return 更新結果
     */
    suspend operator fun invoke(categoryIdsInOrder: List<String>, parentId: String? = null): Result<Unit> {
        return categoryRepository.updateCategoryOrder(categoryIdsInOrder, parentId)
    }
}
