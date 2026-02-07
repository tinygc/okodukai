package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * カテゴリ更新ユースケース
 */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * カテゴリを更新する
     * 
     * @param categoryId 更新対象のカテゴリID
     * @param name カテゴリ名
     * @return 更新結果
     */
    suspend operator fun invoke(
        categoryId: String,
        name: String
    ): Result<Unit> {
        // バリデーション
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("カテゴリ名を入力してください"))
        }
        
        // 既存のカテゴリを取得
        val existingCategoryResult = categoryRepository.getCategoryById(categoryId)
        if (existingCategoryResult.isFailure) {
            return Result.failure(existingCategoryResult.exceptionOrNull()!!)
        }
        
        val existingCategory = existingCategoryResult.getOrNull()
            ?: return Result.failure(IllegalArgumentException("指定されたカテゴリが見つかりません"))
        
        val updatedCategory = Category(
            id = categoryId,
            name = name.trim(),
            parentId = existingCategory.parentId,
            createdAt = existingCategory.createdAt,
            updatedAt = DateTimeUtil.getCurrentDateTime()
        )
        
        return categoryRepository.saveCategory(updatedCategory)
    }
}
