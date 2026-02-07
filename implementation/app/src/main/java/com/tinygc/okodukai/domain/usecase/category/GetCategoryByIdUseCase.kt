package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * カテゴリIDからカテゴリ情報を取得するUseCase
 */
class GetCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: String): Result<Category?> {
        return categoryRepository.getCategoryById(categoryId)
    }
}
