package com.tinygc.okodukai.domain.repository

import com.tinygc.okodukai.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * カテゴリリポジトリインターフェース
 */
interface CategoryRepository {

    /**
     * カテゴリを保存（追加または更新）
     */
    suspend fun saveCategory(category: Category): Result<Unit>

    /**
     * カテゴリを削除
     */
    suspend fun deleteCategory(category: Category): Result<Unit>

    /**
     * IDでカテゴリを取得
     */
    suspend fun getCategoryById(id: String): Result<Category?>

    /**
     * 親カテゴリのみを取得
     */
    suspend fun getParentCategories(): Result<List<Category>>

    /**
     * 親カテゴリのみを監視
     */
    fun observeParentCategories(): Flow<List<Category>>

    /**
     * 親カテゴリの件数を取得
     */
    suspend fun getParentCategoryCount(): Result<Int>

    /**
     * 指定親カテゴリのサブカテゴリを取得
     */
    suspend fun getSubCategories(parentId: String): Result<List<Category>>

    /**
     * 指定親カテゴリのサブカテゴリを監視
     */
    fun observeSubCategories(parentId: String): Flow<List<Category>>

    /**
     * 指定親カテゴリのサブカテゴリ件数を取得
     */
    suspend fun getSubCategoryCount(parentId: String): Result<Int>

    /**
     * 全カテゴリを取得
     */
    suspend fun getAllCategories(): Result<List<Category>>

    /**
     * 全カテゴリを監視
     */
    fun observeAllCategories(): Flow<List<Category>>

    /**
     * カテゴリの並び順を更新（複数一括）
     * @param categoryIdsInOrder 並び順に並べ替え済みのカテゴリIDリスト
     * @param parentId 親カテゴリID（NULLの場合は親カテゴリ間の並び順を更新）
     */
    suspend fun updateCategoryOrder(categoryIdsInOrder: List<String>, parentId: String? = null): Result<Unit>

    /**
     * 親カテゴリを並び順を考慮して取得
     * 並び順が設定されていない場合は作成日時順で返す
     */
    suspend fun getParentCategoriesOrdered(): Result<List<Category>>

    /**
     * 親カテゴリを並び順を考慮して監視
     */
    fun observeParentCategoriesOrdered(): Flow<List<Category>>

    /**
     * 指定親カテゴリのサブカテゴリを並び順を考慮して取得
     */
    suspend fun getSubCategoriesOrdered(parentId: String): Result<List<Category>>

    /**
     * 指定親カテゴリのサブカテゴリを並び順を考慮して監視
     */
    fun observeSubCategoriesOrdered(parentId: String): Flow<List<Category>>
}
