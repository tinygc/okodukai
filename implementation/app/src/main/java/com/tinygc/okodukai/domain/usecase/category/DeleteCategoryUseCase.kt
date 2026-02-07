package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * カテゴリ削除ユースケース
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * カテゴリを削除する
     * 
     * @param category 削除対象のカテゴリ
     * @return 削除結果
     */
    suspend operator fun invoke(category: Category): Result<Unit> {
        return categoryRepository.deleteCategory(category)
    }
}
