package com.tinygc.okodukai.domain.usecase.category

import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.util.DateTimeUtil
import javax.inject.Inject

/**
 * カテゴリ追加ユースケース
 * 
 * ビジネスルール：
 * - 親カテゴリ（parentId = null）は最大10件まで
 * - サブカテゴリは各親カテゴリごとに最大10件まで
 * - 制約チェックはRepositoryで実施
 */
class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * カテゴリを追加する
     * 
     * @param name カテゴリ名
     * @param parentId 親カテゴリID（nullの場合は親カテゴリ）
     * @return 追加結果
     */
    suspend operator fun invoke(
        name: String,
        parentId: String? = null
    ): Result<Unit> {
        // バリデーション
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("カテゴリ名を入力してください"))
        }
        
        val currentDateTime = DateTimeUtil.getCurrentDateTime()
        val category = Category(
            id = DateTimeUtil.generateId(),
            name = name.trim(),
            parentId = parentId,
            createdAt = currentDateTime,
            updatedAt = currentDateTime
        )
        
        return categoryRepository.saveCategory(category)
    }
}
